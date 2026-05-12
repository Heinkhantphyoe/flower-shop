package com.hkp.flowershop.repository;

import com.hkp.flowershop.enums.Role;
import com.hkp.flowershop.enums.UserStatus;
import com.hkp.flowershop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    public Optional<User> findByName(String username);

    public Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByName(String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.otpCode = :otp, u.otpGeneratedAt = :generatedAt WHERE u.email = :email")
    void updateOtpInfo(String otp,LocalDateTime generatedAt,String email );

    Optional<User> findByResetToken(String token);

    @Query(value = """
            SELECT u.id as customerId,
                   u.name as name,
                   u.email as email,
                   u.phoneNumber as phoneNumber,
                   u.profileImageUrl as profileImageUrl,
                   u.status as status,
                   u.createdAt as registeredAt,
                   COUNT(o.id) as totalOrders,
                   COALESCE(SUM(o.totalPrice), 0) as totalSpent
            FROM User u
            LEFT JOIN u.orders o
            WHERE u.role = :role
              AND (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR u.phoneNumber LIKE CONCAT('%', :keyword, '%')
                  )
            GROUP BY u.id, u.name, u.email, u.phoneNumber, u.profileImageUrl, u.status, u.createdAt
            """,
            countQuery = """
                    SELECT COUNT(u)
                    FROM User u
                    WHERE u.role = :role
                      AND (
                            :keyword IS NULL OR :keyword = ''
                            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR u.phoneNumber LIKE CONCAT('%', :keyword, '%')
                          )
                    """)
    Page<AdminCustomerProjection> findCustomersForAdmin(
            @Param("role") Role role,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    interface AdminCustomerProjection {
        Long getCustomerId();
        String getName();
        String getEmail();
        String getPhoneNumber();
        String getProfileImageUrl();
        UserStatus getStatus();
        LocalDateTime getRegisteredAt();
        Long getTotalOrders();
        Double getTotalSpent();
    }
}
