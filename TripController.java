package com.example.expensesplitter.controller;

import com.example.expensesplitter.service.AuthService;
import com.example.expensesplitter.service.TripService;
import com.example.expensesplitter.util.Responses;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final AuthService auth;
    private final TripService trips;

    public TripController(AuthService auth, TripService trips) {
        this.auth = auth;
        this.trips = trips;
    }

    // ---------- trips ----------

    @GetMapping
    public Map<String, Object> list(@RequestHeader(value = "Authorization", required = false) String header) {
        return Responses.ok("trips", trips.listTrips(auth.requireUserId(header)));
    }

    @PostMapping
    public Map<String, Object> create(@RequestHeader(value = "Authorization", required = false) String header,
                                      @RequestBody Map<String, Object> body) {
        return trips.createTrip(auth.requireUserId(header), body);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@RequestHeader(value = "Authorization", required = false) String header,
                                   @PathVariable Long id) {
        return trips.getTrip(auth.requireUserId(header), id);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@RequestHeader(value = "Authorization", required = false) String header,
                                      @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return trips.updateTrip(auth.requireUserId(header), id, body);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@RequestHeader(value = "Authorization", required = false) String header,
                                      @PathVariable Long id) {
        return trips.deleteTrip(auth.requireUserId(header), id);
    }

    // ---------- people ----------

    @PostMapping("/{id}/members")
    public Map<String, Object> addMember(@RequestHeader(value = "Authorization", required = false) String header,
                                         @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return trips.addMember(auth.requireUserId(header), id, body);
    }

    @PutMapping("/{id}/members/{memberId}")
    public Map<String, Object> updateMember(@RequestHeader(value = "Authorization", required = false) String header,
                                            @PathVariable Long id, @PathVariable Long memberId,
                                            @RequestBody Map<String, Object> body) {
        return trips.updateMember(auth.requireUserId(header), id, memberId, body);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public Map<String, Object> deleteMember(@RequestHeader(value = "Authorization", required = false) String header,
                                            @PathVariable Long id, @PathVariable Long memberId) {
        return trips.deleteMember(auth.requireUserId(header), id, memberId);
    }

    // ---------- expenses ----------

    @PostMapping("/{id}/expenses")
    public Map<String, Object> addExpense(@RequestHeader(value = "Authorization", required = false) String header,
                                          @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return trips.addExpense(auth.requireUserId(header), id, body);
    }

    @PutMapping("/{id}/expenses/{expenseId}")
    public Map<String, Object> updateExpense(@RequestHeader(value = "Authorization", required = false) String header,
                                             @PathVariable Long id, @PathVariable Long expenseId,
                                             @RequestBody Map<String, Object> body) {
        return trips.updateExpense(auth.requireUserId(header), id, expenseId, body);
    }

    @DeleteMapping("/{id}/expenses/{expenseId}")
    public Map<String, Object> deleteExpense(@RequestHeader(value = "Authorization", required = false) String header,
                                             @PathVariable Long id, @PathVariable Long expenseId) {
        return trips.deleteExpense(auth.requireUserId(header), id, expenseId);
    }

    // ---------- summary ----------

    @GetMapping("/{id}/summary")
    public Map<String, Object> summary(@RequestHeader(value = "Authorization", required = false) String header,
                                       @PathVariable Long id) {
        return trips.summary(auth.requireUserId(header), id);
    }
}
