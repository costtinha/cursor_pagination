package com.tcc.controller;

import com.tcc.components.CursorCodec;
import com.tcc.dtos.OfficeDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.OfficeCursor;
import com.tcc.pagination.PageDirection;
import com.tcc.service.officeService.OfficeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;


import java.util.List;

@RestController
@RequestMapping("/public/api")
public class OfficeController extends RateLimitedController {
    private final OfficeService service;
    private final CursorCodec cursorCodec;

    public OfficeController(OfficeService service, CursorCodec cursorCodec) {
        this.service = service;
        this.cursorCodec = cursorCodec;
    }


    @GetMapping("/offices")
    //@RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<Page<OfficeDto>> listAllOffices(@PageableDefault(size = 20, sort = "officeId")Pageable pageable){
        return ResponseEntity.ok(service.allOffices(pageable));
    }

    @GetMapping("/offices/{id}")
    @RateLimiter(name = "readLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<OfficeDto> findOfficeById(@PathVariable("id") int id){
        return ResponseEntity.ok(service.findOfficeById(id));
    }

    @PostMapping("/offices")
    @RateLimiter(name = "writeLimiter", fallbackMethod = "itemFallBack")
    public ResponseEntity<OfficeDto> saveOffice(@Valid @RequestBody OfficeDto dto){
        OfficeDto created = service.saveOffice(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/offices/{id}")
    @RateLimiter(name = "writeLimiter",fallbackMethod = "itemFallBack")
    public ResponseEntity<OfficeDto> updateOffice(@Valid @RequestBody OfficeDto dto, @PathVariable("id") int id){
        return ResponseEntity.ok(service.updateOfficeById(dto,id));
    }

    @DeleteMapping("/offices/{id}")
    @RateLimiter(name = "writeLimiter",fallbackMethod = "voidFallBack")
    public ResponseEntity<Void> deleteOfficeById(@PathVariable("id") int id){
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/offices/keyset")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<CursorPageResponse<OfficeDto>> getAllOfficesKeyset(@RequestParam(required = false) String cursor,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(defaultValue = "NEXT")PageDirection direction){
        Integer lastId = null;
        if (cursor != null){
            OfficeCursor decoded = cursorCodec.decode(cursor,OfficeCursor.class);
            lastId = decoded.lastId();
        }
        return ResponseEntity.ok(service.keySetOffices(lastId,size,direction));
    }

}
