package com.hkp.flowershop.service.specification;

import com.hkp.flowershop.model.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;


public class ProductSpecification {

    public static Specification<Product> filterBy(
            String name,
            Integer categoryId,
            Integer minPrice,
            Integer maxPrice,
            Integer stockFilterId,
            double lowStockThreshold
    ) {
        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction(); // always true base

            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Stock filtering based on stockFilterId
            if (stockFilterId != null) {
                switch (stockFilterId) {
                    case 1: // InStock - stock > lowStockThreshold
                        predicate = cb.and(predicate,
                                cb.greaterThan(root.get("stock"), lowStockThreshold));
                        break;
                    case 2: // LowStock - 0 < stock <= lowStockThreshold
                        predicate = cb.and(predicate,
                                cb.and(
                                        cb.greaterThan(root.get("stock"), 0),
                                        cb.lessThanOrEqualTo(root.get("stock"), lowStockThreshold)
                                ));
                        break;
                    case 3: // OutOfStock - stock = 0
                        predicate = cb.and(predicate,
                                cb.equal(root.get("stock"), 0));
                        break;
                }
            }

            return predicate;
        };
    }
}
