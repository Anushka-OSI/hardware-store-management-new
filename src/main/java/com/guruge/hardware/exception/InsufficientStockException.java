package com.guruge.hardware.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends BusinessException {

    private final String productName;
    private final int available;
    private final int requested;

    public InsufficientStockException(String productName, int available, int requested) {
        super(String.format("Insufficient stock for '%s': available=%d, requested=%d",
                productName, available, requested));
        this.productName = productName;
        this.available = available;
        this.requested = requested;
    }
}
