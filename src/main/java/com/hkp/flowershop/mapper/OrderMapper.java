package com.hkp.flowershop.mapper;


import com.hkp.flowershop.dto.OrderDto;
import com.hkp.flowershop.dto.OrderItemsDto;
import com.hkp.flowershop.model.Order;
import com.hkp.flowershop.model.OrderItem;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    @Autowired
    ModelMapper modelMapper;

    public OrderDto toDto(Order order) {
        OrderDto dto = modelMapper.map(order, OrderDto.class);

        List<OrderItemsDto> itemDtos = order.getItems().stream()
                .map(this::toItemDto)
                .toList();
        dto.setItems(itemDtos);
        return dto;
    }

    private OrderItemsDto toItemDto(OrderItem item) {
        OrderItemsDto dto = new OrderItemsDto();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductImageUrl(item.getProduct().getImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }

    public List<OrderDto> toDtoList(List<Order> orders) {
        return orders.stream().map(this::toDto).toList();
    }
}

