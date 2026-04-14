package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.OrderItemsDto;
import com.hkp.flowershop.dto.requests.CreateOrderRequest;
import com.hkp.flowershop.enums.OrderStatus;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
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
        double total = 0;

        for (OrderItemsDto itemDto : request.getOrderItems()) {
            Product product = productRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new BadRequestException("Product not found"));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(product.getPrice() * itemDto.getQuantity());

            total += item.getPrice();
            items.add(item);
        }

        order.setItems(items);
        int shippingFees = 5;
        order.setTotalPrice(total + shippingFees);

        return orderRepo.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Integer orderStatusCode) {
        if (orderStatusCode == null) {
            throw new BadRequestException("orderStatus is required");
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

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

        return orderRepo.save(order);
    }
}
