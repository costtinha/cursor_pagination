package com.tcc.unitTesting.service;

import com.tcc.cache.OfficeCacheRepository;
import com.tcc.dtos.OfficeDto;
import com.tcc.entity.Office;
import com.tcc.entity.OfficeCache;
import com.tcc.exception.ConflictException;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.persistance.OfficeRepository;
import com.tcc.service.officeService.OfficeMapper;
import com.tcc.service.officeService.OfficeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OfficeServiceTest {
    @Mock
    private OfficeRepository repository;

    @Mock
    private OfficeCacheRepository cacheRepository;

    @Mock
    private OfficeMapper mapper;

    @InjectMocks
    private OfficeService service;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }
    private Office office(String officeId, String officeName, String email, String phone) {
        Office office = new Office();
        office.setOfficeId(Integer.parseInt(officeId));
        office.setOfficeName(officeName);
        office.setEmail(email);
        office.setPhone(phone);
        return office;
    }
    private OfficeCache officeCache(String officeId, String officeName, String email, String phone) {
        OfficeCache office = new OfficeCache();
        office.setOfficeId(Integer.parseInt(officeId));
        office.setOfficeName(officeName);
        office.setEmail(email);
        office.setPhone(phone);
        return office;
    }

    @Test
    void shouldReturnAllOffices(){
        //setting the offices and list
        Office office1 = office("1","cargo","email@gmail.com","99888");
        Office office2 = office("2","drake","drake@gmail.com","888111888");

        List<Office> mockedList = List.of(office1, office2);

        Pageable pageable = PageRequest.of(0,20);

        Page<Office> officePage = new PageImpl<>(
                mockedList,
                pageable,
                mockedList.size()
        );


        // preparing the test logic
        when(repository.findAll(any(Pageable.class))).thenReturn(officePage);
        when(mapper.officeToDto(any(Office.class)))
                .thenAnswer(invocation ->{
                    Office office = invocation.getArgument(0);
                    return new OfficeDto(office.getOfficeName(), office.getEmail(), office.getPhone());
                });


        // testing results
        Page<OfficeDto> returned = service.allOffices(pageable);

        assertThat(returned).isNotNull();
        assertThat(returned.getContent()).hasSize(2);
        assertThat(returned.getContent().get(1).officeName()).isEqualTo("drake");
        assertThat(returned.getContent().get(0).officeName()).isEqualTo("cargo");

        assertThat(returned.getTotalElements()).isEqualTo(2);
        assertThat(returned.getTotalPages()).isEqualTo(1);
        assertThat(returned.hasNext()).isEqualTo(false);

        verify(repository).findAll(any(Pageable.class));

    }

    @Test
    void shouldFindOfficeByIdSuccessCache(){
        OfficeCache office1 = officeCache("1","cargo","email@gmail.com","99888");
        when(cacheRepository.findById(1)).thenReturn(Optional.of(office1));
        when(mapper.cacheToOfficeDto(any(OfficeCache.class)))
                .thenAnswer(invocation ->{
                    OfficeCache cache = invocation.getArgument(0);
                    return new OfficeDto(cache.getOfficeName(),cache.getEmail(),cache.getPhone());
                });

        OfficeDto returned = service.findOfficeById(1);

        assertNotNull(returned);
        assertThat(returned.officeName()).isEqualTo("cargo");
        assertThat(returned.email()).isEqualTo("email@gmail.com");
        verify(cacheRepository).findById(1);
        verify(mapper).cacheToOfficeDto(any());

    }

    @Test
    void shouldFindOfficeByIdAfterFailedCache(){
        Office office = office("1","cargo","email@gmail.com","99888");
        OfficeCache cache = officeCache("1","cargo","email@gmail.com","99888");
        OfficeDto dto = new OfficeDto(office.getOfficeName(),office.getEmail(),office.getPhone());


        when(cacheRepository.findById(1)).thenReturn(Optional.empty());
        when(repository.findById(1)).thenReturn(Optional.of(office));
        when(mapper.officeToCache(office)).thenReturn(cache);
        when(cacheRepository.save(cache)).thenReturn(cache);
        when(mapper.officeToDto(office)).thenReturn(dto);

        OfficeDto returned = service.findOfficeById(1);


        assertThat(returned.officeName()).isEqualTo("cargo");
        assertThat(returned.email()).isEqualTo("email@gmail.com");
        assertThat(returned.phone()).isEqualTo("99888");
        verify(cacheRepository).findById(1);
        verify(repository).findById(1);
        verify(mapper).officeToCache(any());
        verify(mapper).officeToDto(any());
        verify(cacheRepository).save(any());
    }

    @Test
    void shouldReceiveAnErrorWhenSearchingAnNonExistentOfficeId(){
        int nonExistentId = 99;

        when(cacheRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(repository.findById(anyInt())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findOfficeById(nonExistentId));

        String expectedMessage = "Office with id " + nonExistentId + " not found";
        assertEquals(expectedMessage,exception.getMessage());
        verify(cacheRepository).findById(nonExistentId);
        verify(repository).findById(nonExistentId);
    }


    @Test
    void shouldSaveNewOfficeSuccessfully(){
        Office office = office("1","cargo","email@gmail.com","99888");
        OfficeDto dto = new OfficeDto(office.getOfficeName(),office.getEmail(),office.getPhone());

        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(mapper.dtoToOffice(dto)).thenReturn(office);
        when(cacheRepository.save(any())).thenReturn(any());
        when(repository.save(office)).thenReturn(office);
        when(mapper.officeToDto(office)).thenReturn(dto);

        OfficeDto returned = service.saveOffice(dto);
    }

    @Test
    void shouldNotSaveNewOfficeWhenEmailAlreadyExists(){
        Office office = office("1","cargo","email@gmail.com","99888");
        OfficeDto dto = new OfficeDto(office.getOfficeName(),office.getEmail(),office.getPhone());

        when(repository.existsByEmail(dto.email())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,() -> service.saveOffice(dto));
        String expectedMessage = "There is already an office with this email: " + dto.email();



        assertEquals(expectedMessage,exception.getMessage());
        verify(repository).existsByEmail(dto.email());
    }

    @Test
    void shouldDeleteById(){
        when(repository.existsById(any())).thenReturn(true);
        doNothing().when(repository).deleteById(anyInt());
        doNothing().when(cacheRepository).deleteById(anyInt());

        service.deleteById(anyInt());
        verify(repository).existsById(any());
        verify(repository).deleteById(anyInt());
        verify(cacheRepository).deleteById(anyInt());

    }

    @Test
    void shouldThrowWhenDeletingById(){
        int id = 99;
        when(repository.existsById(id)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.deleteById(id));


        String expectedMessage = "Office with id " + id + " not found";
        assertEquals(expectedMessage,exception.getMessage());
        verify(repository).existsById(id);


    }

    @Test
    void shouldUpdateOffice(){
        OfficeDto dto = new OfficeDto("cargo","email@gmail.com","99888");
        OfficeCache cache = officeCache("1","cargo","email@gmail.com","99888");
        Office updatedOffice = office("1","cargo","email@gmail.com","99888");
        Office oldOffice = office("1","dinoland","oldMail@gmail.com","999999");

        int id = 1;

        when(repository.findById(anyInt())).thenReturn(Optional.of(oldOffice));
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.save(any())).thenReturn(updatedOffice);
        when(mapper.officeToCache(any())).thenReturn(cache);
        when(cacheRepository.save(any())).thenReturn(cache);
        when(mapper.officeToDto(any())).thenReturn(dto);

        OfficeDto returned = service.updateOfficeById(dto,id);

        assertEquals("cargo",returned.officeName());
        assertEquals("email@gmail.com",returned.email());
        assertEquals("99888",returned.phone());
        verify(repository).findById(id);
        verify(repository).existsByEmail("email@gmail.com");
        verify(repository).save(any());
        verify(mapper).officeToCache(any());
        verify(cacheRepository).save(any());
        verify(mapper).officeToDto(any());

    }

    @Test
    void shouldFailToUpdateOfficeWhenOfficeIdDontExist(){
        int id = 99;
        OfficeDto dto = new OfficeDto("cargo","email@gmail.com","99888");

        when(repository.findById(anyInt())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.updateOfficeById(dto,id));

        String expectedMessage = "Office with id " + id + " not found";

        assertEquals(expectedMessage,exception.getMessage());
        verify(repository).findById(id);
    }

    @Test
    void shouldFailToUpdateOfficeWhenNewEmailAlreadyExists(){
        int id = 1;
        OfficeDto dto = new OfficeDto("cargo","email@gmail.com","99888");
        Office oldOffice = office("1","dinoland","oldmail@gmail.com","999999");

        when(repository.findById(anyInt())).thenReturn(Optional.of(oldOffice));
        when(repository.existsByEmail(any())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.updateOfficeById(dto,id));

        String expectedMessage = "There is already an office with this email: " + dto.email();

        assertEquals(expectedMessage,exception.getMessage());
        verify(repository).findById(id);
        verify(repository).existsByEmail(dto.email());



    }



}
