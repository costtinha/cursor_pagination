package com.tcc.controller;

import com.tcc.components.CursorCodec;
import com.tcc.dtos.OrderDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.OrderCursor;
import com.tcc.entity.Order;
import com.tcc.pagination.PageDirection;
import com.tcc.service.orderService.OrderService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/api")
public class OrderController extends RateLimitedController{
    private final OrderService service;
    private final CursorCodec cursorCodec;

    public OrderController(OrderService service, CursorCodec cursorCodec) {
        this.service = service;
        this.cursorCodec = cursorCodec;
    }

    @GetMapping("/orders")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<List<OrderDto>> getOrders(){
        return ResponseEntity.ok(service.getAllOrders());
    }

    @GetMapping("/orders/{id}")
    @RateLimiter(name = "readLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<OrderDto> findOrderById(@PathVariable("id")int id){
        return ResponseEntity.ok(service.findOrderById(id));
    }

    @PostMapping("/orders")
    @RateLimiter(name = "writeLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<OrderDto> saveOrder(@Valid @RequestBody OrderDto dto){
        OrderDto body = service.saveOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
    @PutMapping("/orders/{id}")
    @RateLimiter(name = "writeLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable("id") int id,@Valid @RequestBody OrderDto dto){
        return ResponseEntity.ok(service.updateOrder(dto,id));
    }

    @DeleteMapping("/orders/{id}")
    @RateLimiter(name = "writeLimiter",fallbackMethod = "voidFallBack")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") int id){
        service.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders/keyset")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<CursorPageResponse<OrderDto>> findOrdersKeySet(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "NEXT")PageDirection direction
            ){
        Integer lastId = null;
        if (cursor != null){
            OrderCursor lastOrder = cursorCodec.decode(cursor,OrderCursor.class);
            lastId = lastOrder.lastId();
        }
        return ResponseEntity.ok(service.findOrdersKeySet(lastId,size,direction));
    }

}
