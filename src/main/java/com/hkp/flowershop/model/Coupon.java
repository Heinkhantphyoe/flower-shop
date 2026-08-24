package com.hkp.flowershop.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    // Fixed amount off the order subtotal
    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int usedCount = 0;
}
