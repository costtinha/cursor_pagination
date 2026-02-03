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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
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
        return repository.findAll()
                .stream()
                .peek(product -> cacheRepository.save(mapper.productToCache(product)))
                .map(mapper::productToResponseDto)
                .collect(Collectors.toList());
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
        Product product =mapper.dtoToProduct(dto);
        cacheRepository.save(mapper.productToCache(repository.save(product)));
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
        if(repository.existsById(id)){
            repository.deleteById(id);
        }
        cacheRepository.deleteById(id);

    }

    public CursorPageResponse<ProductResponseDto> findAllProductsKeySet(Integer lastId, int size, PageDirection direction) {
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize + 1);
        List<Product> products;
        if (lastId == null){
            products = repository.findAll(pageable).getContent();
        }
        else if (direction == PageDirection.NEXT) {
            products = repository.findByIdGreaterThanOrderByIdAsc(lastId,pageable);
        }
        else {
            products = repository.findByIdLessThanOrderByIdDesc(lastId,pageable);
            Collections.reverse(products);
        }
        boolean hasNext = products.size() > pageSize;
        boolean hasPrev;
        if (direction == PageDirection.NEXT){
            hasPrev = lastId != null;
        }else {
            hasPrev = !products.isEmpty();
        }

        if (hasNext){
            products = products.subList(0,pageSize);
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


        List<ProductResponseDto> dtos = products
                                        .stream()
                                        .peek(product -> { cacheRepository.save(mapper.productToCache(product));})
                                        .map(mapper::productToResponseDto)
                                        .toList();

        return new CursorPageResponse<>(dtos,nextCursor,prevCursor,hasNext,hasPrev);
    }
}
