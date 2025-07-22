package com.hkp.flowershop.controller;


import com.hkp.flowershop.dto.OrderDto;
import com.hkp.flowershop.dto.OrderItemsDto;
import com.hkp.flowershop.dto.ProductDto;
import com.hkp.flowershop.dto.requests.CreateOrderRequest;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.mapper.OrderMapper;
import com.hkp.flowershop.model.Order;
import com.hkp.flowershop.model.OrderItem;
import com.hkp.flowershop.service.OrderService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllOrders() {
        try{
            List<Order> orders = orderService.getAllOrder();
            List<OrderDto> orderDtos = orderMapper.toDtoList(orders);
            return ResponseUtil.success(orderDtos);
        }catch(Exception e){
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try{
            Order order = orderService.createOrder(request);
            OrderDto orderDto = orderMapper.toDto(order);
            return ResponseUtil.created(orderDto, "Order created successfully");
        }catch(BadRequestException e){
            return ResponseUtil.badRequest(e.getMessage());
        }catch(Exception e){
            return ResponseUtil.internalError("Internal Server Error");
        }
    }



}
