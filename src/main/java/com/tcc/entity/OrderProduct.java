package com.tcc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class OrderProduct {
    @EmbeddedId
    private OrderProductKey orderProductKey;

    @MapsId("orderId")
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "order_id")
    private Order order;

    @MapsId("productId")
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "product_id")
    private Product product;

    private int qnty;
    private int priceEach;

    public OrderProduct(OrderProductKey orderProductKey, Order order, Product product, int qnty, int priceEach) {
        this.orderProductKey = orderProductKey;
        this.order = order;
        this.product = product;
        this.qnty = qnty;
        this.priceEach = priceEach;
    }

    public OrderProduct() {
    }

    public OrderProductKey getOrderProductKey() {
        return orderProductKey;
    }

    public void setOrderProductKey(OrderProductKey orderProductKey) {
        this.orderProductKey = orderProductKey;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQnty() {
        return qnty;
    }

    public void setQnty(int qnty) {
        this.qnty = qnty;
    }

    public int getPriceEach() {
        return priceEach;
    }

    public void setPriceEach(int priceEach) {
        this.priceEach = priceEach;
    }
}
