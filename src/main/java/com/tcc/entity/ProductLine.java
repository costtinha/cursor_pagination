package com.tcc.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class ProductLine {
    @Id
    @GeneratedValue
    private Integer productLineId;
    private String descInText;
    private String descInHtml;
    private String image;
    @OneToMany(mappedBy = "productLine")
    @JsonManagedReference
    private List<Product> products;

    public ProductLine() {
    }

    public ProductLine(Integer productLineId, String descInText, String descInHtml, String image, List<Product> products) {
        this.productLineId = productLineId;
        this.descInText = descInText;
        this.descInHtml = descInHtml;
        this.image = image;
        this.products = products;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Integer getProductLineId() {
        return productLineId;
    }

    public void setProductLineId(Integer productLineId) {
        this.productLineId = productLineId;
    }

    public String getDescInText() {
        return descInText;
    }

    public void setDescInText(String descInText) {
        this.descInText = descInText;
    }

    public String getDescInHtml() {
        return descInHtml;
    }

    public void setDescInHtml(String descInHtml) {
        this.descInHtml = descInHtml;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
