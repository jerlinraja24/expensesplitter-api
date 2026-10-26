package com.example.expensesplitter.repository;

import com.example.expensesplitter.model.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TripMemberRepository extends JpaRepository<TripMember, Long> {
    List<TripMember> findByTripIdOrderByIdAsc(Long tripId);
    Optional<TripMember> findByIdAndTripId(Long id, Long tripId);
    List<TripMember> findByTripIdAndNameIgnoreCase(Long tripId, String name);
}
