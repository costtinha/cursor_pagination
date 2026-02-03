package com.tcc.unitTesting.service;

import com.tcc.cache.OrderCacheRepository;
import com.tcc.dtos.OrderDto;
import com.tcc.entity.Customer;
import com.tcc.entity.Order;
import com.tcc.entity.OrderCache;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.persistance.OrderRepository;
import com.tcc.service.orderService.OrderMapper;
import com.tcc.service.orderService.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;



import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceTest {
    @Mock
    private OrderRepository repository;

    @Mock
    private OrderCacheRepository cacheRepository;

    @Mock
    private OrderMapper mapper;

    @InjectMocks
    private OrderService service;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    private Order order1(){
        Order order = new Order();
        order.setOrderId(1);
        order.setOrderDate("12/08/2010");
        order.setComments("comments");
        Customer customer = new Customer();
        customer.setCustomerId(1);
        order.setCustomerId(customer);
        order.setOrderProductList(Collections.emptyList());
        order.setRequiredDate("20/12/2011");
        return order;
    }
    private Order order2(){
        Order order = new Order();
        order.setOrderId(1);
        order.setOrderDate("12/08/2015");
        order.setComments("newComments");
        Customer customer = new Customer();
        customer.setCustomerId(1);
        order.setCustomerId(customer);
        order.setOrderProductList(Collections.emptyList());
        order.setRequiredDate("20/12/2016");
        return order;
    }
    private OrderDto orderDto(){
        return new OrderDto(1,"12/08/2010","20/12/2011","comments");
    }

    private OrderCache orderCache(){
        OrderCache order = new OrderCache();
        order.setOrderId(1);
        order.setOrderDate("12/08/2010");
        order.setComments("comments");
        order.setCustomerId(1);
        order.setOrderProductList(Collections.emptyList());
        order.setRequiredDate("20/12/2011");
        return order;
    }

    @Test
    void shouldReturnAllOrders(){
        Order order1 = order1();
        Order order2 = order2();
        List<Order> mockedList = List.of(order1,order2);

        when(repository.findAll()).thenReturn(mockedList);
        when(mapper.orderToCache(any()))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    OrderCache cache = new OrderCache();
                    cache.setOrderId(order.getOrderId());
                    cache.setComments(order.getComments());
                    cache.setOrderDate(order.getOrderDate());
                    cache.setRequiredDate(order.getRequiredDate());
                    cache.setOrderProductList(Collections.emptyList());
                    return cache;
                });
        when(mapper.orderToOrderDto(any()))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    return new OrderDto(order.getCustomerId().getCustomerId(),
                            order.getOrderDate(),order.getRequiredDate(),
                            order.getComments());
                });

        List<OrderDto> returned = service.getAllOrders();

        assertThat(returned.getFirst().commments()).isEqualTo(order1.getComments());
        assertThat(returned.getFirst().orderDate()).isEqualTo(order1.getOrderDate());
        assertThat(returned.getFirst().requiredDate()).isEqualTo(order1.getRequiredDate());
        assertThat(returned.get(1).commments()).isEqualTo(order2.getComments());
        assertThat(returned.get(1).orderDate()).isEqualTo(order2.getOrderDate());
        assertThat(returned.get(1).requiredDate()).isEqualTo(order2.getRequiredDate());
        verify(repository).findAll();
        verify(mapper,times(2)).orderToCache(any());
        verify(mapper,times(2)).orderToOrderDto(any());
    }


    @Test
    void shouldFindOrderByIdByCache(){
        OrderDto dto = orderDto();
        OrderCache cache = orderCache();

        when(cacheRepository.findById(1)).thenReturn(Optional.of(cache));
        when(mapper.cacheToOrderDto(cache)).thenReturn(dto);

        OrderDto returned = service.findOrderById(1);

        assertThat(returned.commments()).isEqualTo(dto.commments());
        verify(cacheRepository).findById(1);
        verify(mapper).cacheToOrderDto(cache);

    }

    @Test
    void shouldFindOrderWhenCacheFails(){
        int id = 1;
        Order order = order1();
        OrderCache cache = orderCache();
        OrderDto dto = orderDto();

        when(cacheRepository.findById(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.of(order));
        when(mapper.orderToCache(order)).thenReturn(cache);
        when(cacheRepository.save(cache)).thenReturn(cache);
        when(mapper.orderToOrderDto(order)).thenReturn(dto);

        OrderDto returned = service.findOrderById(id);

        assertThat(returned.orderDate()).isEqualTo(dto.orderDate());
        assertThat(returned.requiredDate()).isEqualTo(dto.requiredDate());
        verify(cacheRepository).findById(id);
        verify(repository).findById(id);
        verify(mapper).orderToCache(order);
        verify(cacheRepository).save(cache);
        verify(mapper).orderToOrderDto(order);
        verify(mapper).orderToOrderDto(order);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1,0,99})
    void shouldFailToFindOrderWhenIdNotFound(int id){
        when(cacheRepository.findById(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,() -> service.findOrderById(id));

        String expectedString = "Order with id " + id + " not found";

        assertThat(exception.getMessage()).isEqualTo(expectedString);
        verify(cacheRepository).findById(id);
        verify(repository).findById(id);
    }


    @Test
    void shouldUpdateOrder(){
        Order oldOrder = order2();
        OrderDto dto = orderDto();
        int id =1;
        when(repository.findById(id)).thenReturn(Optional.of(oldOrder));
        when(repository.save(any())).thenReturn(oldOrder);
        when(mapper.orderToOrderDto(oldOrder))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    return new OrderDto(order.getCustomerId().getCustomerId(),
                            order.getOrderDate(),
                            order.getRequiredDate(),
                            order.getComments());
                });

        OrderDto returned = service.updateOrder(dto,id);

        assertThat(returned.requiredDate()).isEqualTo(dto.requiredDate());
        assertThat(returned.orderDate()).isEqualTo(dto.orderDate());
        assertThat(returned.commments()).isEqualTo(dto.commments());
        verify(repository).findById(id);
        verify(repository).save(any());
        verify(mapper).orderToOrderDto(oldOrder);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1,0,99})
    void shouldNotUpdateOrderWhenOrderIdNotFound(int id){
        OrderDto dto = orderDto();
        when(repository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,() -> service.updateOrder(dto,id));

        String expectedString = "Order with id "+ id + " not found";

        assertThat(expectedString).isEqualTo(exception.getMessage());
        verify(repository).findById(id);

    }



}
