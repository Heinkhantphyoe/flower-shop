package com.hkp.flowershop.repository;

import com.hkp.flowershop.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemsRepo extends JpaRepository<CartItem, Long> {
}
