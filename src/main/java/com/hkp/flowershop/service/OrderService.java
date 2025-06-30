package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.OrderDto;
import com.hkp.flowershop.dto.OrderItemsDto;
import com.hkp.flowershop.model.Order;
import com.hkp.flowershop.repository.OrderRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ModelMapper modelMapper;


    public List<Order> getAllOrder() {
        return  orderRepo.findAll();


    }
}
