package com.tcc.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash(value = "product_cache",timeToLive = 3600)
public class ProductCache {
    @Id
    private Integer productCode;
    private String name;
    private int scale;
    private String PdtDescription;
    private int QntyInStock;
    private int BuyPrice;

    private int productLine;


    private List<String> orderProductList;

    public ProductCache() {
    }

    public ProductCache(Integer productCode, String name, int scale, String pdtDescription, int qntyInStock, int buyPrice, int productLine, List<String> orderProductList) {
        this.productCode = productCode;
        this.name = name;
        this.scale = scale;
        PdtDescription = pdtDescription;
        QntyInStock = qntyInStock;
        BuyPrice = buyPrice;
        this.productLine = productLine;
        this.orderProductList = orderProductList;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getPdtDescription() {
        return PdtDescription;
    }

    public void setPdtDescription(String pdtDescription) {
        PdtDescription = pdtDescription;
    }

    public int getQntyInStock() {
        return QntyInStock;
    }

    public void setQntyInStock(int qntyInStock) {
        QntyInStock = qntyInStock;
    }

    public int getBuyPrice() {
        return BuyPrice;
    }

    public void setBuyPrice(int buyPrice) {
        BuyPrice = buyPrice;
    }

    public int getProductLine() {
        return productLine;
    }

    public void setProductLine(int productLine) {
        this.productLine = productLine;
    }

    public List<String> getOrderProductList() {
        return orderProductList;
    }

    public void setOrderProductList(List<String> orderProductList) {
        this.orderProductList = orderProductList;
    }
}
