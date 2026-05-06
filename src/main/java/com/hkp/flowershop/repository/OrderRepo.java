package com.hkp.flowershop.repository;

import com.hkp.flowershop.enums.OrderStatus;
import com.hkp.flowershop.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
	Page<Order> findByStatus(OrderStatus status, Pageable pageable);

	@Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.orderDate >= :start AND o.orderDate < :end")
	Double sumTotalPriceBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	@Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :start AND o.orderDate < :end")
	Long countByOrderDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	@Query("""
			SELECT p.id as productId, p.name as productName, p.imageUrl as imageUrl, COALESCE(SUM(oi.quantity), 0) as totalSold
			FROM OrderItem oi
			JOIN oi.order o
			JOIN oi.product p
			WHERE o.orderDate >= :start AND o.orderDate < :end
			GROUP BY p.id, p.name, p.imageUrl
			ORDER BY SUM(oi.quantity) DESC
			""")
	Page<BestSellingProductProjection> findBestSellingProductsBetween(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end,
			Pageable pageable
	);

	@Query("""
			SELECT o.id as orderId, o.orderDate as orderDate, u.name as customerName, o.status as status, o.totalPrice as totalPrice, COALESCE(SUM(oi.quantity), 0) as totalItems
			FROM Order o
			JOIN o.user u
			LEFT JOIN o.items oi
			GROUP BY o.id, o.orderDate, u.name, o.status, o.totalPrice
			ORDER BY o.orderDate DESC
			""")
	Page<RecentOrderProjection> findRecentOrders(Pageable pageable);

	interface BestSellingProductProjection {
		Long getProductId();
		String getProductName();
		String getImageUrl();
		Long getTotalSold();
	}

	interface RecentOrderProjection {
		Long getOrderId();
		LocalDateTime getOrderDate();
		String getCustomerName();
		OrderStatus getStatus();
		Double getTotalPrice();
		Long getTotalItems();
	}
}
