package com.tcc.entity;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class OrderProductKey {
    private int orderId;
    private int productId;

    public OrderProductKey(int orderId, int productId) {
        this.orderId = orderId;
        this.productId = productId;
    }

    public OrderProductKey() {
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderProductKey that = (OrderProductKey) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, productId);
    }
}
