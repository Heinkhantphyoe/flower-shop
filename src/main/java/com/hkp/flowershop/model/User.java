package com.hkp.flowershop.model;

import com.hkp.flowershop.enums.Role;
import com.hkp.flowershop.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;


    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    String address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String otpCode;

    private LocalDateTime otpGeneratedAt;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private String resetToken;

    private LocalDateTime resetTokenExpiry;

}

