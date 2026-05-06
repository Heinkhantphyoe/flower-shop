package com.hkp.flowershop.repository;

import com.hkp.flowershop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    Optional<Product> findById(Long productId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock <= :threshold")
    Long countByStockLessThanEqual(@Param("threshold") double threshold);

    List<Product> findByStockLessThanEqualOrderByStockAsc(double threshold);

}
