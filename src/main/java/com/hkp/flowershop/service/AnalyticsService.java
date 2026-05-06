package com.hkp.flowershop.service;

import com.hkp.flowershop.config.StockConfig;
import com.hkp.flowershop.dto.AnalyticsSummaryDto;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.repository.OrderRepo;
import com.hkp.flowershop.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final StockConfig stockConfig;

    public AnalyticsSummaryDto getSummary(String filter) {
        LocalDateTime start;
        LocalDateTime end;

        if (filter == null) filter = "today";
        switch (filter.toLowerCase()) {
            case "today":
                start = LocalDate.now().atStartOfDay();
                end = start.plusDays(1);
                break;
            case "week":
                LocalDate today = LocalDate.now();
                LocalDate monday = today.with(DayOfWeek.MONDAY);
                start = monday.atStartOfDay();
                end = start.plusWeeks(1);
                break;
            case "month":
                LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
                start = firstOfMonth.atStartOfDay();
                end = start.plusMonths(1);
                break;
            case "year":
                LocalDate firstOfYear = LocalDate.now().withDayOfYear(1);
                start = firstOfYear.atStartOfDay();
                end = start.plusYears(1);
                break;
            default:
                // fallback to today
                start = LocalDate.now().atStartOfDay();
                end = start.plusDays(1);
                break;
        }

        Double revenue = orderRepo.sumTotalPriceBetween(start, end);
        if (revenue == null) revenue = 0.0;

        Long orders = orderRepo.countByOrderDateBetween(start, end);
        if (orders == null) orders = 0L;

        long totalProducts = productRepo.count();
        Long lowStock = productRepo.countByStockLessThanEqual(stockConfig.getLowStockThreshold());
        if (lowStock == null) lowStock = 0L;
        List<AnalyticsSummaryDto.BestSellingProductDto> bestSellingProducts = orderRepo
                .findBestSellingProductsBetween(start, end, PageRequest.of(0, 5))
                .stream()
                .map(product -> AnalyticsSummaryDto.BestSellingProductDto.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .imageUrl(product.getImageUrl())
                        .totalSold(product.getTotalSold())
                        .build())
                .toList();

        List<AnalyticsSummaryDto.LowStockProductDto> lowStockProducts = productRepo
                .findByStockLessThanEqualOrderByStockAsc(stockConfig.getLowStockThreshold())
                .stream()
                .map(this::toLowStockProductDto)
                .toList();

        List<AnalyticsSummaryDto.RecentOrderDto> recentOrders = orderRepo
                .findRecentOrders(PageRequest.of(0, 8))
                .stream()
                .map(order -> AnalyticsSummaryDto.RecentOrderDto.builder()
                        .orderId(order.getOrderId())
                        .orderDate(order.getOrderDate())
                        .customerName(order.getCustomerName())
                        .status(order.getStatus())
                        .totalPrice(order.getTotalPrice())
                        .totalItems(order.getTotalItems())
                        .build())
                .toList();

        return AnalyticsSummaryDto.builder()
                .totalRevenue(revenue)
                .totalOrders(orders)
                .totalProducts(totalProducts)
                .lowStockCount(lowStock)
                .bestSellingProducts(bestSellingProducts)
                .lowStockProducts(lowStockProducts)
                .recentOrders(recentOrders)
                .build();
    }

    private AnalyticsSummaryDto.LowStockProductDto toLowStockProductDto(Product product) {
        return AnalyticsSummaryDto.LowStockProductDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .imageUrl(product.getImageUrl())
                .stock(product.getStock())
                .price(product.getPrice())
                .build();
    }

}
