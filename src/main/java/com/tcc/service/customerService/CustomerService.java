package com.tcc.service.customerService;

import com.tcc.cache.CustomerCacheRepository;
import com.tcc.components.CursorCodec;
import com.tcc.dtos.CustomerDto;
import com.tcc.dtos.CustomerResponseDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.CustomerCursor;
import com.tcc.entity.Customer;
import com.tcc.entity.CustomerCache;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.pagination.PageDirection;
import com.tcc.persistance.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final CustomerCacheRepository cacheRepository;
    private final CursorCodec cursorCodec;

    public CustomerService(CustomerRepository repository, CustomerMapper mapper, CustomerCacheRepository cacheRepository, CursorCodec cursorCodec) {
        this.repository = repository;
        this.mapper = mapper;
        this.cacheRepository = cacheRepository;
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

    public List<CustomerResponseDto> findAllCustomers() {
        return repository.findAll()
                .stream()
                .peek(customer -> cacheRepository.save(mapper.customerToCache(customer)))
                .map(mapper::customerToResponseDto)
                .collect(Collectors.toList());
    }

    public CustomerResponseDto findCustomersById(int id) {
        return cacheRepository.findById(id)
                .map(mapper::cacheToCustomerResponseDto)
                .orElseGet(() -> {
                    Customer customer = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer",id));
                    cacheRepository.save(mapper.customerToCache(customer));
                    return mapper.customerToResponseDto(customer);
                });

    }

    public CustomerResponseDto saveCustomer(CustomerDto dto) {
        Customer customer = repository.save(mapper.customerDtoToCustomer(dto));
        CustomerCache cache = cacheRepository.save(mapper.customerToCache(customer));
        return mapper.customerToResponseDto(customer);
    }

    public CustomerResponseDto updateCustomer(CustomerDto dto, int id) {
        Customer oldCustomer = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer",id));
        oldCustomer.setName(dto.name());
        oldCustomer.setCountry(dto.country());
        oldCustomer.setPhone(dto.phone());
        oldCustomer.setState(dto.state());
        oldCustomer.setPostalCode(dto.postalCode());
        return mapper.customerToResponseDto(repository.save(oldCustomer));

    }

    public void deleteCustomer(int id) {
        if(repository.existsById(id)){
            repository.deleteById(id);

        }
        cacheRepository.deleteById(id);

    }

    public CursorPageResponse<CustomerResponseDto> findAllCustomersKeyset(Integer lastId, int size, PageDirection direction) {
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize + 1);
        List<Customer> customers;
        if (lastId == null){
            customers = repository.findAll(pageable).getContent();
        }
        else if(direction == PageDirection.NEXT){
            customers = repository.findByCustomerIdGreaterThanOrderByCustomerIdAsc(lastId,pageable);
        }
        else {
            customers = repository.findByCustomerIdLessThanOrderByCustomerIdDesc(lastId,pageable);
            Collections.reverse(customers);
        }
        boolean hasNext = customers.size() > pageSize;
        boolean hasPrev;
        if (direction == PageDirection.NEXT){
            hasPrev = lastId != null;
        }
        else{
            hasPrev = !customers.isEmpty();
        }
        if (hasNext){
            customers = customers.subList(0,pageSize);
        }
        String nextCursor = null;
        String prevCursor = null;
        if (!customers.isEmpty()){
            Customer customer = customers.getLast();
            nextCursor = cursorCodec.encode(new CustomerCursor(customer.getCustomerId()));
            Customer firstCustomer = customers.getFirst();
            prevCursor = cursorCodec.encode(firstCustomer.getCustomerId());
        }

        if(lastId == null){
            hasPrev = false;
            prevCursor = null;
        }


        List<CustomerResponseDto> dtos = customers.stream()
                .peek(customer -> { cacheRepository.save(mapper.customerToCache(customer));})
                .map(mapper::customerToResponseDto)
                .toList();

        return new CursorPageResponse<>(dtos,nextCursor,prevCursor,hasNext,hasPrev);
    }
}
