package com.hkp.flowershop.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@Data
public class PaginationRequest {

    @Min(value = 0, message = "Page number must be non-negative.")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1.")
    private int size = 10;

    private String sortBy = "id";

    @Pattern(regexp = "^(asc|desc)$", message = "Sort order must be 'asc' or 'desc'.")
    private String sortOrder = "asc";

    public Pageable toPageable() {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page,size,sort);
    }

}

