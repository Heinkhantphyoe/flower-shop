package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.AdminCustomerDto;
import com.hkp.flowershop.dto.AnalyticsSummaryDto;
import com.hkp.flowershop.dto.requests.PaginationRequest;
import com.hkp.flowershop.dto.response.PaginationResponse;
import com.hkp.flowershop.service.AnalyticsService;
import com.hkp.flowershop.service.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    @Autowired
    private final AnalyticsService analyticsService;

    /**
     * GET /api/admin/analytics/summary?filter=today|week|month|year
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getSummary(@RequestParam(value = "filter", required = false) String filter) {
        try {
            AnalyticsSummaryDto summary = analyticsService.getSummary(filter);
            return ResponseUtil.success(summary);
        } catch (Exception e) {
            log.error("Error while computing analytics summary", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getCustomers(PaginationRequest paginationRequest,
                                          @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            Pageable pageable = paginationRequest.toPageable();
            Page<AdminCustomerDto> pageCustomers = analyticsService.getCustomers(keyword, pageable);
            PaginationResponse<AdminCustomerDto> response =
                    new PaginationResponse<>(pageCustomers.getContent(), pageCustomers);
            return ResponseUtil.success(response);
        } catch (Exception e) {
            log.error("Error while fetching customer data for admin dashboard", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }
}
