package com.tcc.service.employeeService;

import com.tcc.cache.EmployeeCacheRepository;
import com.tcc.components.CursorCodec;
import com.tcc.dtos.EmployeeDto;
import com.tcc.dtos.EmployeeResponseDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.EmployeeCursor;
import com.tcc.entity.Employee;
import com.tcc.entity.Office;
import com.tcc.exception.ConflictException;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.pagination.PageDirection;
import com.tcc.persistance.EmployeeRepository;
import jakarta.transaction.Transactional;
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
@Transactional
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeMapper mapper;
    private final EmployeeRepository repository;
    private final EmployeeCacheRepository cacheRepository;
    private final CursorCodec cursorCodec;

    public EmployeeService(EmployeeMapper mapper, EmployeeRepository repository, EmployeeCacheRepository cacheRepository, CursorCodec cursorCodec) {
        this.mapper = mapper;
        this.repository = repository;
        this.cacheRepository = cacheRepository;
        this.cursorCodec = cursorCodec;
    }

    private static final int maxSize = 50;
    private static final int defaultSize = 20;

    private int normalizeSize(int size){
        if (size <= 0 ){
            size = defaultSize;
        }
        return Math.min(size,maxSize);
    }

    public List<EmployeeResponseDto> findAllEmployees() {
        List<Employee> all = repository.findAll();
        List<EmployeeResponseDto> dto = new ArrayList<>();
        for(Employee emp : all){
            try {
                cacheRepository.save(mapper.employeeToCache(emp));
            }catch (Exception e){
                log.warn("Redis unavailable at time, skipping cache for Employee id={}",emp.getEmployeeId());
            }
            dto.add(mapper.employeeToResponseDto(emp));
        }
        return dto;
    }

    public EmployeeResponseDto findEmployeeById(int id) {
      return cacheRepository.findById(id)
              .map(mapper:: cacheToEmployeeResponseDto)
              .orElseGet(() -> {
                  Employee employee = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee",id));
                  cacheRepository.save(mapper.employeeToCache(employee));
                  return mapper.employeeToResponseDto(employee);

              });

    }

    public EmployeeResponseDto saveEmployee(EmployeeDto dto) {
        if(repository.existsByEmail(dto.email())){
            throw new ConflictException("There is already an employee with the email: " + dto.email());
        }
        Employee employee = mapper.dtoToEmployee(dto);
        repository.save(employee);
        cacheRepository.save(mapper.employeeToCache(employee));
        return mapper.employeeToResponseDto(employee);
    }

    public EmployeeResponseDto updateEmployee(EmployeeDto dto, int id) {
        Employee oldEmployee = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee",id));
        if(!oldEmployee.getEmail().equals(dto.email()) && repository.existsByEmail(dto.email())){
            throw new ConflictException("There is already an employee with the " + dto.email() + " email");
        }
        oldEmployee.setEmployeeName(dto.employeeName());
        oldEmployee.setPhone(dto.phone());
        oldEmployee.setEmail(dto.email());
        Employee reportsTo = new Employee();
        reportsTo.setEmployeeId(dto.reportsTo());
        Office office = new Office();
        office.setOfficeId(dto.office());
        oldEmployee.setReportsTo(reportsTo);
        oldEmployee.setOffice(office);
        oldEmployee = repository.save(oldEmployee);
        cacheRepository.save(mapper.employeeToCache(oldEmployee));
        return mapper.employeeToResponseDto(oldEmployee);
    }

    public void deleteById(int id) {
        if(!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee",id);

        }
        repository.deleteById(id);
        try {
            cacheRepository.deleteById(id);
        } catch (Exception e) {
            log.warn("Redis unavailable, deletion of Employee id={} skipped",id);
        }
        
    }

    public CursorPageResponse<EmployeeResponseDto> findAllEmployeesKeyset(Integer lastId, int size, PageDirection direction) {
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize +1);
        List<Employee> employees;
        if(lastId == null){
            employees = repository.findAll(pageable).getContent();
        } else if (direction == PageDirection.NEXT) {
            employees = repository.findByEmployeeIdGreaterThanOrderByEmployeeIdAsc(lastId,pageable);
        }
        else {
            employees = repository.findByEmployeeIdLessThanOrderByEmployeeIdDesc(lastId,pageable);
            Collections.reverse(employees);
        }
        boolean hasNext;
        boolean hasPrev;
        if (direction == PageDirection.NEXT){
            hasNext = employees.size() > pageSize;
            hasPrev = lastId != null;
            if(hasNext){
                employees = employees.subList(0,pageSize);
            }
        }
        else {
            hasNext = lastId != null;
            hasPrev = !employees.isEmpty();
            if (hasPrev){
                employees = employees.subList(1,employees.size());
            }
        }

        String nextCursor = null;
        String prevCursor = null;
        if (!employees.isEmpty()){
            Employee lastEmployee = employees.getLast();
            nextCursor = cursorCodec.encode(new EmployeeCursor(lastEmployee.getEmployeeId()));
            Employee firstEmployee = employees.getFirst();
            prevCursor = cursorCodec.encode(new EmployeeCursor(firstEmployee.getEmployeeId()));
        }

        if (lastId == null){
            hasPrev = false;
            prevCursor = null;
        }
        if (!hasPrev){
            prevCursor= null;
        }
        if(!hasNext){
            nextCursor = null;
        }


        List<EmployeeResponseDto> dtos = new ArrayList<>();
        for(Employee emp : employees){
            try {
                cacheRepository.save(mapper.employeeToCache(emp));
            }catch (Exception e){
                log.warn("Redis unavailable at time, skipping cache for Employee id={}",emp.getEmployeeId());
            }
            dtos.add(mapper.employeeToResponseDto(emp));
        }
        return new CursorPageResponse<>(dtos,nextCursor,prevCursor,hasNext,hasPrev);
    }
}
