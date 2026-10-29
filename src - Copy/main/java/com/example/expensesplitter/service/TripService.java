package com.example.expensesplitter.service;

import com.example.expensesplitter.exception.ApiException;
import com.example.expensesplitter.model.Expense;
import com.example.expensesplitter.model.ExpenseMember;
import com.example.expensesplitter.model.Trip;
import com.example.expensesplitter.model.TripMember;
import com.example.expensesplitter.repository.ExpenseMemberRepository;
import com.example.expensesplitter.repository.ExpenseRepository;
import com.example.expensesplitter.repository.TripMemberRepository;
import com.example.expensesplitter.repository.TripRepository;
import com.example.expensesplitter.util.Input;
import com.example.expensesplitter.util.Responses;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TripService {

    public static final List<String> CATEGORIES =
            List.of("Food & Dining", "Transport", "Stay", "Shopping", "Entertainment", "Other");

    private static final BigDecimal MAX_MONEY = new BigDecimal("10000000");

    private final TripRepository trips;
    private final TripMemberRepository members;
    private final ExpenseRepository expenses;
    private final ExpenseMemberRepository shares;

    public TripService(TripRepository trips, TripMemberRepository members,
                       ExpenseRepository expenses, ExpenseMemberRepository shares) {
        this.trips = trips;
        this.members = members;
        this.expenses = expenses;
        this.shares = shares;
    }

    // ---------------- TRIPS ----------------

    public List<Map<String, Object>> listTrips(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Trip t : trips.findByUserIdOrderByCreatedAtDesc(userId)) {
            List<Expense> es = expenses.findByTripIdOrderByExpenseDateDescIdDesc(t.getId());
            int memberCount = members.findByTripIdOrderByIdAsc(t.getId()).size();
            result.add(Responses.map(
                    "id", t.getId(),
                    "name", t.getName(),
                    "startDate", t.getStartDate(),
                    "endDate", t.getEndDate(),
                    "memberCount", memberCount,
                    "totalSpent", sumAmounts(es)));
        }
        return result;
    }

    public Map<String, Object> createTrip(Long userId, Map<String, Object> body) {
        Trip trip = new Trip();
        trip.setUserId(userId);
        trip.setCreatedAt(LocalDateTime.now());
        applyTrip(trip, body);
        return Responses.ok("trip", tripMap(trips.save(trip)));
    }

    public Map<String, Object> updateTrip(Long userId, Long tripId, Map<String, Object> body) {
        Trip trip = owned(userId, tripId);
        applyTrip(trip, body);
        trips.save(trip);
        return Responses.ok();
    }

    public Map<String, Object> deleteTrip(Long userId, Long tripId) {
        trips.delete(owned(userId, tripId));
        return Responses.ok();
    }

    public Map<String, Object> getTrip(Long userId, Long tripId) {
        Trip trip = owned(userId, tripId);
        List<TripMember> ms = members.findByTripIdOrderByIdAsc(tripId);
        List<Expense> es = expenses.findByTripIdOrderByExpenseDateDescIdDesc(tripId);

        Map<Long, List<Long>> memberIdsByExpense = new HashMap<>();
        List<Long> expenseIds = es.stream().map(Expense::getId).toList();
        if (!expenseIds.isEmpty()) {
            for (ExpenseMember row : shares.findByExpenseIdIn(expenseIds)) {
                memberIdsByExpense.computeIfAbsent(row.getExpenseId(), k -> new ArrayList<>()).add(row.getMemberId());
            }
        }

        List<Map<String, Object>> memberList = ms.stream().map(this::memberMap).toList();
        List<Map<String, Object>> expenseList = es.stream()
                .map(e -> expenseMap(e, memberIdsByExpense.getOrDefault(e.getId(), List.of())))
                .toList();

        return Responses.ok(
                "trip", tripMap(trip),
                "members", memberList,
                "expenses", expenseList,
                "categories", CATEGORIES);
    }

    // ---------------- MEMBERS ----------------

    public Map<String, Object> addMember(Long userId, Long tripId, Map<String, Object> body) {
        owned(userId, tripId);
        String name = memberName(body);
        ensureUniqueName(tripId, name, null);
        TripMember m = new TripMember();
        m.setTripId(tripId);
        m.setName(name);
        m.setMoneyGiven(moneyGiven(body));
        m.setCreatedAt(LocalDateTime.now());
        return Responses.ok("member", memberMap(members.save(m)));
    }

    public Map<String, Object> updateMember(Long userId, Long tripId, Long memberId, Map<String, Object> body) {
        owned(userId, tripId);
        TripMember m = members.findByIdAndTripId(memberId, tripId).orElseThrow(this::notFound);
        String name = memberName(body);
        ensureUniqueName(tripId, name, memberId);
        m.setName(name);
        m.setMoneyGiven(moneyGiven(body));
        members.save(m);
        return Responses.ok();
    }

    public Map<String, Object> deleteMember(Long userId, Long tripId, Long memberId) {
        owned(userId, tripId);
        TripMember m = members.findByIdAndTripId(memberId, tripId).orElseThrow(this::notFound);
        if (shares.existsByMemberId(memberId)) {
            throw new ApiException(400, "This person is part of expenses. Remove those expenses first.");
        }
        members.delete(m);
        return Responses.ok();
    }

    // ---------------- EXPENSES ----------------

    @Transactional
    public Map<String, Object> addExpense(Long userId, Long tripId, Map<String, Object> body) {
        owned(userId, tripId);
        Expense e = new Expense();
        e.setTripId(tripId);
        e.setCreatedAt(LocalDateTime.now());
        List<Long> memberIds = applyExpense(tripId, e, body);
        Expense saved = expenses.save(e);
        writeShares(saved.getId(), memberIds, saved.getAmount());
        return Responses.ok("expense", expenseMap(saved, memberIds));
    }

    @Transactional
    public Map<String, Object> updateExpense(Long userId, Long tripId, Long expenseId, Map<String, Object> body) {
        owned(userId, tripId);
        Expense e = expenses.findByIdAndTripId(expenseId, tripId).orElseThrow(this::notFound);
        List<Long> memberIds = applyExpense(tripId, e, body);
        Expense saved = expenses.save(e);
        shares.deleteByExpenseId(expenseId);
        writeShares(saved.getId(), memberIds, saved.getAmount());
        return Responses.ok();
    }

    @Transactional
    public Map<String, Object> deleteExpense(Long userId, Long tripId, Long expenseId) {
        owned(userId, tripId);
        Expense e = expenses.findByIdAndTripId(expenseId, tripId).orElseThrow(this::notFound);
        expenses.delete(e);
        shares.deleteByExpenseId(expenseId);
        return Responses.ok();
    }

    // ---------------- SUMMARY ----------------

    public Map<String, Object> summary(Long userId, Long tripId) {
        owned(userId, tripId);
        List<TripMember> ms = members.findByTripIdOrderByIdAsc(tripId);
        List<Expense> es = expenses.findByTripIdOrderByExpenseDateDescIdDesc(tripId);
        BigDecimal total = sumAmounts(es);

        List<Long> expenseIds = es.stream().map(Expense::getId).toList();
        Map<Long, BigDecimal> spentBy = new HashMap<>();
        if (!expenseIds.isEmpty()) {
            for (ExpenseMember row : shares.findByExpenseIdIn(expenseIds)) {
                spentBy.merge(row.getMemberId(), row.getShareAmount(), BigDecimal::add);
            }
        }

        List<Map<String, Object>> people = new ArrayList<>();
        for (TripMember m : ms) {
            BigDecimal spent = spentBy.getOrDefault(m.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal given = m.getMoneyGiven().setScale(2, RoundingMode.HALF_UP);
            people.add(Responses.map(
                    "id", m.getId(),
                    "name", m.getName(),
                    "moneyGiven", given,
                    "spent", spent,
                    "balance", given.subtract(spent)));
        }
        return Responses.ok("totalSpent", total, "people", people);
    }

    // ---------------- HELPERS ----------------

    private Trip owned(Long userId, Long tripId) {
        return trips.findByIdAndUserId(tripId, userId).orElseThrow(this::notFound);
    }

    private ApiException notFound() {
        return new ApiException(404, "Not found.");
    }

    private void applyTrip(Trip trip, Map<String, Object> body) {
        String name = Input.text(body, "name");
        if (name.isEmpty() || name.length() > 100) {
            throw new ApiException(400, "Trip name is required (up to 100 characters).");
        }
        LocalDate start = Input.date(body, "startDate");
        LocalDate end = Input.date(body, "endDate");
        if (start != null && end != null && end.isBefore(start)) {
            throw new ApiException(400, "End date must be on or after the start date.");
        }
        trip.setName(name);
        trip.setStartDate(start);
        trip.setEndDate(end);
    }

    private String memberName(Map<String, Object> body) {
        String name = Input.text(body, "name");
        if (name.isEmpty() || name.length() > 60) {
            throw new ApiException(400, "Person name is required (up to 60 characters).");
        }
        return name;
    }

    private BigDecimal moneyGiven(Map<String, Object> body) {
        BigDecimal value = Input.money(body, "moneyGiven", BigDecimal.ZERO.setScale(2));
        if (value.signum() < 0 || value.compareTo(MAX_MONEY) > 0) {
            throw new ApiException(400, "Money given must be between 0 and 10,000,000.");
        }
        return value;
    }

    private void ensureUniqueName(Long tripId, String name, Long exceptMemberId) {
        for (TripMember existing : members.findByTripIdAndNameIgnoreCase(tripId, name)) {
            if (exceptMemberId == null || !exceptMemberId.equals(existing.getId())) {
                throw new ApiException(400, "A person with this name is already in this trip.");
            }
        }
    }

    private List<Long> applyExpense(Long tripId, Expense e, Map<String, Object> body) {
        LocalDate date = Input.date(body, "date");
        if (date == null) {
            throw new ApiException(400, "Choose a date for this expense.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ApiException(400, "The date cannot be in the future.");
        }
        String category = Input.text(body, "category");
        if (!CATEGORIES.contains(category)) {
            throw new ApiException(400, "Choose a category from the list.");
        }
        BigDecimal amount = Input.money(body, "amount", null);
        if (amount == null || amount.signum() <= 0) {
            throw new ApiException(400, "Enter an amount greater than 0.");
        }
        if (amount.compareTo(MAX_MONEY) > 0) {
            throw new ApiException(400, "Amount must be 10,000,000 or less.");
        }
        String description = Input.text(body, "description");
        if (description.length() > 200) {
            throw new ApiException(400, "Description must be 200 characters or fewer.");
        }
        List<Long> memberIds = Input.ids(body, "memberIds");
        if (memberIds.isEmpty()) {
            throw new ApiException(400, "Select at least one person who shares this expense.");
        }
        Set<Long> valid = members.findByTripIdOrderByIdAsc(tripId).stream()
                .map(TripMember::getId).collect(Collectors.toSet());
        if (!valid.containsAll(memberIds)) {
            throw new ApiException(400, "Each selected person must belong to this trip.");
        }
        e.setExpenseDate(date);
        e.setCategory(category);
        e.setAmount(amount);
        e.setDescription(description.isEmpty() ? null : description);
        return memberIds;
    }

    /** Splits the amount equally in cents; the last person absorbs any rounding difference. */
    private void writeShares(Long expenseId, List<Long> memberIds, BigDecimal amount) {
        long cents = amount.movePointRight(2).longValueExact();
        int n = memberIds.size();
        long base = cents / n;
        long remainder = cents - base * n;
        for (int i = 0; i < n; i++) {
            long shareCents = base + (i == n - 1 ? remainder : 0);
            shares.save(new ExpenseMember(expenseId, memberIds.get(i), BigDecimal.valueOf(shareCents, 2)));
        }
    }

    private BigDecimal sumAmounts(List<Expense> list) {
        return list.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, Object> tripMap(Trip t) {
        return Responses.map(
                "id", t.getId(),
                "name", t.getName(),
                "startDate", t.getStartDate(),
                "endDate", t.getEndDate());
    }

    public Map<String, Object> memberMap(TripMember m) {
        return Responses.map(
                "id", m.getId(),
                "name", m.getName(),
                "moneyGiven", m.getMoneyGiven());
    }

    public Map<String, Object> expenseMap(Expense e, List<Long> memberIds) {
        return Responses.map(
                "id", e.getId(),
                "date", e.getExpenseDate(),
                "category", e.getCategory(),
                "amount", e.getAmount(),
                "description", e.getDescription() == null ? "" : e.getDescription(),
                "memberIds", memberIds);
    }
}
