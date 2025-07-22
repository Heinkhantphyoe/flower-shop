package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.requests.CreateOrderRequest;
import com.hkp.flowershop.enums.OrderStatus;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.model.Order;
import com.hkp.flowershop.model.OrderItem;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.repository.OrderRepo;
import com.hkp.flowershop.repository.ProductRepo;
import com.hkp.flowershop.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductRepo productRepo;


    public List<Order> getAllOrder() {
        return  orderRepo.findAll();


    }

    public Order createOrder(CreateOrderRequest request) {
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderAddress(request.getOrderAddress());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = new ArrayList<>();
        double total = 0;

        for (CreateOrderRequest.OrderItemDto itemDto : request.getOrderItems()) {
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
        order.setTotalPrice(total);

        return orderRepo.save(order);
    }
}
