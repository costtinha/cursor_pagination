package com.tcc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash(value = "product_line",timeToLive = 3600)
public class ProductLineCache {
    @Id
    private Integer productLineId;
    private String DescInText;
    private String DescInHtml;
    private String image;

    private List<Integer> products;

    public ProductLineCache() {
    }

    public ProductLineCache(Integer productLineId, String descInText, String descInHtml, String image, List<Integer> products) {
        this.productLineId = productLineId;
        DescInText = descInText;
        DescInHtml = descInHtml;
        this.image = image;
        this.products = products;
    }

    public List<Integer> getProducts() {
        return products;
    }

    public void setProducts(List<Integer> products) {
        this.products = products;
    }

    public Integer getProductLineId() {
        return productLineId;
    }

    public void setProductLineId(Integer productLineId) {
        this.productLineId = productLineId;
    }

    public String getDescInText() {
        return DescInText;
    }

    public void setDescInText(String descInText) {
        DescInText = descInText;
    }

    public String getDescInHtml() {
        return DescInHtml;
    }

    public void setDescInHtml(String descInHtml) {
        DescInHtml = descInHtml;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
