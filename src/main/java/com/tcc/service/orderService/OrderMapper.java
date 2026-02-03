package com.tcc.service.orderService;

import com.tcc.dtos.OrderDto;
import com.tcc.entity.Customer;
import com.tcc.entity.Order;
import com.tcc.entity.OrderCache;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderDto orderToOrderDto(Order order){
        return new OrderDto(order.getCustomerId().getCustomerId(),order.getOrderDate(),order.getRequiredDate(),order.getComments());
    }
    public OrderCache orderToCache(Order order){
        OrderCache cache = new OrderCache();
        cache.setOrderId(order.getOrderId());
        cache.setCustomerId(order.getCustomerId().getCustomerId());
        cache.setOrderDate(order.getOrderDate());
        cache.setComments(order.getComments());
        cache.setRequiredDate(order.getRequiredDate());
        cache.setOrderProductList(order.getOrderProductList() != null ?
                order.getOrderProductList()
                        .stream()
                        .map(orderProduct -> {
                            return "orderId:"+ orderProduct.getOrder().getOrderId()+",productId:"+ orderProduct.getProduct().getProductCode();
                        })
                        .collect(Collectors.toList()): Collections.emptyList());
        return cache;

    }
    public OrderDto cacheToOrderDto(OrderCache cache){
        return new OrderDto(cache.getCustomerId(),cache.getOrderDate(), cache.getRequiredDate(), cache.getComments());
    }
    public Order dtoToOrder(OrderDto dto){
        Order order = new Order();
        order.setComments(dto.commments());
        order.setOrderDate(dto.orderDate());
        order.setRequiredDate(dto.requiredDate());
        Customer customer = new Customer();
        customer.setCustomerId(dto.customerId());
        order.setCustomerId(customer);
        order.setOrderProductList(Collections.emptyList());
        return order;
    }
}
