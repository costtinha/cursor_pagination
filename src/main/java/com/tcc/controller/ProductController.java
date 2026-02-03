package com.tcc.controller;

import com.tcc.components.CursorCodec;
import com.tcc.dtos.ProductDto;
import com.tcc.dtos.ProductResponseDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.ProductCursor;
import com.tcc.pagination.PageDirection;
import com.tcc.service.productService.ProductService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/api/products")
public class ProductController extends RateLimitedController {
    private final ProductService service;
    private final CursorCodec cursorCodec;

    public ProductController(ProductService service, CursorCodec cursorCodec) {
        this.service = service;
        this.cursorCodec = cursorCodec;
    }

    @GetMapping
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<List<ProductResponseDto>> findProducts(){
        return ResponseEntity.ok(service.findAllProducts());
    }

    @GetMapping("/{id}")
    @RateLimiter(name = "readLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<ProductResponseDto> findProductById(@PathVariable("id") int id){
        return ResponseEntity.ok(service.findProductById(id));
    }

    @PostMapping
    @RateLimiter(name = "writeLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<ProductResponseDto> saveProduct(@Valid @RequestBody ProductDto dto){
        ProductResponseDto body = service.saveProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }


    @PutMapping("/{id}")
    @RateLimiter(name = "writeLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable("id")int id, @Valid @RequestBody ProductDto dto){
        return ResponseEntity.ok(service.updateProduct(id,dto));
    }

    @DeleteMapping("/{id}")
    @RateLimiter(name = "writeLimiter", fallbackMethod = "voidFallBack")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id")int id){
         service.deleteProductById(id);
         return ResponseEntity.noContent().build();
    }

    @GetMapping("/keyset")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<CursorPageResponse<ProductResponseDto>> findProductsKeyset(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "NEXT")PageDirection direction
            ){
        Integer lastId = null;
        if (cursor != null){
            ProductCursor lastProduct = cursorCodec.decode(cursor,ProductCursor.class);
            lastId = lastProduct.lastCode();
        }
        return ResponseEntity.ok(service.findAllProductsKeySet(lastId,size,direction));

    }
}
