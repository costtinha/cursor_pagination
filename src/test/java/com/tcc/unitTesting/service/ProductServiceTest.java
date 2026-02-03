package com.tcc.unitTesting.service;

import com.tcc.cache.ProductCacheRepository;
import com.tcc.dtos.ProductDto;
import com.tcc.dtos.ProductResponseDto;
import com.tcc.entity.Product;
import com.tcc.entity.ProductCache;
import com.tcc.entity.ProductLine;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.persistance.ProductRepository;
import com.tcc.service.productService.ProductMapper;
import com.tcc.service.productService.ProductService;
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

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProductServiceTest {
    @Mock
    private ProductRepository repository;

    @Mock
    private ProductCacheRepository cacheRepository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService service;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    private Product product1(){
        Product product = new Product();
        product.setName("Bench press");
        product.setProductCode(1);
        product.setBuyPrice(10);
        product.setScale(5);
        product.setQntyInStock(100);
        ProductLine pl = new ProductLine();
        pl.setProductLineId(1);
        product.setProductLine(pl);
        product.setOrderProductList(Collections.emptyList());
        product.setPdtDescription("For gains");
        return product;
    }

    private Product product2(){
        Product product = new Product();
        product.setName("Deadlift");
        product.setProductCode(2);
        product.setBuyPrice(100);
        product.setScale(20);
        product.setQntyInStock(1);
        ProductLine pl = new ProductLine();
        pl.setProductLineId(1);
        product.setProductLine(pl);
        product.setOrderProductList(Collections.emptyList());
        product.setPdtDescription("For hardcore training");
        return product;
    }

    private ProductCache productCache(){
        ProductCache product = new ProductCache();
        product.setName("Bench press");
        product.setProductCode(1);
        product.setBuyPrice(10);
        product.setScale(5);
        product.setQntyInStock(100);;
        product.setProductLine(1);
        product.setOrderProductList(Collections.emptyList());
        product.setPdtDescription("For gains");
        return product;
    }

    private ProductDto productDto(){
        return new ProductDto("Bench press",5,"For gains",100,10,1);
    }

    private ProductResponseDto responseDto(){
        return new ProductResponseDto("Bench press",5,"For gains",10);
    }


    @Test
    void shouldReturnAllProducts(){
        Product product1 = product1();
        Product product2 = product2();
        List<Product> mockedList = List.of(product1,product2);

        when(repository.findAll()).thenReturn(mockedList);
        when(mapper.productToCache(any()))
                .thenAnswer(invocation ->{
                    Product product = invocation.getArgument(0);
                    ProductCache cache = new ProductCache();
                    cache.setName(product.getName());
                    cache.setProductCode(product.getProductCode());
                    cache.setProductLine(product.getProductLine().getProductLineId());
                    cache.setScale(product.getScale());
                    cache.setBuyPrice(product.getBuyPrice());
                    cache.setOrderProductList(Collections.emptyList());
                    cache.setPdtDescription(product.getPdtDescription());
                    cache.setQntyInStock(product.getQntyInStock());
                    return cache;
                });
        when(mapper.productToResponseDto(any()))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    return new ProductResponseDto(product.getName(),product.getScale(),product.getPdtDescription(),product.getBuyPrice());
                });

        List<ProductResponseDto> returned = service.findAllProducts();

        assertThat(returned).hasSize(2).isNotNull();
        assertThat(returned.getFirst().name()).isEqualTo(product1.getName());
        assertThat(returned.getFirst().buyPrice()).isEqualTo(product1.getBuyPrice());
        assertThat(returned.get(1).name()).isEqualTo(product2.getName());
        assertThat(returned.get(1).buyPrice()).isEqualTo(product2.getBuyPrice());
        verify(repository).findAll();
        verify(mapper,times(2)).productToCache(any());
        verify(mapper,times(2)).productToResponseDto(any());
    }

    @Test
    void shouldFindProductByIdWithCache(){
        int id = 1;
        ProductCache cache = productCache();
        ProductResponseDto dto = responseDto();

        when(cacheRepository.findById(id)).thenReturn(Optional.of(cache));
        when(mapper.productCacheToResponseDto(cache)).thenReturn(dto);

        ProductResponseDto returned = service.findProductById(id);

        assertThat(returned.name()).isEqualTo(dto.name());
        assertThat(returned.buyPrice()).isEqualTo(dto.buyPrice());
        verify(cacheRepository).findById(id);
        verify(mapper).productCacheToResponseDto(cache);



    }


    @Test
    void shouldReturnProductByPersistence(){
        int id = 1;
        Product product1 = product1();
        ProductResponseDto responseDto = responseDto();
        ProductCache cache = productCache();

        when(cacheRepository.findById(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.of(product1));
        when(mapper.productToCache(product1)).thenReturn(cache);
        when(cacheRepository.save(cache)).thenReturn(cache);
        when(mapper.productToResponseDto(product1)).thenReturn(responseDto);

        ProductResponseDto returned = service.findProductById(id);

        assertThat(returned.name()).isEqualTo(responseDto.name());
        assertThat(returned.buyPrice()).isEqualTo(responseDto.buyPrice());
        verify(cacheRepository).findById(id);
        verify(repository).findById(id);
        verify(mapper).productToCache(product1);
        verify(cacheRepository).save(cache);
        verify(mapper).productToResponseDto(product1);

    }

    @ParameterizedTest
    @ValueSource(ints = {-1,0,99})
    void shouldFailToFindProductById(int id){
        when(cacheRepository.findById(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findProductById(id));

        String expectedMessage = "Product with id "+ id + " not found";

        assertThat(expectedMessage).isEqualTo(exception.getMessage());
        verify(cacheRepository).findById(id);
        verify(repository).findById(id);
    }

    @Test
    void shouldUpdateProduct(){
        Product oldProduct = product2();
        ProductDto dto = productDto();
        ProductResponseDto responseDto = responseDto();
        int id = 1;

        when(repository.findById(id)).thenReturn(Optional.of(oldProduct));
        when(repository.save(any())).thenReturn(oldProduct);
        when(mapper.productToCache(any())).thenReturn(productCache());
        when(cacheRepository.save(any())).thenReturn(productCache());
        when(mapper.productToResponseDto(any()))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    return new ProductResponseDto(product.getName(),
                            product.getScale(),
                            product.getPdtDescription(),
                            product.getBuyPrice());
                });

        ProductResponseDto returned = service.updateProduct(id, dto);

        assertThat(returned.buyPrice()).isEqualTo(responseDto.buyPrice());
        assertThat(returned.pdtDescription()).isEqualTo(responseDto.pdtDescription());
        assertThat(returned.scale()).isEqualTo(responseDto.scale());
        assertThat(returned.name()).isEqualTo(responseDto.name());
        verify(repository).findById(id);
        verify(cacheRepository).save(any());
        verify(repository).save(any());
        verify(mapper).productToCache(any());
        verify(mapper).productToResponseDto(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1,0,99})
    void shouldNotUpdateProduct(int id){

        when(repository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.updateProduct(id,productDto()));

        String expectedMessage = "Product with id " + id + " not found";

        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        verify(repository).findById(id);
    }



}
