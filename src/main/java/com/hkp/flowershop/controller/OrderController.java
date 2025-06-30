package com.hkp.flowershop.controller;


import com.hkp.flowershop.dto.OrderDto;
import com.hkp.flowershop.dto.OrderItemsDto;
import com.hkp.flowershop.dto.ProductDto;
import com.hkp.flowershop.mapper.OrderMapper;
import com.hkp.flowershop.model.Order;
import com.hkp.flowershop.model.OrderItem;
import com.hkp.flowershop.service.OrderService;
import com.hkp.flowershop.service.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        List<Order> orders = orderService.getAllOrder();
        List<OrderDto> orderDtos = orderMapper.toDtoList(orders);
        return ResponseUtil.success(orderDtos);
    }

}
