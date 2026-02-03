package com.tcc.service.productService;

import com.tcc.dtos.ProductDto;
import com.tcc.dtos.ProductResponseDto;
import com.tcc.entity.Product;
import com.tcc.entity.ProductCache;
import com.tcc.entity.ProductLine;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public ProductCache productToCache(Product product) {
        ProductCache cache = new ProductCache();
        cache.setProductCode(product.getProductCode());
        cache.setProductLine(product.getProductLine().getProductLineId());
        cache.setName(product.getName());
        cache.setScale(product.getScale());
        cache.setBuyPrice(product.getBuyPrice());
        cache.setPdtDescription(product.getPdtDescription());
        cache.setQntyInStock(product.getQntyInStock());
        cache.setOrderProductList(product.getOrderProductList() != null ?
                product.getOrderProductList()
                        .stream()
                        .map(orderProduct -> {
                            return "orderId:"+ orderProduct.getOrder().getOrderId() + ",productId:"+orderProduct.getProduct().getProductCode();
                        })
                        .collect(Collectors.toList()): Collections.emptyList());
        return cache;
    }

    public ProductResponseDto productToResponseDto(Product product){
        return new ProductResponseDto(product.getName(), product.getScale(), product.getPdtDescription(), product.getBuyPrice());
    }

    public ProductResponseDto productCacheToResponseDto(ProductCache productCache) {
        return new ProductResponseDto(productCache.getName(), productCache.getScale(), productCache.getPdtDescription(), productCache.getBuyPrice());
    }
    public Product dtoToProduct(ProductDto dto){
        Product product = new Product();
        product.setName(dto.name());
        product.setBuyPrice(dto.buyPrice());
        product.setPdtDescription(dto.pdtDescription());
        product.setQntyInStock(dto.qntyInStock());
        product.setScale(dto.scale());
        ProductLine pl = new ProductLine();
        pl.setProductLineId(dto.productLine());
        product.setProductLine(pl);
        product.setOrderProductList(Collections.emptyList());
        return product;
    }
}
