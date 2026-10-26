package com.example.expensesplitter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_members")
public class TripMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "money_given", nullable = false, precision = 12, scale = 2)
    private BigDecimal moneyGiven;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMoneyGiven() { return moneyGiven; }
    public void setMoneyGiven(BigDecimal moneyGiven) { this.moneyGiven = moneyGiven; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
