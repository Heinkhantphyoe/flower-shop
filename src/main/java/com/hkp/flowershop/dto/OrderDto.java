package com.hkp.flowershop.dto;

import com.hkp.flowershop.enums.OrderStatus;
import com.hkp.flowershop.model.OrderItem;
import com.hkp.flowershop.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private List<OrderItemsDto> items;
    private String orderAddress;
    private double totalPrice;
}
