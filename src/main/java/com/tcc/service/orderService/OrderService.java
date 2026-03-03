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
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
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
        List<Order> all = repository.findAll();
        List<OrderDto> dto = new ArrayList<>();
        for(Order o : all){
            try {
                cacheRepository.save(mapper.orderToCache(o));
            } catch (Exception e) {
                log.warn("Redis unavailable at time, skipping cache for Order id={}",o.getOrderId());
            }
            dto.add(mapper.orderToOrderDto(o));
        }
        return dto;
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
        oldOrder = repository.save(oldOrder);
        cacheRepository.save(mapper.orderToCache(oldOrder));
        return mapper.orderToOrderDto(oldOrder);
    }

    public void deleteOrderById(int id) {
        if(!repository.existsById(id)){
            throw new ResourceNotFoundException("Order",id);

        }
        repository.deleteById(id);
        try {
            cacheRepository.deleteById(id);
        } catch (Exception e) {
            log.warn("Redis unavailable at time, deleting for Order id={} skipped",id);
        }
    }

    public CursorPageResponse<OrderDto> findOrdersKeySet(Integer lastId, int size, PageDirection direction) {
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize + 1);
        List<Order> orders;
        if(lastId == null){
            orders = repository.findAll(pageable).getContent();
        } else if (direction == PageDirection.NEXT) {
            orders = repository.findByOrderIdGreaterThanOrderByOrderIdAsc(lastId,pageable);
        }
        else {
            orders = repository.findByOrderIdLessThanOrderByOrderIdDesc(lastId,pageable);
            Collections.reverse(orders);
        }

        boolean hasNext;
        boolean hasPrev;
        if (direction == PageDirection.NEXT){
            hasNext = orders.size() > pageSize;
            hasPrev = lastId != null;
            if (hasNext){
                orders = orders.subList(0,pageSize);
            }
        }
        else {
            hasNext = lastId != null;
            hasPrev = orders.size() > pageSize;
            if (hasPrev){
                orders = orders.subList(1,orders.size());
            }
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

        if (!hasNext){
            nextCursor = null;
        }
        if (!hasPrev){
            prevCursor = null;
        }


        List<OrderDto> dtos = orders.stream()
                .peek(order -> { cacheRepository.save(mapper.orderToCache(order));})
                .map(mapper::orderToOrderDto)
                .toList();

        return new CursorPageResponse<>(dtos,nextCursor,prevCursor,hasNext,hasPrev);
    }
}
