package com.hkp.flowershop.repository;

import com.hkp.flowershop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {


   Optional<Product> findById(Long productId);
}
