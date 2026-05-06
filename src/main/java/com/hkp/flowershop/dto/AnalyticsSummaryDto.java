package com.hkp.flowershop.dto;

import com.hkp.flowershop.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for analytics summary response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDto {
    private double totalRevenue;
    private long totalOrders;
    private long totalProducts;
    private long lowStockCount;
    private List<BestSellingProductDto> bestSellingProducts;
    private List<LowStockProductDto> lowStockProducts;
    private List<RecentOrderDto> recentOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BestSellingProductDto {
        private Long productId;
        private String productName;
        private String imageUrl;
        private Long totalSold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockProductDto {
        private Long productId;
        private String productName;
        private String imageUrl;
        private double stock;
        private double price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderDto {
        private Long orderId;
        private LocalDateTime orderDate;
        private String customerName;
        private OrderStatus status;
        private double totalPrice;
        private Long totalItems;
    }
}
