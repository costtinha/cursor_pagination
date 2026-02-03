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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductLineService{
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
        return repository.findAll()
                .stream()
                .peek(productLine -> cacheRepository.save(mapper.productLineToCache(productLine)))
                .map(mapper::productLineToDto)
                .collect(Collectors.toList());
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
        return mapper.cacheToProductLineDto(
                cacheRepository.save(
                        mapper.productLineToCache(
                                repository.save(
                                        mapper.dtoToProductLine(dto)))));
    }

    public ProductLineDto updateProductLine(int id, ProductLineDto dto) {
        ProductLine oldPl = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ProductLine",id));
        oldPl.setDescInText(dto.descInText());
        oldPl.setImage(dto.image());
        oldPl.setDescInHtml(dto.descInHtml());
        cacheRepository.save(mapper.productLineToCache(oldPl));
        return mapper.productLineToDto(repository.save(oldPl));
    }

    public void deleteProductLineById(int id) {
        if(repository.existsById(id)){
            repository.deleteById(id);
        }
        cacheRepository.deleteById(id);
    }

    public CursorPageResponse<ProductLineDto> findAllProductLineKeyset(Integer lastId, int size, PageDirection direction) {
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize +1);
        List<ProductLine> productLines;
        if (lastId == null){
            productLines = repository.findAll(pageable).getContent();
        }
        else if (direction == PageDirection.NEXT) {
            productLines = repository.findByIdGreaterThanOrderByIdAsc(lastId, pageable);
        }
        else{
            productLines = repository.findByIdLessThanOrderByIdDesc(lastId, pageable);
            Collections.reverse(productLines);
        }
        boolean hasNext = productLines.size() > pageSize;
        boolean hasPrevious;
        if (direction == PageDirection.NEXT){
            hasPrevious = lastId != null;
        }else{
            hasPrevious = !productLines.isEmpty();

        }

        if (hasNext){
            productLines = productLines.subList(0,pageSize);
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


        List<ProductLineDto> dtos = productLines.stream()
                                    .peek(productLine -> {cacheRepository.save(mapper.productLineToCache(productLine));})
                                    .map(mapper::productLineToDto)
                                    .toList();
        return new CursorPageResponse<>(dtos,nextCursor,prevCursor,hasNext,hasPrevious);
    }
}
