package com.tcc.controller;

import com.tcc.components.CursorCodec;
import com.tcc.dtos.ProductLineDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.ProductLineCursor;
import com.tcc.pagination.PageDirection;
import com.tcc.service.productLineService.ProductLineService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/api/productLine")
public class ProductLineController extends RateLimitedController {
    private final ProductLineService service;
    private final CursorCodec cursorCodec;
    private static final Logger log = LoggerFactory.getLogger(ProductLineController.class);

    public ProductLineController(ProductLineService service, CursorCodec cursorCodec) {
        this.service = service;
        this.cursorCodec = cursorCodec;
    }

    @GetMapping
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<List<ProductLineDto>> findProductLine(){
        return ResponseEntity.ok(service.findAllProductLine());
    }

    @GetMapping("/{id}")
    @RateLimiter(name = "readLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<ProductLineDto> findProductLineById(@PathVariable("id")int id){
        return ResponseEntity.ok(service.findProductLineById(id));
    }

    @PostMapping
    @RateLimiter(name = "writeLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<ProductLineDto> saveProductLine(@Valid @RequestBody ProductLineDto dto){
        try {
            ProductLineDto body = service.saveProductLine(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (Exception e) {
            log.error("saveProductLine failed", e); // this will show the real cause
            throw e;
        }
    }

    @PutMapping("/{id}")
    @RateLimiter(name = "writeLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<ProductLineDto> updateProductLine(@PathVariable("id")int id, @Valid @RequestBody ProductLineDto dto){
        return ResponseEntity.ok(service.updateProductLine(id,dto));
    }

    @DeleteMapping("/{id}")
    @RateLimiter(name = "writeLimiter", fallbackMethod = "voidFallBack")
    public ResponseEntity<Void> deleteProductLine(@PathVariable("id")int id){
        service.deleteProductLineById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/keyset")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<CursorPageResponse<ProductLineDto>> findAllProductLineKeyset(
            @RequestParam(required = false)String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "NEXT")PageDirection direction){
        Integer lastId = null;
        if (cursor != null){
            ProductLineCursor productLineCursor = cursorCodec.decode(cursor,ProductLineCursor.class);
            lastId = productLineCursor.lastId();
        }
        return ResponseEntity.ok(service.findAllProductLineKeyset(lastId,size,direction));
    }
}
