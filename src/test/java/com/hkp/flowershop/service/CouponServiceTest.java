package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.requests.CreateCouponRequest;
import com.hkp.flowershop.dto.requests.UpdateCouponRequest;
import com.hkp.flowershop.exceptions.BadRequestException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.Coupon;
import com.hkp.flowershop.repository.CouponRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepo couponRepo;

    @InjectMocks
    private CouponService couponService;

    @Test
    void createCoupon_normalizesCodeAndSaves() {
        CreateCouponRequest request = new CreateCouponRequest();
        request.setCode("  save10  ");
        request.setAmount(10.0);

        when(couponRepo.existsByCodeIgnoreCase("SAVE10")).thenReturn(false);
        when(couponRepo.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Coupon result = couponService.createCoupon(request);

        assertThat(result.getCode()).isEqualTo("SAVE10");
        assertThat(result.getAmount()).isEqualTo(10.0);
        assertThat(result.isActive()).isTrue();

        ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepo).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("SAVE10");
    }

    @Test
    void createCoupon_duplicateCode_throwsBadRequest() {
        CreateCouponRequest request = new CreateCouponRequest();
        request.setCode("DUPLICATE");
        request.setAmount(5.0);

        when(couponRepo.existsByCodeIgnoreCase("DUPLICATE")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(couponRepo, never()).save(any());
    }

    @Test
    void createCoupon_blankCode_throwsBadRequest() {
        CreateCouponRequest request = new CreateCouponRequest();
        request.setCode("   ");
        request.setAmount(5.0);

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Coupon code is required");
    }

    @Test
    void findById_notFound_throwsResourceNotFound() {
        when(couponRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Coupon not found");
    }

    @Test
    void validateForOrder_validCoupon_returnsCoupon() {
        Coupon coupon = activeCoupon("SAVE10", 15.0);

        when(couponRepo.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        Coupon result = couponService.validateForOrder("save10", 100.0);

        assertThat(result).isEqualTo(coupon);
    }

    @Test
    void validateForOrder_unknownCode_throwsBadRequest() {
        when(couponRepo.findByCodeIgnoreCase("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.validateForOrder("missing", 50.0))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid coupon code");
    }

    @Test
    void validateForOrder_inactiveCoupon_throwsBadRequest() {
        Coupon coupon = activeCoupon("OFF", 10.0);
        coupon.setActive(false);

        when(couponRepo.findByCodeIgnoreCase("OFF")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateForOrder("off", 50.0))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no longer active");
    }

    @Test
    void validateForOrder_expiredCoupon_throwsBadRequest() {
        Coupon coupon = activeCoupon("OLD", 10.0);
        coupon.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(couponRepo.findByCodeIgnoreCase("OLD")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateForOrder("old", 50.0))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("has expired");
    }

    @Test
    void validateForOrder_zeroSubtotal_throwsBadRequest() {
        Coupon coupon = activeCoupon("SAVE10", 10.0);
        when(couponRepo.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.validateForOrder("save10", 0))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Order subtotal must be greater than 0");
    }

    @Test
    void calculateDiscount_capsAtSubtotal() {
        Coupon coupon = activeCoupon("BIG", 50.0);

        assertThat(couponService.calculateDiscount(coupon, 30.0)).isEqualTo(30.0);
    }

    @Test
    void calculateDiscount_appliesFullAmountWhenSubtotalIsLarger() {
        Coupon coupon = activeCoupon("SMALL", 10.0);

        assertThat(couponService.calculateDiscount(coupon, 100.0)).isEqualTo(10.0);
    }

    @Test
    void updateCoupon_updatesOnlyProvidedFields() {
        Coupon existing = activeCoupon("KEEP", 10.0);
        existing.setId(1L);

        when(couponRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(couponRepo.save(existing)).thenReturn(existing);

        UpdateCouponRequest request = new UpdateCouponRequest();
        request.setAmount(25.0);

        Coupon updated = couponService.updateCoupon(1L, request);

        assertThat(updated.getAmount()).isEqualTo(25.0);
        assertThat(updated.getCode()).isEqualTo("KEEP");
        assertThat(updated.isActive()).isTrue();
    }

    @Test
    void deleteCoupon_removesExistingCoupon() {
        Coupon coupon = activeCoupon("DELETE", 5.0);
        coupon.setId(3L);

        when(couponRepo.findById(3L)).thenReturn(Optional.of(coupon));

        couponService.deleteCoupon(3L);

        verify(couponRepo).delete(coupon);
    }

    private Coupon activeCoupon(String code, double amount) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setAmount(amount);
        coupon.setActive(true);
        return coupon;
    }
}
