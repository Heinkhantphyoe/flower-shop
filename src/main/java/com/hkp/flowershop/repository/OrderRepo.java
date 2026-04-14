package com.hkp.flowershop.repository;

import com.hkp.flowershop.enums.OrderStatus;
import com.hkp.flowershop.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
	Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
