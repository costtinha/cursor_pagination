package com.tcc.controller;


import com.tcc.components.CursorCodec;
import com.tcc.dtos.CustomerDto;
import com.tcc.dtos.CustomerResponseDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.CustomerCursor;
import com.tcc.entity.Customer;
import com.tcc.pagination.PageDirection;
import com.tcc.service.customerService.CustomerService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/api")
public class CustomerController extends RateLimitedController {
    private final CustomerService service;
    private final CursorCodec cursorCodec;

    public CustomerController(CustomerService service, CursorCodec cursorCodec) {
        this.service = service;
        this.cursorCodec = cursorCodec;
    }

    @GetMapping("/customers")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<List<CustomerResponseDto>> findCustomers(){
         return ResponseEntity.ok(service.findAllCustomers());
    }
    @GetMapping("/customers/{id}")
    @RateLimiter(name = "readLimiter",fallbackMethod = "itemFallBack")
    public ResponseEntity<CustomerResponseDto> findCustomerById(@PathVariable("id") int id){
        return ResponseEntity.ok(service.findCustomersById(id));
    }

    @PostMapping("/customers")
    @RateLimiter(name = "writeLimiter",fallbackMethod = "itemFallBack")
    public ResponseEntity<CustomerResponseDto> saveCustomer(@Valid @RequestBody CustomerDto dto){
        CustomerResponseDto body = service.saveCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/customer/{id}")
    @RateLimiter(name = "writeLimiter",fallbackMethod = "itemFallBack")
    public ResponseEntity<CustomerResponseDto> updateCustomer(@Valid @RequestBody CustomerDto dto, @PathVariable("id") int id){
        return ResponseEntity.ok(service.updateCustomer(dto,id));
    }

    @DeleteMapping("/customer/{id}")
    @RateLimiter(name = "writeLimiter",fallbackMethod = "voidFallBack")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id")int id){
        service.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customers/keyset")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<CursorPageResponse<CustomerResponseDto>> findAllCustomersKeySet(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "NEXT")PageDirection direction
            ){
        Integer lastId = null;
        if (cursor != null){
            CustomerCursor lastCustomer = cursorCodec.decode(cursor,CustomerCursor.class);
            lastId = lastCustomer.lastId();
        }
        return ResponseEntity.ok(service.findAllCustomersKeyset(lastId,size,direction));
    }


}
