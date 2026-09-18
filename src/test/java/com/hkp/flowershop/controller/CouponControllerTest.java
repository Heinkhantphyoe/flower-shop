package com.hkp.flowershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkp.flowershop.dto.requests.ApplyCouponRequest;
import com.hkp.flowershop.mapper.CouponMapper;
import com.hkp.flowershop.model.Coupon;
import com.hkp.flowershop.service.CouponService;
import com.hkp.flowershop.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CouponController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(CouponMapper.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponService couponService;

    @MockBean
    private JWTService jwtService;

    @Test
    void applyCoupon_validRequest_returnsDiscount() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setCode("SAVE10");
        coupon.setAmount(10.0);
        coupon.setActive(true);

        when(couponService.validateForOrder(eq("SAVE10"), eq(100.0))).thenReturn(coupon);
        when(couponService.calculateDiscount(coupon, 100.0)).thenReturn(10.0);

        ApplyCouponRequest request = new ApplyCouponRequest();
        request.setCode("SAVE10");
        request.setSubtotal(100.0);

        mockMvc.perform(post("/coupons/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("SAVE10"))
                .andExpect(jsonPath("$.data.discountAmount").value(10.0))
                .andExpect(jsonPath("$.message").value("Coupon applied"));
    }

    @Test
    void applyCoupon_invalidCoupon_returnsBadRequest() throws Exception {
        when(couponService.validateForOrder(any(), any(Double.class)))
                .thenThrow(new com.hkp.flowershop.exceptions.BadRequestException("Invalid coupon code"));

        ApplyCouponRequest request = new ApplyCouponRequest();
        request.setCode("BAD");
        request.setSubtotal(50.0);

        mockMvc.perform(post("/coupons/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid coupon code"));
    }
}
