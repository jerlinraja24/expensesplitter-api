package com.example.expensesplitter.repository;

import com.example.expensesplitter.model.ExpenseMember;
import com.example.expensesplitter.model.ExpenseMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface ExpenseMemberRepository extends JpaRepository<ExpenseMember, ExpenseMemberId> {

    List<ExpenseMember> findByExpenseIdIn(Collection<Long> expenseIds);

    boolean existsByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ExpenseMember em where em.expenseId = :expenseId")
    void deleteByExpenseId(@Param("expenseId") Long expenseId);
}
