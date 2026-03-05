package com.tcc.service.productLineService;

import com.tcc.cache.ProductLineCacheRepository;
import com.tcc.components.CursorCodec;
import com.tcc.dtos.ProductLineDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.ProductLineCursor;
import com.tcc.entity.ProductLine;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.pagination.PageDirection;
import com.tcc.persistance.ProductLineRepository;
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
public class ProductLineService{
    private static final Logger log = LoggerFactory.getLogger(ProductLineService.class);
    private final ProductLineRepository repository;
    private final ProductLineCacheRepository cacheRepository;
    private final ProductLineMapper mapper;
    private final CursorCodec cursorCodec;

    public ProductLineService(ProductLineRepository repository, ProductLineCacheRepository cacheRepository, ProductLineMapper mapper, CursorCodec cursorCodec) {
        this.repository = repository;
        this.cacheRepository = cacheRepository;
        this.mapper = mapper;
        this.cursorCodec = cursorCodec;
    }

    private static final int maxSize = 50;
    private static final int defaultSize = 20;

    private int normalizeSize(int size){
        if (size <= 0){
            size = defaultSize;
        }
        return Math.min(size,maxSize);
    }


    public List<ProductLineDto> findAllProductLine() {
        List<ProductLine> all = repository.findAll();
        List<ProductLineDto> dto = new ArrayList<>();
        for (ProductLine pl : all){
            try{
                cacheRepository.save(mapper.productLineToCache(pl));
            } catch (Exception e) {
                log.warn("Redis unavailable at the moment, skipping cache for id={}", pl.getProductLineId());
            }
            dto.add(mapper.productLineToDto(pl));
        }
        return dto;
    }

    public ProductLineDto findProductLineById(int id) {
        return cacheRepository.findById(id)
                .map(mapper::cacheToProductLineDto)
                .orElseGet(() -> {
                    ProductLine pl = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ProductLine",id));
                    cacheRepository.save(mapper.productLineToCache(pl));
                    return mapper.productLineToDto(pl);
                });
    }

    public ProductLineDto saveProductLine(ProductLineDto dto) {
        ProductLine saved = repository.save(mapper.dtoToProductLine(dto));
        try {
            cacheRepository.save(mapper.productLineToCache(saved));
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache for productLine at id={}",saved.getProductLineId());
        }
        return mapper.productLineToDto(saved);
    }

    public ProductLineDto updateProductLine(int id, ProductLineDto dto) {
        ProductLine oldPl = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ProductLine",id));
        oldPl.setDescInText(dto.descInText());
        oldPl.setImage(dto.image());
        oldPl.setDescInHtml(dto.descInHtml());
        oldPl = repository.save(oldPl);
        try {
            cacheRepository.save(mapper.productLineToCache(oldPl));
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache for productLine at id={}",oldPl.getProductLineId());
        }

        return mapper.productLineToDto(oldPl);
    }

    public void deleteProductLineById(int id) {
        if(!repository.existsById(id)){
            throw new ResourceNotFoundException("Productline",id);
        }
        repository.deleteById(id);
        try {
            cacheRepository.deleteById(id);
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping cache deletion for ProductLine id={}",id);
        }

    }

    public CursorPageResponse<ProductLineDto> findAllProductLineKeyset(Integer lastId, int size, PageDirection direction) {
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize +1);
        List<ProductLine> productLines;
        if (lastId == null){
            productLines = repository.findAll(pageable).getContent();
        }
        else if (direction == PageDirection.NEXT) {
            productLines = repository.findByProductLineIdGreaterThanOrderByProductLineIdAsc(lastId, pageable);
        }
        else{
            productLines = repository.findByProductLineIdLessThanOrderByProductLineIdDesc(lastId, pageable);
            Collections.reverse(productLines);
        }
        boolean hasNext;
        boolean hasPrevious;
        if (direction == PageDirection.NEXT){
            hasNext = productLines.size() > pageSize;
            hasPrevious = lastId != null;
            if(hasNext){
                productLines = productLines.subList(0,pageSize);
            }
        }else{
            hasNext = lastId != null;
            hasPrevious = productLines.size() > pageSize;
            if(hasPrevious){
                productLines = productLines.subList(1,productLines.size());
            }

        }

        String nextCursor = null;
        String prevCursor= null;
        if (!productLines.isEmpty()){
            ProductLine productLine = productLines.getLast();
            nextCursor = cursorCodec.encode(new ProductLineCursor(productLine.getProductLineId()));

            ProductLine firstPl = productLines.getFirst();
            prevCursor = cursorCodec.encode(new ProductLineCursor(firstPl.getProductLineId()));
        }
        if (lastId == null){
            hasPrevious = false;
            prevCursor = null;
        }
        if (!hasPrevious){
            prevCursor= null;
        }
        if(!hasNext){
            nextCursor = null;
        }


        List<ProductLineDto> dtos = new ArrayList<>();
        for (ProductLine pl : productLines){
            try {
                cacheRepository.save(mapper.productLineToCache(pl));
            } catch (Exception e) {
                log.warn("Redis unavailable at time,skipping cache PL id={}",pl.getProductLineId());
            }
            dtos.add(mapper.productLineToDto(pl));
        }
        return new CursorPageResponse<>(dtos,nextCursor,prevCursor,hasNext,hasPrevious);
    }
}
