package com.hkp.flowershop.repository;

import com.hkp.flowershop.model.Coupon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CouponRepoTest {

    @Autowired
    private CouponRepo couponRepo;

    @Test
    void findByCodeIgnoreCase_matchesRegardlessOfCase() {
        Coupon coupon = new Coupon();
        coupon.setCode("SPRING20");
        coupon.setAmount(20.0);
        couponRepo.save(coupon);

        assertThat(couponRepo.findByCodeIgnoreCase("spring20")).isPresent();
        assertThat(couponRepo.findByCodeIgnoreCase("SPRING20").get().getAmount()).isEqualTo(20.0);
    }

    @Test
    void existsByCodeIgnoreCase_detectsDuplicatesRegardlessOfCase() {
        Coupon coupon = new Coupon();
        coupon.setCode("WELCOME");
        coupon.setAmount(5.0);
        couponRepo.save(coupon);

        assertThat(couponRepo.existsByCodeIgnoreCase("welcome")).isTrue();
        assertThat(couponRepo.existsByCodeIgnoreCase("NEWCODE")).isFalse();
    }
}
