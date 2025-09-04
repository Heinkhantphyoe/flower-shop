package com.hkp.flowershop.repository;

import com.hkp.flowershop.model.Cart;
import com.hkp.flowershop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart,Long> {
    Optional<Cart> findByUser(User user);
}
