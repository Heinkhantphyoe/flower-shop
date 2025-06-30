package com.hkp.flowershop.dto.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PaginationResponse<T> {
    private List<T> items;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;

    public PaginationResponse(List<T> items, Page<?> pageData) {
        this.items = items;
        this.currentPage = pageData.getNumber();
        this.totalPages = pageData.getTotalPages();
        this.totalItems = pageData.getTotalElements();
        this.pageSize = pageData.getSize();
    }

}

