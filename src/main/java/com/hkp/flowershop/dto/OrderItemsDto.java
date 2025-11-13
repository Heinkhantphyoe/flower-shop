package com.hkp.flowershop.dto;

import com.hkp.flowershop.model.Order;
import com.hkp.flowershop.model.Product;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class OrderItemsDto {

    private Long productId;

    private Integer quantity;

    //Price is not require,cause In service,we find with productId,so get price
    //private Double price;

}
