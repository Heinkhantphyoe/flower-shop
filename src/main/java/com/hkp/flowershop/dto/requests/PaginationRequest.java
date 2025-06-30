package com.hkp.flowershop.dto.requests;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@Data
public class PaginationRequest {
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String sortOrder = "asc";

    public Pageable toPageable() {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page,size,sort);
    }

}

