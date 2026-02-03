package com.tcc.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "order_product",timeToLive = 3600)
public class OrderProductCache {

    @Id
    private String orderProductKey;
    private int orderId;
    private int productId;
    private int qnty;
    private int priceEach;

    public OrderProductCache() {
    }

    public OrderProductCache(int orderId, int productId, int qnty, int priceEach) {
        this.orderId = orderId;
        this.productId = productId;
        this.qnty = qnty;
        this.priceEach = priceEach;
        this.orderProductKey = "orderId: "+orderId+",productId: "+productId;
    }

    public String getOrderProductKey() {
        return orderProductKey;
    }

    public void setOrderProductKey(String orderProductKey) {
        this.orderProductKey = orderProductKey;
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
    public void generateOrderProductKey(){
        this.orderProductKey = "orderId: "+orderId+",productId: "+productId;
    }
}
