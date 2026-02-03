package com.tcc.service.productLineService;

import com.tcc.dtos.ProductLineDto;
import com.tcc.entity.Product;
import com.tcc.entity.ProductLine;
import com.tcc.entity.ProductLineCache;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class ProductLineMapper {
    public ProductLineCache productLineToCache(ProductLine pl){
        ProductLineCache cache = new ProductLineCache();
        cache.setProductLineId(pl.getProductLineId());
        cache.setDescInHtml(pl.getDescInHtml());
        cache.setImage(pl.getImage());
        cache.setDescInText(pl.getDescInText());
        cache.setProducts(pl.getProducts() != null ?
                pl.getProducts()
                        .stream()
                        .map(Product::getProductCode)
                        .collect(Collectors.toList()) : Collections.emptyList());
        return cache;
    }

    public ProductLineDto productLineToDto(ProductLine pl){
        return new ProductLineDto(pl.getDescInText(), pl.getDescInHtml(), pl.getImage());
    }

    public ProductLineDto cacheToProductLineDto(ProductLineCache cache){
        return new ProductLineDto(cache.getDescInText(), cache.getDescInHtml(), cache.getImage());
    }

    public ProductLine dtoToProductLine(ProductLineDto dto){
        ProductLine pl = new ProductLine();
        pl.setDescInHtml(dto.descInHtml());
        pl.setImage(dto.image());
        pl.setDescInText(dto.descInText());
        pl.setProducts(Collections.emptyList());
        return pl;

    }
}
