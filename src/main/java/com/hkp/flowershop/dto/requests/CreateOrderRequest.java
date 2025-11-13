package com.hkp.flowershop.dto.requests;

import com.hkp.flowershop.dto.OrderItemsDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Order address must not be blank")
    private String orderAddress;

    @NotBlank(message = "City must not be blank")
    private String city;

    @NotBlank(message = "ZipCode must not be blank")
    private Integer zipCode;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemsDto> orderItems;

    @NotNull(message = "Image is required")
    private MultipartFile paymentSs;

    private String deliveryNotes;

}

