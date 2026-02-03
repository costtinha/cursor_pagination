package com.tcc.service.orderProductService;

import com.tcc.dtos.OrderProductDto;
import com.tcc.entity.*;
import org.springframework.stereotype.Component;

@Component
public class OrderProductMapper {
    public OrderProductDto opToDto(OrderProduct op){
        return new OrderProductDto(op.getOrder().getOrderId(),op.getProduct().getProductCode(),op.getQnty(),op.getPriceEach());
    }
    public OrderProductCache opToCache(OrderProduct op){
        OrderProductCache cache = new OrderProductCache();
        cache.setOrderId(op.getOrder().getOrderId());
        cache.setProductId(op.getProduct().getProductCode());
        cache.setQnty(op.getQnty());
        cache.setPriceEach(op.getPriceEach());
        cache.generateOrderProductKey();
        return cache;
    }
    public OrderProductDto cacheToOpDto(OrderProductCache cache){
        return new OrderProductDto(cache.getOrderId(),cache.getProductId(),cache.getQnty(),cache.getPriceEach());
    }

    public OrderProduct dtoToOp(OrderProductDto dto){
        OrderProduct op = new OrderProduct();
        Order order = new Order();
        order.setOrderId(dto.orderId());
        op.setOrder(order);
        Product product = new Product();
        product.setProductCode(dto.productId());
        op.setProduct(product);
        op.setQnty(dto.qnty());
        op.setPriceEach(dto.priceEach());
        OrderProductKey key = new OrderProductKey(dto.orderId(), dto.productId());
        op.setOrderProductKey(key);
        return op;
    }
}
