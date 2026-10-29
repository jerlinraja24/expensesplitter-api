package com.example.expensesplitter.repository;

import com.example.expensesplitter.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {
}
