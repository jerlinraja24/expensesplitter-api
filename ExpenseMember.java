package com.example.expensesplitter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_members")
@IdClass(ExpenseMemberId.class)
public class ExpenseMember {

    @Id
    @Column(name = "expense_id")
    private Long expenseId;

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "share_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal shareAmount;

    public ExpenseMember() {}

    public ExpenseMember(Long expenseId, Long memberId, BigDecimal shareAmount) {
        this.expenseId = expenseId;
        this.memberId = memberId;
        this.shareAmount = shareAmount;
    }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public BigDecimal getShareAmount() { return shareAmount; }
    public void setShareAmount(BigDecimal shareAmount) { this.shareAmount = shareAmount; }
}
