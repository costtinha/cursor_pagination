package com.tcc.unitTesting.service;

import com.tcc.cache.OrderProductCacheRepository;
import com.tcc.dtos.OrderProductDto;
import com.tcc.entity.*;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.persistance.OrderProductRepository;
import com.tcc.service.orderProductService.OrderProductMapper;
import com.tcc.service.orderProductService.OrderProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSources;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderProductServiceTest {
    @Mock
    private OrderProductCacheRepository cacheRepository;

    @Mock
    private OrderProductRepository repository;

    @Mock
    private OrderProductMapper mapper;

    @InjectMocks
    private OrderProductService service;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    private OrderProduct orderProduct1(){
        OrderProduct op = new OrderProduct();
        OrderProductKey key = new OrderProductKey(1,1);
        Product product = new Product();
        product.setProductCode(1);
        Order order = new Order();
        order.setOrderId(1);
        op.setProduct(product);
        op.setOrder(order);
        op.setQnty(10);
        op.setPriceEach(100);
        return op;
    }

    private OrderProduct orderProduct2(){
        OrderProduct op = new OrderProduct();
        OrderProductKey key = new OrderProductKey(1,2);
        Product product = new Product();
        product.setProductCode(2);
        Order order = new Order();
        order.setOrderId(1);
        op.setProduct(product);
        op.setOrder(order);
        op.setQnty(100);
        op.setPriceEach(10);
        return op;
    }
    private OrderProductCache orderProductCache(){
        OrderProductCache cache = new OrderProductCache();
        cache.setOrderId(1);
        cache.setProductId(1);
        cache.setQnty(10);
        cache.setPriceEach(100);
        cache.generateOrderProductKey();
        return cache;
    }

    private OrderProductDto orderProductDto(){
        return new OrderProductDto(1,1,10,100);
    }


    @Test
    void shouldReturnAllOrderProducts(){
        OrderProduct op1 = orderProduct1();
        OrderProduct op2 = orderProduct2();
        List<OrderProduct> mockedList = List.of(op1,op2);

        when(repository.findAll()).thenReturn(mockedList);
        when(mapper.opToDto(any()))
                .thenAnswer(invocation -> {
                    OrderProduct op = invocation.getArgument(0);
                    return new OrderProductDto(op.getOrder().getOrderId(),
                            op.getProduct().getProductCode(),
                            op.getQnty(),op.getPriceEach());
                });

        List<OrderProductDto> returned = service.findOrderProduct();


        assertThat(returned).hasSize(2).isNotNull();
        assertThat(returned.getFirst().orderId()).isEqualTo(op1.getOrder().getOrderId());
        assertThat(returned.getFirst().priceEach()).isEqualTo(op1.getPriceEach());
        assertThat(returned.get(1).priceEach()).isEqualTo(op2.getPriceEach());
        verify(repository).findAll();
        verify(mapper,times(2)).opToDto(any());


    }

    @Test
    void shouldFindOrderProductByCacheRepository(){
        int orderId = 1 , productId = 1;
        OrderProductCache cache = orderProductCache();
        OrderProductDto dto = orderProductDto();


        when(cacheRepository.findById(cache.getOrderProductKey())).thenReturn(Optional.of(cache));
        when(mapper.cacheToOpDto(cache)).thenReturn(dto);

        OrderProductDto returned = service.findOrderProductById(orderId,productId);

        assertThat(returned.priceEach()).isEqualTo(dto.priceEach()).isEqualTo(cache.getPriceEach());
        assertThat(returned.qnty()).isEqualTo(dto.qnty()).isEqualTo(cache.getQnty());
        verify(cacheRepository).findById(cache.getOrderProductKey());
        verify(mapper).cacheToOpDto(cache);

    }

    @Test
    void shouldFindOrderProductByPersistence(){
        int orderId =1, productId =1;
        OrderProductKey key = new OrderProductKey(1,1);
        OrderProductCache cache = orderProductCache();
        OrderProduct op1 = orderProduct1();
        OrderProductDto dto = orderProductDto();

        when(cacheRepository.findById(cache.getOrderProductKey())).thenReturn(Optional.empty());
        when(repository.findById(key)).thenReturn(Optional.of(op1));
        when(mapper.opToCache(op1)).thenReturn(cache);
        when(cacheRepository.save(cache)).thenReturn(cache);
        when(mapper.opToDto(op1)).thenReturn(dto);

        OrderProductDto returned = service.findOrderProductById(orderId,productId);

        assertThat(returned.qnty()).isEqualTo(op1.getQnty()).isEqualTo(dto.qnty());
        assertThat(returned.priceEach()).isEqualTo(op1.getPriceEach());
        verify(cacheRepository).findById(cache.getOrderProductKey());
        verify(repository).findById(key);
        verify(mapper).opToCache(op1);
        verify(cacheRepository).save(cache);
        verify(mapper).opToDto(op1);
    }


    @Test
    void shouldNotFindOrderProductById(){
        OrderProductCache cache = orderProductCache();

        when(cacheRepository.findById(any())).thenReturn(Optional.empty());
        when(repository.findById(any())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findOrderProductById(1,1));
        String expectedString = "OrderProduct with id " + cache.getOrderProductKey() + " not found";

        assertThat(exception.getMessage()).isEqualTo(expectedString);
        verify(cacheRepository).findById(any());
        verify(repository).findById(any());
    }

    @Test
    void shouldUpdateOrderProduct(){
        OrderProduct olderOp = orderProduct2();
        OrderProductDto dto = orderProductDto();
        OrderProductKey key = new OrderProductKey(1,1);

        when(repository.findById(key)).thenReturn(Optional.of(olderOp));
        when(repository.save(any())).thenReturn(olderOp);
        when(mapper.opToDto(olderOp))
                .thenAnswer(invocation -> {
                   OrderProduct op = invocation.getArgument(0);
                   return new OrderProductDto(op.getOrder().getOrderId(),
                           op.getProduct().getProductCode(),
                           op.getQnty(),
                           op.getPriceEach());
                });

        OrderProductDto returned = service.updateOp(1,1, dto);

        assertThat(returned.priceEach()).isEqualTo(dto.priceEach());
        assertThat(returned.qnty()).isEqualTo(dto.qnty());
        verify(repository).findById(key);
        verify(repository).save(any());
        verify(mapper).opToDto(olderOp);

    }

    @Test
    void shouldNotUpdateOrderProduct(){
        OrderProductKey key = new OrderProductKey(1,1);
        OrderProductCache cache = orderProductCache();
        OrderProductDto dto = orderProductDto();

        when(repository.findById(key)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.updateOp(1,1,dto));
        String expectedString = "OrderProduct with id " + cache.getOrderProductKey() + " not found";

        assertThat(exception.getMessage()).isEqualTo(expectedString);
        verify(repository).findById(key);
    }

    @Test
    void shouldDeleteOrderProductById(){
        OrderProductKey key = new OrderProductKey(1,1);
        OrderProductCache cache = orderProductCache();

        when(repository.existsById(key)).thenReturn(true);

        service.deleteOpById(1,1);
        verify(repository).existsById(key);
        verify(repository).deleteById(key);
        verify(cacheRepository).deleteById(cache.getOrderProductKey());
    }

    @Test
    void shouldNotDeleteOrderProductById(){
        OrderProductKey key = new OrderProductKey(1,1);
        OrderProductCache cache = orderProductCache();

        when(repository.existsById(key)).thenReturn(false);

        service.deleteOpById(1,1);

        verify(repository).existsById(key);
        verify(cacheRepository).deleteById(cache.getOrderProductKey());
    }


}
