package com.hkp.flowershop.service.ai;

import com.hkp.flowershop.config.StockConfig;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.repository.CategoryRepo;
import com.hkp.flowershop.repository.OrderRepo;
import com.hkp.flowershop.repository.ProductRepo;
import com.hkp.flowershop.repository.UserRepo;
import com.hkp.flowershop.service.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopTools {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;
    private final StockConfig stockConfig;

    @Tool(description = "Search the flower shop catalog by keyword. Use for questions about available flowers, bouquets, prices or stock. Returns a list of matching products with id, name, price, stock and category.")
    public List<ProductSummary> searchProducts(
            @ToolParam(description = "Search keyword from the customer message, e.g. 'rose', 'tulip', 'bouquet'") String keyword) {
        Specification<Product> spec = ProductSpecification.filterBy(
                keyword, null, null, null, null, stockConfig.getLowStockThreshold());
        Page<Product> page = productRepo.findAll(spec, PageRequest.of(0, 6));
        return page.getContent().stream().map(ProductSummary::from).toList();
    }

    @Tool(description = "List all product categories offered by the shop, e.g. to suggest what the customer can browse.")
    public List<String> getCategories() {
        return categoryRepo.findAll().stream()
                .map(category -> category.getName())
                .toList();
    }

    @Tool(description = "Get the current logged-in customer's 5 most recent orders with status, total price and date. Use whenever the customer asks about their orders or delivery status.")
    public List<OrderSummary> getMyRecentOrders() {
        User user = currentUser();
        Page<OrderRepo.CustomerRecentOrderProjection> page =
                orderRepo.findRecentOrdersByUserId(user.getId(), PageRequest.of(0, 5));
        return page.getContent().stream()
                .map(order -> new OrderSummary(
                        order.getOrderId(),
                        order.getOrderDate().format(DATE_FORMAT),
                        order.getStatus() != null ? order.getStatus().name() : "UNKNOWN",
                        order.getTotalPrice(),
                        0,
                        null))
                .toList();
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BadRequestException("Not authenticated");
        }
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    public record ProductSummary(long id, String name, double price, double stock, String category) {
        static ProductSummary from(Product p) {
            return new ProductSummary(
                    p.getId(),
                    p.getName(),
                    p.getPrice(),
                    p.getStock(),
                    p.getCategory() != null ? p.getCategory().getName() : "");
        }
    }

    public record OrderSummary(long orderId, String orderDate, String status, double totalPrice, int itemCount, String deliveryDate) {
    }
}
