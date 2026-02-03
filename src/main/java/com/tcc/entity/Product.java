package com.tcc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Product {
    @Id
    @GeneratedValue
    private Integer productCode;
    private String name;
    private int scale;
    private String PdtDescription;
    private int QntyInStock;
    private int BuyPrice;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "product_line_id")
    private ProductLine productLine;

    @OneToMany(mappedBy = "product")
    @JsonManagedReference
    private List<OrderProduct> orderProductList;

    public Product() {
    }

    public Product(Integer productCode, String name, int scale, String pdtDescription, int qntyInStock, int buyPrice, ProductLine productLine, List<OrderProduct> orderProductList) {
        this.productCode = productCode;
        this.name = name;
        this.scale = scale;
        PdtDescription = pdtDescription;
        QntyInStock = qntyInStock;
        BuyPrice = buyPrice;
        this.productLine = productLine;
        this.orderProductList = orderProductList;
    }


    public ProductLine getProductLine() {
        return productLine;
    }

    public void setProductLine(ProductLine productLine) {
        this.productLine = productLine;
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

    public List<OrderProduct> getOrderProductList() {
        return orderProductList;
    }

    public void setOrderProductList(List<OrderProduct> orderProductList) {
        this.orderProductList = orderProductList;
    }
}
