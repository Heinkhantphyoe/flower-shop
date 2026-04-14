package com.hkp.flowershop.controller;


import com.hkp.flowershop.dto.OrderDto;
import com.hkp.flowershop.dto.requests.CreateOrderRequest;
import com.hkp.flowershop.dto.requests.PaginationRequest;
import com.hkp.flowershop.dto.requests.UpdateOrderStatusRequest;
import com.hkp.flowershop.dto.response.PaginationResponse;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.mapper.OrderMapper;
import com.hkp.flowershop.model.Order;
import com.hkp.flowershop.service.OrderService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public ResponseEntity<?> getAllOrders(PaginationRequest paginationRequest,
                                          @RequestParam(required = false) Integer orderStatus) {
        try{
            Pageable pageable = paginationRequest.toPageable();
            Page<Order> pageOrders = orderService.getAllOrder(pageable, orderStatus);

            List<OrderDto> orderDtos = pageOrders.stream()
                    .map(orderMapper::toDto)
                    .toList();

            PaginationResponse<OrderDto> response = new PaginationResponse<>(orderDtos, pageOrders);
            return ResponseUtil.success(response);
        }catch(BadRequestException e){
            return ResponseUtil.badRequest(e.getMessage());
        }catch(Exception e){
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrderByCustomer(@ModelAttribute CreateOrderRequest request,
                                                   Principal principal) {

        log.info("request >>>> {}",request);
        try{
            Order order = orderService.createOrder(request,principal.getName());
            OrderDto orderDto = orderMapper.toDto(order);
            return ResponseUtil.created(orderDto, "Order created successfully");
        }catch(BadRequestException e){
            return ResponseUtil.badRequest(e.getMessage());
        }catch(Exception e){
            return ResponseUtil.internalError("Internal Server Error");
        }
    }


    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                               @Valid @RequestBody UpdateOrderStatusRequest request) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, request.getOrderStatus());
            OrderDto orderDto = orderMapper.toDto(updatedOrder);
            return ResponseUtil.success(orderDto, "Order status updated successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalError("Internal Server Error");
        }
    }



}
