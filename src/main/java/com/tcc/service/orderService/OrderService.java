package com.tcc.service.orderService;

import com.tcc.cache.OrderCacheRepository;
import com.tcc.components.CursorCodec;
import com.tcc.dtos.OrderDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.OrderCursor;
import com.tcc.entity.Customer;
import com.tcc.entity.Order;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.pagination.PageDirection;
import com.tcc.persistance.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderMapper mapper;
    private final OrderRepository repository;
    private final OrderCacheRepository cacheRepository;
    private final CursorCodec cursorCodec;

    public OrderService(OrderMapper mapper, OrderRepository repository, OrderCacheRepository cacheRepository, CursorCodec cursorCodec) {
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

    public List<OrderDto> getAllOrders() {
        return repository.findAll()
                .stream()
                .peek(order -> { cacheRepository.save(mapper.orderToCache(order));})
                .map(mapper::orderToOrderDto)
                .collect(Collectors.toList());
    }
    public OrderDto findOrderById(int id){
        return cacheRepository.findById(id)
                .map(mapper::cacheToOrderDto)
                .orElseGet(() ->{
                    Order order = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order",id));
                    cacheRepository.save(mapper.orderToCache(order));
                    return mapper.orderToOrderDto(order);
                });

    }

    public OrderDto saveOrder(OrderDto dto) {
        Order order = repository.save(mapper.dtoToOrder(dto));
        cacheRepository.save(mapper.orderToCache(order));
        return dto;
    }

    public OrderDto updateOrder(OrderDto dto, int id) {
        Order oldOrder = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order",id));
        oldOrder.setOrderDate(dto.orderDate());
        oldOrder.setRequiredDate(dto.requiredDate());
        oldOrder.setComments(dto.commments());
        Customer customer = new Customer();
        customer.setCustomerId(dto.customerId());
        oldOrder.setCustomerId(customer);
        repository.save(oldOrder);
        return mapper.orderToOrderDto(oldOrder);
    }

    public void deleteOrderById(int id) {
        if(repository.existsById(id)){
            repository.deleteById(id);

        }
        cacheRepository.deleteById(id);
    }

    public CursorPageResponse<OrderDto> findOrdersKeySet(Integer lastId, int size, PageDirection direction) {
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize + 1);
        List<Order> orders;
        if(lastId == null){
            orders = repository.findAll(pageable).getContent();
        } else if (direction == PageDirection.NEXT) {
            orders = repository.findByIdGreaterThanOrderByIdAsc(lastId,pageable);
        }
        else {
            orders = repository.findByIdLessThanOrderByIdDesc(lastId,pageable);
            Collections.reverse(orders);
        }

        boolean hasNext = orders.size() > pageSize;
        boolean hasPrev;
        if (direction == PageDirection.NEXT){
            hasPrev = lastId != null;
        }
        else {
            hasPrev = !orders.isEmpty();
        }
        if (hasNext){
            orders = orders.subList(0,pageSize);
        }
        String nextCursor = null;
        String prevCursor = null;
        if (!orders.isEmpty()){
            Order lastOrder = orders.getLast();
            nextCursor = cursorCodec.encode(new OrderCursor(lastOrder.getOrderId()));
            Order firstOrder = orders.getFirst();
            prevCursor = cursorCodec.encode(new OrderCursor(firstOrder.getOrderId()));
        }

        if (lastId == null){
            hasPrev = false;
            prevCursor = null;
        }


        List<OrderDto> dtos = orders.stream()
                .peek(order -> { cacheRepository.save(mapper.orderToCache(order));})
                .map(mapper::orderToOrderDto)
                .toList();

        return new CursorPageResponse<>(dtos,nextCursor,prevCursor,hasNext,hasPrev);
    }
}
