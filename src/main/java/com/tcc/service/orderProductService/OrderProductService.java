package com.tcc.service.orderProductService;

import com.tcc.cache.OrderProductCacheRepository;
import com.tcc.components.CursorCodec;
import com.tcc.dtos.OrderProductDto;
import com.tcc.dtos.cursors.CursorPageResponse;
import com.tcc.dtos.cursors.OrderProductCursor;
import com.tcc.entity.*;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.pagination.PageDirection;
import com.tcc.persistance.OrderProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderProductService {
    private final OrderProductRepository repository;
    private final OrderProductCacheRepository cacheRepository;
    private final OrderProductMapper mapper;
    private final CursorCodec cursorCodec;

    public OrderProductService(OrderProductRepository repository, OrderProductCacheRepository cacheRepository, OrderProductMapper mapper, CursorCodec cursorCodec) {
        this.repository = repository;
        this.cacheRepository = cacheRepository;
        this.mapper = mapper;
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

    public List<OrderProductDto> findOrderProduct() {
        return repository.findAll()
                .stream()
                .peek(orderProduct -> cacheRepository.save(mapper.opToCache(orderProduct)))
                .map(mapper::opToDto)
                .collect(Collectors.toList());
    }

    public OrderProductDto findOrderProductById(int orderId,int productId) {
        OrderProductKey key = new OrderProductKey(orderId,productId);
        String stringKey = "orderId: "+orderId+",productId: "+productId;
        return cacheRepository.findById(stringKey)
                .map(mapper::cacheToOpDto)
                .orElseGet(() -> {
                    OrderProduct op = repository.findById(key).orElseThrow(() -> new ResourceNotFoundException("OrderProduct",stringKey));
                    cacheRepository.save(mapper.opToCache(op));
                    return mapper.opToDto(op);
                });
    }

    public OrderProductDto saveOp(OrderProductDto dto) {
        OrderProduct op = repository.save(mapper.dtoToOp(dto));
        cacheRepository.save(mapper.opToCache(op));
        return dto;

    }

    public OrderProductDto updateOp(int orderId, int productId, OrderProductDto dto) {
        OrderProductKey key = new OrderProductKey(orderId,productId);
        OrderProduct oldOp = repository.findById(key).orElseThrow(() -> new ResourceNotFoundException("OrderProduct","orderId: " +orderId+ ",productId: "+productId));
        oldOp.setQnty(dto.qnty());
        oldOp.setPriceEach(dto.priceEach());
        cacheRepository.save(mapper.opToCache(oldOp));
        return mapper.opToDto(repository.save(oldOp));
    }

    public void deleteOpById(int orderId, int productId) {
        OrderProductKey key = new OrderProductKey(orderId,productId);
        String stringKey = "orderId: "+orderId+",productId: "+productId;
        if (repository.existsById(key)){
            repository.deleteById(key);

        }
        cacheRepository.deleteById(stringKey);


    }

    public CursorPageResponse<OrderProductDto> findOrderProductsKeyset(Integer orderId, Integer productId, int size, PageDirection direction) {
        int pageSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(0,pageSize + 1);
        List<OrderProduct> orderProducts;
        if (orderId == null && productId == null){
            orderProducts = repository.findAll(pageable).getContent();
        }
        else if(direction == PageDirection.NEXT) {
            orderProducts = repository.findNextKeySet(orderId, productId, pageable);
        }
        else {
            orderProducts = repository.findPreviousKeySet(orderId,productId,pageable);
            Collections.reverse(orderProducts);
        }
        boolean hasNext = orderProducts.size() > pageSize;
        boolean hasPrev;
        if (direction == PageDirection.NEXT){
            hasPrev = orderId != null && productId != null;
        }
        else {
            hasPrev = !orderProducts.isEmpty();
        }
        if (hasNext){
            orderProducts = orderProducts.subList(0,pageSize);
        }

        String nextCursor = null;
        String prevCursor = null;
        if(!orderProducts.isEmpty()){
            OrderProduct lastOp = orderProducts.getLast();
            nextCursor = cursorCodec.encode(new OrderProductCursor(lastOp.getOrder().getOrderId(),
                    lastOp.getProduct().getProductCode()));

            OrderProduct firstOp = orderProducts.getFirst();
            prevCursor = cursorCodec.encode(new OrderProductCursor(firstOp.getOrder().getOrderId(),
                    firstOp.getProduct().getProductCode()));
        }
        if (orderId == null && productId == null){
            hasPrev = false;
            prevCursor = null;
        }

        List<OrderProductDto> dtos = orderProducts.stream()
                .peek(orderProduct -> { cacheRepository.save(mapper.opToCache(orderProduct));})
                .map(mapper::opToDto)
                .toList();

        return new CursorPageResponse<>(dtos,nextCursor, prevCursor, hasNext, hasPrev);
    }
}
