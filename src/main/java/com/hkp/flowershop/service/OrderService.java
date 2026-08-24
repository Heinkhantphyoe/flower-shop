package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.OrderItemsDto;
import com.hkp.flowershop.dto.requests.CreateOrderRequest;
import com.hkp.flowershop.enums.OrderStatus;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.Coupon;
import com.hkp.flowershop.model.Order;
import com.hkp.flowershop.model.OrderItem;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.repository.OrderRepo;
import com.hkp.flowershop.repository.ProductRepo;
import com.hkp.flowershop.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;

    private final ModelMapper modelMapper;

    private final UserRepo userRepo;

    private final ProductRepo productRepo;

    private final FileStorageService fileStorageService;

    private final CouponService couponService;

    public Page<Order> getAllOrder(Pageable pageable, Integer orderStatus) {
        if (orderStatus == null) {
            return orderRepo.findAll(pageable);
        }

        try {
            OrderStatus status = OrderStatus.fromCode(orderStatus);
            return orderRepo.findByStatus(status, pageable);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid orderStatus. Use: 0=PENDING, 1=CONFIRMED, 2=DELIVERED, 3=CANCELLED");
        }
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request,String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Order order = new Order();
        MultipartFile imageFile = request.getPaymentSs();
        fileStorageService.validateImageFile(imageFile);


        String fileName = fileStorageService.saveImage(imageFile);
        order.setPaymentSs(fileName);
        order.setUser(user);
        order.setOrderAddress(request.getOrderAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setCity(request.getCity());
        order.setZipCode(request.getZipCode());

        List<OrderItem> items = new ArrayList<>();
        double subtotal = 0;
        List<Product> changedProducts = new ArrayList<>();

        for (OrderItemsDto itemDto : request.getOrderItems()) {
            Product product = productRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new BadRequestException("Product not found"));

            if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                throw new BadRequestException("Invalid quantity for product: " + product.getName());
            }
            // Oversell guard
            if (product.getStock() < itemDto.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for " + product.getName()
                                + ". Only " + (int) product.getStock() + " left");
            }

            // Decrement stock atomically within the transaction
            product.setStock(product.getStock() - itemDto.getQuantity());
            changedProducts.add(product);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            // Use sale price when set; never trust client-sent prices
            item.setPrice(product.getEffectivePrice() * itemDto.getQuantity());

            subtotal += item.getPrice();
            items.add(item);
        }
        productRepo.saveAll(changedProducts);

        double discountAmount = 0;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponService.validateForOrder(request.getCouponCode(), subtotal);
            discountAmount = couponService.calculateDiscount(coupon, subtotal);
            order.setCouponCode(coupon.getCode());
            order.setDiscountAmount(discountAmount);
            couponService.markUsed(coupon);
        }

        order.setItems(items);
        int shippingFees = 5;
        order.setTotalPrice(subtotal - discountAmount + shippingFees);

        return orderRepo.save(order);
    }

    public Page<Order> getOrdersForUser(String userEmail, Pageable pageable, Integer orderStatus) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (orderStatus == null) {
            return orderRepo.findByUserId(user.getId(), pageable);
        }

        try {
            OrderStatus status = OrderStatus.fromCode(orderStatus);
            return orderRepo.findByUserIdAndStatus(user.getId(), status, pageable);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid orderStatus. Use: 0=PENDING, 1=CONFIRMED, 2=DELIVERED, 3=CANCELLED");
        }
    }

    @Transactional
    public Order cancelOrder(Long orderId, String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Hide orders belonging to other users
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only pending orders can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        restockOrder(order);
        return orderRepo.save(order);
    }

    // Returns ordered items to inventory; only call on a transition to CANCELLED
    private void restockOrder(Order order) {
        List<Product> changedProducts = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            changedProducts.add(product);
        }
        productRepo.saveAll(changedProducts);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Integer orderStatusCode) {
        if (orderStatusCode == null) {
            throw new BadRequestException("orderStatus is required");
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus previousStatus = order.getStatus();

        OrderStatus status;
        try {
            status = OrderStatus.fromCode(orderStatusCode);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid orderStatus. Use: 0=PENDING, 1=CONFIRMED, 2=DELIVERED, 3=CANCELLED");
        }

        order.setStatus(status);
        if (status == OrderStatus.DELIVERED) {
            if (order.getDeliveryDate() == null) {
                order.setDeliveryDate(LocalDateTime.now());
            }
        } else {
            order.setDeliveryDate(null);
        }

        if (status == OrderStatus.CANCELLED && previousStatus != OrderStatus.CANCELLED) {
            restockOrder(order);
        }

        return orderRepo.save(order);
    }
}
