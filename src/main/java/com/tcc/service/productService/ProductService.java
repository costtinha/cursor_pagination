package com.tcc.service.productService;

import com.tcc.cache.ProductCacheRepository;
import com.tcc.components.CursorCodec;
import com.tcc.dtos.ProductDto;
import com.tcc.dtos.ProductResponseDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.ProductCursor;
import com.tcc.entity.Product;
import com.tcc.entity.ProductLine;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.pagination.PageDirection;
import com.tcc.persistance.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository repository;
    private final ProductCacheRepository cacheRepository;
    private final ProductMapper mapper;
    private final CursorCodec cursorCodec;

    public ProductService(ProductRepository repository, ProductCacheRepository cacheRepository, ProductMapper mapper, CursorCodec cursorCodec) {
        this.repository = repository;
        this.cacheRepository = cacheRepository;
        this.mapper = mapper;
        this.cursorCodec = cursorCodec;
    }

    private static final int maxSize = 50;
    private static final int defaultSize = 20;
    private int normalizeSize(int size){
        if(size <= 0){
            size = defaultSize;
        }
        return Math.min(size,maxSize);
    }

    public List<ProductResponseDto> findAllProducts() {
        List<Product> all = repository.findAll();
        List<ProductResponseDto> dtos = new ArrayList<>();
        for(Product p : all){
            try {
                cacheRepository.save(mapper.productToCache(p));
            } catch (Exception e) {
                log.warn("Redis unavailable at time, sipping cache for Product id={}",p.getProductCode());
            }
            dtos.add(mapper.productToResponseDto(p));
        }
        return dtos;
    }

    public ProductResponseDto findProductById(int id) {
        return cacheRepository.findById(id)
                .map(mapper::productCacheToResponseDto)
                .orElseGet(() -> {
                    Product product = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product",id));
                    cacheRepository.save(mapper.productToCache(product));
                    return mapper.productToResponseDto(product);
                });
    }

    public ProductResponseDto saveProduct(ProductDto dto) {
        Product product =repository.save(mapper.dtoToProduct(dto));
        cacheRepository.save(mapper.productToCache(product));
        return mapper.productToResponseDto(product);
    }

    public ProductResponseDto updateProduct(int id, ProductDto dto) {
        Product oldProduct = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product",id));
        oldProduct.setScale(dto.scale());
        oldProduct.setName(dto.name());
        oldProduct.setQntyInStock(dto.qntyInStock());
        oldProduct.setPdtDescription(dto.pdtDescription());
        oldProduct.setBuyPrice(dto.buyPrice());
        ProductLine pl = new ProductLine();
        pl.setProductLineId(dto.productLine());
        oldProduct.setProductLine(pl);
        oldProduct = repository.save(oldProduct);
        cacheRepository.save(mapper.productToCache(oldProduct));
        return mapper.productToResponseDto(oldProduct);
    }

    public void deleteProductById(int id) {
        if(!repository.existsById(id)){
            throw new ResourceNotFoundException("Product",id);
        }
        repository.deleteById(id);
        try {
            cacheRepository.deleteById(id);
        }catch (Exception e){
            log.warn("Redis unavailable at the time, deleting for Product id={} skipped",id);
        }

    }

    public CursorPageResponse<ProductResponseDto> findAllProductsKeySet(Integer lastId, int size, PageDirection direction) {
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize + 1);
        List<Product> products;
        if (lastId == null){
            products = repository.findAll(pageable).getContent();
        }
        else if (direction == PageDirection.NEXT) {
            products = repository.findByProductCodeGreaterThanOrderByProductCodeAsc(lastId,pageable);
        }
        else {
            products = repository.findByProductCodeLessThanOrderByProductCodeDesc(lastId,pageable);
            Collections.reverse(products);
        }
        boolean hasNext;
        boolean hasPrev;
        if (direction == PageDirection.NEXT){
            hasNext = products.size() > pageSize;
            hasPrev = lastId != null;
            if (hasNext){
                products = products.subList(0,pageSize);
            }
        }else {
            hasNext = lastId != null;
            hasPrev = products.size() > pageSize;
            if (hasPrev){
                products = products.subList(1,products.size());
            }
        }


        String nextCursor = null;
        String prevCursor = null;
        if (!products.isEmpty()){
            Product lastProduct = products.getLast();
            nextCursor = cursorCodec.encode(new ProductCursor(lastProduct.getProductCode()));

            Product firstProduct = products.getFirst();
            prevCursor = cursorCodec.encode(new ProductCursor(firstProduct.getProductCode()));
        }
        if (lastId == null){
            hasPrev = false;
            prevCursor = null;
        }
        if (!hasNext){
            nextCursor = null;
        }
        if (!hasPrev){
            prevCursor = null;
        }


        List<ProductResponseDto> dtos = products
                                        .stream()
                                        .peek(product -> { cacheRepository.save(mapper.productToCache(product));})
                                        .map(mapper::productToResponseDto)
                                        .toList();

        return new CursorPageResponse<>(dtos,nextCursor,prevCursor,hasNext,hasPrev);
    }
}
