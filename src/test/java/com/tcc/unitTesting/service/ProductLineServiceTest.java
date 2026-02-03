package com.tcc.unitTesting.service;

import com.tcc.cache.ProductLineCacheRepository;
import com.tcc.dtos.ProductLineDto;
import com.tcc.entity.ProductLine;
import com.tcc.entity.ProductLineCache;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.persistance.ProductLineRepository;
import com.tcc.service.productLineService.ProductLineMapper;
import com.tcc.service.productLineService.ProductLineService;
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

public class ProductLineServiceTest {
    @Mock
    private ProductLineRepository repository;

    @Mock
    private ProductLineCacheRepository cacheRepository;

    @Mock
    private ProductLineMapper mapper;

    @InjectMocks
    private ProductLineService service;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    private ProductLine productLine1(){
        ProductLine pl = new ProductLine();
        pl.setProductLineId(1);
        pl.setProducts(Collections.emptyList());
        pl.setProducts(Collections.emptyList());
        pl.setImage("Photo");
        pl.setDescInHtml("<body>Nice beautiful text</body>");
        pl.setDescInText("Great product line");
        return pl;
    }
    private ProductLine productLine2(){
        ProductLine pl = new ProductLine();
        pl.setProductLineId(2);
        pl.setProducts(Collections.emptyList());
        pl.setProducts(Collections.emptyList());
        pl.setImage("PhotoBomb");
        pl.setDescInHtml("<body>Bad ugly text</body>");
        pl.setDescInText("Bad product line");
        return pl;
    }
    private ProductLineCache productLineCache(){
        ProductLineCache pl = new ProductLineCache();
        pl.setProductLineId(1);
        pl.setProducts(Collections.emptyList());
        pl.setProducts(Collections.emptyList());
        pl.setImage("Photo");
        pl.setDescInHtml("<body>Nice beautiful text</body>");
        pl.setDescInText("Great product line");
        return pl;
    }
    private ProductLineDto productLineDto(){
        return new ProductLineDto("Great product line","<body>Nice beautiful text</body>","Photo");
    }

    @Test
    void shouldReturnAllProductLine(){
        ProductLine productLine1 = productLine1();
        ProductLine productLine2 = productLine2();
        List<ProductLine> mockedList = List.of(productLine1,productLine2);

        when(repository.findAll()).thenReturn(mockedList);
        when(mapper.productLineToDto(any()))
                .thenAnswer(invocation -> {
                    ProductLine pl = invocation.getArgument(0);
                    return new ProductLineDto(pl.getDescInText(),pl.getDescInHtml(),pl.getImage());
                });

        List<ProductLineDto> returned = service.findAllProductLine();

        assertThat(returned).hasSize(2).isNotNull();
        assertThat(returned.getFirst().descInHtml()).isEqualTo(productLine1.getDescInHtml());
        assertThat(returned.get(1).descInHtml()).isEqualTo(productLine2.getDescInHtml());

        verify(repository).findAll();
        verify(mapper,times(2)).productLineToDto(any());
    }

    @Test
    void shouldFindProductLineWithCacheMemory(){
        int id = 1;
        ProductLineCache cache = productLineCache();
        ProductLineDto dto = productLineDto();

        when(cacheRepository.findById(id)).thenReturn(Optional.of(cache));
        when(mapper.cacheToProductLineDto(cache)).thenReturn(dto);

        ProductLineDto returned = service.findProductLineById(id);

        assertThat(returned.descInHtml()).isEqualTo(dto.descInHtml());
        assertThat(returned.descInText()).isEqualTo(dto.descInText());
        assertThat(returned.image()).isEqualTo(dto.image());

    }

    @Test
    void shouldFindProductLineWithPersistence(){
        int id = 1;
        ProductLine pl = productLine1();
        ProductLineDto dto = productLineDto();
        ProductLineCache cache = productLineCache();

        when(cacheRepository.findById(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.of(pl));
        when(mapper.productLineToCache(pl)).thenReturn(cache);
        when(cacheRepository.save(cache)).thenReturn(cache);
        when(mapper.productLineToDto(pl)).thenReturn(dto);

        ProductLineDto returned = service.findProductLineById(id);

        assertThat(returned.descInHtml()).isEqualTo(pl.getDescInHtml());
        assertThat(returned.descInText()).isEqualTo(pl.getDescInText());
        assertThat(returned.image()).isEqualTo(pl.getImage());
        verify(cacheRepository).findById(id);
        verify(repository).findById(id);
        verify(mapper).productLineToCache(pl);
        verify(cacheRepository).save(cache);
        verify(mapper).productLineToDto(pl);
    }


    @ParameterizedTest
    @ValueSource(ints ={-1,0,99})
    void shouldFailToFindProductLine(int id){
        when(cacheRepository.findById(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,() -> service.findProductLineById(id));

        String expectedMessage = "ProductLine with id " + id + " not found";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        verify(cacheRepository).findById(id);
        verify(repository).findById(id);
    }

    @Test
    void shouldUpdateProductLine(){
        int id = 1;
        ProductLine oldPl = productLine2();
        ProductLineDto dto = productLineDto();

        when(repository.findById(id)).thenReturn(Optional.of(oldPl));
        when(repository.save(any())).thenReturn(oldPl);
        when(mapper.productLineToDto(any()))
                .thenAnswer(invocation ->{
                    ProductLine pl = invocation.getArgument(0);
                    return new ProductLineDto(pl.getDescInText(),pl.getDescInHtml(),pl.getImage());
                });

        ProductLineDto returned = service.updateProductLine(id, dto);

        assertThat(returned.descInHtml()).isEqualTo(dto.descInHtml());
        assertThat(returned.descInText()).isEqualTo(dto.descInText());
        assertThat(returned.image()).isEqualTo(dto.image());
        verify(repository).findById(id);
        verify(repository).save(any());
        verify(mapper).productLineToDto(any());
    }

    @ParameterizedTest
    @ValueSource(ints ={-1,0,99})
    void shouldNotUpdateProductLine(int id){
        ProductLineDto dto = productLineDto();

        when(repository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception =assertThrows(ResourceNotFoundException.class,() -> service.updateProductLine(id,dto));
        String expectedString = "ProductLine with id " + id + " not found";

        assertThat(exception.getMessage()).isEqualTo(expectedString);
        verify(repository).findById(id);


    }
}
