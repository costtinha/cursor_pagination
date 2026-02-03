package com.tcc.controller;

import com.tcc.components.CursorCodec;
import com.tcc.dtos.OrderProductDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.OrderProductCursor;
import com.tcc.pagination.PageDirection;
import com.tcc.service.orderProductService.OrderProductService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/api/order_product")
public class OrderProductController extends RateLimitedController {
    private final OrderProductService service;
    private final CursorCodec cursorCodec;

    public OrderProductController(OrderProductService service, CursorCodec cursorCodec) {
        this.service = service;
        this.cursorCodec = cursorCodec;
    }

    @GetMapping
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<List<OrderProductDto>> findOrderProduct(){
        return ResponseEntity.ok(service.findOrderProduct());
    }

    @GetMapping("/{orderId}/{productId}")
    @RateLimiter(name = "readLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<OrderProductDto> findOpById(@PathVariable("orderId")int orderId,@PathVariable("productId")int productId){
        return ResponseEntity.ok(service.findOrderProductById(orderId,productId));
    }

    @PostMapping
    @RateLimiter(name = "writeLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<OrderProductDto> saveOrderProduct(@Valid @RequestBody OrderProductDto dto){
        OrderProductDto body = service.saveOp(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{orderId}/{productId}")
    @RateLimiter(name = "writeLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<OrderProductDto> updateOp(@PathVariable("orderId")int orderId,@PathVariable("productId")int productId,
                                    @Valid @RequestBody OrderProductDto dto){
        return ResponseEntity.ok(service.updateOp(orderId,productId,dto));
    }

    @DeleteMapping("/{orderId}/{productId}")
    @RateLimiter(name = "writeLimiter", fallbackMethod = "voidFallBack")
    public ResponseEntity<Void> deleteOp(@PathVariable("orderId")int orderId,@PathVariable("productId")int productId){
         service.deleteOpById(orderId,productId);
         return ResponseEntity.noContent().build();
    }

    @GetMapping("/keyset")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<CursorPageResponse<OrderProductDto>> findOrderProductsKeySet(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "NEXT")PageDirection direction
            ){
        Integer lastOrderId = null;
        Integer lastProductId = null;
        if (cursor != null){
            OrderProductCursor dto = cursorCodec.decode(cursor,OrderProductCursor.class);
            lastProductId = dto.lastProductId();
            lastOrderId = dto.lastOrderId();
        }
        return ResponseEntity.ok(service.findOrderProductsKeyset(lastOrderId,lastProductId,size, direction));
    }

}
