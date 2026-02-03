package com.tcc.controller;

import com.tcc.components.CursorCodec;
import com.tcc.dtos.EmployeeDto;
import com.tcc.dtos.EmployeeResponseDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.EmployeeCursor;
import com.tcc.pagination.PageDirection;
import com.tcc.service.employeeService.EmployeeService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/api")
public class EmployeeController extends RateLimitedController {
    private final EmployeeService service;
    private final CursorCodec cursorCodec;

    public EmployeeController(EmployeeService service, CursorCodec cursorCodec) {
        this.service = service;
        this.cursorCodec = cursorCodec;
    }

    @GetMapping("/employees")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<List<EmployeeResponseDto>> findEmployees(){
        return ResponseEntity.ok(service.findAllEmployees());
    }

    @GetMapping("/employees/{id}")
    @RateLimiter(name = "readLimiter",fallbackMethod = "itemFallBack")
    public ResponseEntity<EmployeeResponseDto> findEmployeeById(@PathVariable("id") int id){
        return ResponseEntity.ok(service.findEmployeeById(id));
    }


    @PostMapping("/employees")
    @RateLimiter(name = "writeLimiter",fallbackMethod = "itemFallBack")
    public ResponseEntity<EmployeeResponseDto> saveEmployee(@Valid @RequestBody EmployeeDto dto){
        EmployeeResponseDto created= service.saveEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/employees/{id}")
    @RateLimiter(name = "writeLimiter",fallbackMethod = "itemFallBack")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(@Valid @RequestBody EmployeeDto dto, @PathVariable("id") int id){
        return ResponseEntity.ok(service.updateEmployee(dto,id));
    }

    @DeleteMapping("/employees/{id}")
    @RateLimiter(name = "writeBack",fallbackMethod = "voidFallBack")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") int id){
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/keyset")
    @RateLimiter(name = "publicListLimiter", fallbackMethod = "listFallBack")
    public ResponseEntity<CursorPageResponse<EmployeeResponseDto>> findEmployeesKeyset(@RequestParam(required = false) String cursor,
                                                                                       @RequestParam(defaultValue = "20") int size,
                                                                                       @RequestParam(defaultValue = "NEXY")PageDirection direction){
        Integer lastId = null;
                if(cursor != null){
                    EmployeeCursor dto = cursorCodec.decode(cursor,EmployeeCursor.class);
                    lastId = dto.lastId();
                }
        return ResponseEntity.ok(service.findAllEmployeesKeyset(lastId,size,direction));
    }
}
