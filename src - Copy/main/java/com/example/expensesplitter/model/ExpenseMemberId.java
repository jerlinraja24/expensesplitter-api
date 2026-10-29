package com.example.expensesplitter.model;

import java.io.Serializable;
import java.util.Objects;

/** Composite key for expense_members: (expense_id, member_id). */
public class ExpenseMemberId implements Serializable {

    private Long expenseId;
    private Long memberId;

    public ExpenseMemberId() {}

    public ExpenseMemberId(Long expenseId, Long memberId) {
        this.expenseId = expenseId;
        this.memberId = memberId;
    }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseMemberId other)) return false;
        return Objects.equals(expenseId, other.expenseId) && Objects.equals(memberId, other.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, memberId);
    }
}
