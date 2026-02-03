package com.tcc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


import java.util.List;

@RedisHash(value = "order_table",timeToLive = 3600)
public class OrderCache {
    @Id
    private Integer orderId;
    private int customerId;

    private String orderDate;
    private String requiredDate;
    private String comments;


    private List<String> orderProductList;

    public OrderCache() {
    }

    public OrderCache(Integer orderId, int customerId, String orderDate, String requiredDate, String comments) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.requiredDate = requiredDate;
        this.comments = comments;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(String requiredDate) {
        this.requiredDate = requiredDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<String> getOrderProductList() {
        return orderProductList;
    }

    public void setOrderProductList(List<String> orderProductList) {
        this.orderProductList = orderProductList;
    }
}
