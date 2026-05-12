package com.hkp.flowershop.service;

import com.hkp.flowershop.config.StockConfig;
import com.hkp.flowershop.dto.AnalyticsSummaryDto;
import com.hkp.flowershop.enums.OrderStatus;
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
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final StockConfig stockConfig;

    public AnalyticsSummaryDto getSummary(String filter) {
        String normalizedFilter = normalizeFilter(filter);
        LocalDateTime start;
        LocalDateTime end;

        switch (normalizedFilter) {
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

        List<AnalyticsSummaryDto.SalesOverviewDto> salesOverview = buildSalesOverview(normalizedFilter, start, end);
        List<AnalyticsSummaryDto.OrderStatusDto> orderStatus = buildOrderStatus(start, end);

        return AnalyticsSummaryDto.builder()
                .totalRevenue(revenue)
                .totalOrders(orders)
                .totalProducts(totalProducts)
                .lowStockCount(lowStock)
                .bestSellingProducts(bestSellingProducts)
                .lowStockProducts(lowStockProducts)
                .recentOrders(recentOrders)
                .salesOverview(salesOverview)
                .orderStatus(orderStatus)
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

    private String normalizeFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            return "today";
        }
        return switch (filter.toLowerCase()) {
            case "this_week" -> "week";
            case "this_month" -> "month";
            case "this_year" -> "year";
            default -> filter.toLowerCase();
        };
    }

    private List<AnalyticsSummaryDto.SalesOverviewDto> buildSalesOverview(String filter, LocalDateTime start, LocalDateTime end) {
        List<AnalyticsSummaryDto.SalesOverviewDto> points = initializeSalesOverviewBuckets(filter, start);
        List<OrderRepo.OrderSalesProjection> salesRows = orderRepo.findOrderSalesBetween(start, end);

        for (OrderRepo.OrderSalesProjection row : salesRows) {
            int index = resolveBucketIndex(filter, row.getOrderDate());
            if (index < 0 || index >= points.size()) {
                continue;
            }

            AnalyticsSummaryDto.SalesOverviewDto current = points.get(index);
            double totalRevenue = current.getRevenue() + (row.getTotalPrice() == null ? 0 : row.getTotalPrice());
            long totalOrders = current.getOrders() + 1;
            current.setRevenue(totalRevenue);
            current.setOrders(totalOrders);
        }
        return points;
    }

    private List<AnalyticsSummaryDto.SalesOverviewDto> initializeSalesOverviewBuckets(String filter, LocalDateTime start) {
        List<AnalyticsSummaryDto.SalesOverviewDto> buckets = new ArrayList<>();

        switch (filter) {
            case "today":
                for (int hour = 0; hour < 24; hour++) {
                    String label = String.format("%02d:00", hour);
                    buckets.add(createSalesBucket(label));
                }
                break;
            case "week":
                String[] weekLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                for (String label : weekLabels) {
                    buckets.add(createSalesBucket(label));
                }
                break;
            case "month":
                int totalDays = start.toLocalDate().lengthOfMonth();
                for (int day = 1; day <= totalDays; day++) {
                    buckets.add(createSalesBucket(String.valueOf(day)));
                }
                break;
            case "year":
                for (Month month : Month.values()) {
                    String label = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    buckets.add(createSalesBucket(label));
                }
                break;
            default:
                for (int hour = 0; hour < 24; hour++) {
                    String label = String.format("%02d:00", hour);
                    buckets.add(createSalesBucket(label));
                }
                break;
        }
        return buckets;
    }

    private int resolveBucketIndex(String filter, LocalDateTime orderDate) {
        return switch (filter) {
            case "today" -> orderDate.getHour();
            case "week" -> orderDate.getDayOfWeek().getValue() - 1;
            case "month" -> orderDate.getDayOfMonth() - 1;
            case "year" -> orderDate.getMonthValue() - 1;
            default -> orderDate.getHour();
        };
    }

    private AnalyticsSummaryDto.SalesOverviewDto createSalesBucket(String label) {
        return AnalyticsSummaryDto.SalesOverviewDto.builder()
                .label(label)
                .revenue(0)
                .orders(0)
                .build();
    }

    private List<AnalyticsSummaryDto.OrderStatusDto> buildOrderStatus(LocalDateTime start, LocalDateTime end) {
        EnumMap<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, 0L);
        }

        for (OrderRepo.OrderStatusCountProjection row : orderRepo.countByStatusBetween(start, end)) {
            counts.put(row.getStatus(), row.getTotal() == null ? 0L : row.getTotal());
        }

        long totalOrders = counts.values().stream().mapToLong(Long::longValue).sum();

        return counts.entrySet().stream()
                .map(entry -> AnalyticsSummaryDto.OrderStatusDto.builder()
                        .name(toDisplayStatus(entry.getKey()))
                        .value(toPercentage(entry.getValue(), totalOrders))
                        .build())
                .toList();
    }

    private String toDisplayStatus(OrderStatus status) {
        String value = status.name().toLowerCase(Locale.ENGLISH);
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private double toPercentage(long count, long total) {
        if (total == 0) {
            return 0;
        }
        double percentage = (count * 100.0) / total;
        return Math.round(percentage * 100.0) / 100.0;
    }

}
