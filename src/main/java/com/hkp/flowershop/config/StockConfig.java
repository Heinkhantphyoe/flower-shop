package com.hkp.flowershop.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class StockConfig {

    @Value("${stock.threshold.low}")
    private double lowStockThreshold;
}
