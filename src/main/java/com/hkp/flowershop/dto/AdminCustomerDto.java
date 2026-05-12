package com.hkp.flowershop.dto;

import com.hkp.flowershop.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCustomerDto {
    private Long customerId;
    private String name;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private UserStatus status;
    private LocalDateTime registeredAt;
    private Long totalOrders;
    private Double totalSpent;
    private List<RecentOrderDto> recentOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderDto {
        private String id;
        private LocalDate date;
        private double total;
        private String status;
    }
}
