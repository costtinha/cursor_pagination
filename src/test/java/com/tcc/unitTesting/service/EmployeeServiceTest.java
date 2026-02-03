package com.tcc.unitTesting.service;

import com.tcc.cache.EmployeeCacheRepository;
import com.tcc.dtos.EmployeeDto;
import com.tcc.dtos.EmployeeResponseDto;
import com.tcc.entity.Employee;
import com.tcc.entity.EmployeeCache;
import com.tcc.entity.Office;
import com.tcc.exception.ConflictException;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.persistance.EmployeeRepository;
import com.tcc.service.employeeService.EmployeeMapper;
import com.tcc.service.employeeService.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private EmployeeCacheRepository cacheRepository;

    @Mock
    private EmployeeMapper mapper;

    @InjectMocks
    private EmployeeService service;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    private Employee employee1(){
        Employee e = new Employee();
        e.setEmployeeId(1);
        e.setEmployeeName("Carlos");
        e.setEmail("carlos@gmail.com");
        e.setEmployeeList(Collections.emptyList());
        Office o = new Office();
        o.setOfficeId(1);
        o.setOfficeName("cargo");
        e.setOffice(o);
        e.setPhone("99888");
        e.setCustomers(Collections.emptyList());
        Employee chief = new Employee();
        chief.setEmployeeId(99);
        e.setReportsTo(chief);
        return e;
    }
    private Employee employee2(){
        Employee e = new Employee();
        e.setEmployeeId(1);
        e.setEmployeeName("Lucia");
        e.setEmail("lucia@gmail.com");
        e.setEmployeeList(Collections.emptyList());
        Office o = new Office();
        o.setOfficeId(1);
        o.setOfficeName("drake");
        e.setOffice(o);
        e.setPhone("888100");
        e.setCustomers(Collections.emptyList());
        Employee chief = new Employee();
        chief.setEmployeeId(99);
        e.setReportsTo(chief);
        return e;
    }
    private EmployeeCache employeeCache(){
        EmployeeCache e = new EmployeeCache();
        e.setEmployeeId(1);
        e.setEmployeeName("Carlos");
        e.setEmail("carlos@gmail.com");
        e.setSubordinary(Collections.emptyList());
        e.setOffice(1);
        e.setPhone("99888");
        e.setCustomerId(Collections.emptyList());
        e.setReportsTo(99);
        return e;
    }
    private EmployeeDto employeeDto(){
        return new EmployeeDto("Carlos","99888","carlos@gmail.com",99,1);
    }
    private EmployeeResponseDto responseDto(){
        return new EmployeeResponseDto("Carlos","99888","carlos@gmail.com",1);
    }

    @Test
    void shouldReturnAllEmployees(){
        Employee employee1 = employee1();
        Employee employee2 = employee2();
        List<Employee> mockedList = List.of(employee1,employee2);

        when(repository.findAll()).thenReturn(mockedList);
        when(mapper.employeeToCache(any(Employee.class))).thenAnswer(invocation -> {
                    Employee employee = invocation.getArgument(0);
                    EmployeeCache cache = new EmployeeCache();
                    cache.setCustomerId(Collections.emptyList());
                    cache.setEmployeeId(employee.getEmployeeId());
                    cache.setSubordinary(Collections.emptyList());
                    cache.setEmail(employee.getEmail());
                    cache.setEmployeeName(employee.getEmployeeName());
                    cache.setPhone(employee.getPhone());
                    cache.setOffice(employee.getOffice().getOfficeId());
                    cache.setReportsTo(employee.getReportsTo().getEmployeeId());

                    return cache;
                });
        when(cacheRepository.save(any(EmployeeCache.class))).thenReturn(new EmployeeCache());
        when(mapper.employeeToResponseDto(any(Employee.class)))
                .thenAnswer(invocation -> {
                    Employee employee = invocation.getArgument(0);
                    return new EmployeeResponseDto(employee.getEmployeeName()
                            ,employee.getPhone(),
                            employee.getEmail(),
                            employee.getOffice().getOfficeId());
                });

        List<EmployeeResponseDto> returned = service.findAllEmployees();

        ArgumentCaptor<EmployeeCache> captor = ArgumentCaptor.forClass(EmployeeCache.class);

        assertThat(returned).hasSize(2).isNotNull();
        assertThat(returned.getFirst().employeeName()).isEqualTo("Carlos");
        assertThat(returned.get(1).employeeName()).isEqualTo("Lucia");

        verify(repository).findAll();
        verify(cacheRepository,times(2)).save(captor.capture());
        verify(mapper,times(2)).employeeToCache(any(Employee.class));
        verify(mapper,times(2)).employeeToResponseDto(any(Employee.class));
        List<EmployeeCache> savedCache = captor.getAllValues();

        assertThat(savedCache.getFirst().getEmployeeName()).isEqualTo("Carlos");
        assertThat(savedCache.get(1).getEmployeeName()).isEqualTo("Lucia");
    }

    @Test
    void shouldReturnEmployeeByIdByCacheRepository(){
        int id = 1;
        EmployeeCache cache = employeeCache();
        EmployeeResponseDto dto = responseDto();

        when(cacheRepository.findById(id)).thenReturn(Optional.of(cache));
        when(mapper.cacheToEmployeeResponseDto(cache)).thenReturn(dto);

        EmployeeResponseDto returned = service.findEmployeeById(id);

        assertThat(returned.employeeName()).isEqualTo("Carlos");
        assertThat(returned.email()).isEqualTo("carlos@gmail.com");
        assertThat(returned.phone()).isEqualTo("99888");
        assertThat(returned.office()).isEqualTo(1);

        verify(cacheRepository).findById(id);
        verify(mapper).cacheToEmployeeResponseDto(cache);
    }

    @Test
    void shouldReturnEmployeeByIdWhenCacheFails(){
        int id = 1;
        Employee employee1 = employee1();
        EmployeeResponseDto dto = responseDto();
        EmployeeCache cache = employeeCache();

        when(cacheRepository.findById(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.of(employee1));
        when(mapper.employeeToCache(employee1)).thenReturn(cache);
        when(cacheRepository.save(cache)).thenReturn(cache);
        when(mapper.employeeToResponseDto(employee1)).thenReturn(dto);

        EmployeeResponseDto returned = service.findEmployeeById(id);

        assertThat(returned.employeeName()).isEqualTo("Carlos");
        assertThat(returned.phone()).isEqualTo("99888");
        assertThat(returned.email()).isEqualTo("carlos@gmail.com");

        verify(cacheRepository).findById(id);
        verify(repository).findById(id);
        verify(mapper).employeeToCache(employee1);
        verify(cacheRepository).save(cache);
        verify(mapper).employeeToResponseDto(employee1);
    }



    @ParameterizedTest
    @ValueSource(ints = {99, 0, -1})
    void shouldNotReturnEmployeeWhenIdDoesNotExists(int invalidId){

        when(cacheRepository.findById(invalidId)).thenReturn(Optional.empty());
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findEmployeeById(invalidId));

        String expectedString = "Employee with id "+ invalidId +  " not found";

        assertThat(expectedString).isEqualTo(exception.getMessage());
        verify(cacheRepository).findById(invalidId);
        verify(repository).findById(invalidId);
    }

    @Test
    void shouldSaveNewEmployee(){
        EmployeeDto dto = employeeDto();
        Employee employee = employee1();
        EmployeeCache cache = employeeCache();
        EmployeeResponseDto responseDto = responseDto();

        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(mapper.dtoToEmployee(dto)).thenReturn(employee);
        when(mapper.employeeToCache(employee)).thenReturn(cache);
        when(cacheRepository.save(cache)).thenReturn(cache);
        when(repository.save(employee)).thenReturn(employee);
        when(mapper.employeeToResponseDto(employee)).thenReturn(responseDto);

        EmployeeResponseDto returned = service.saveEmployee(dto);

        assertThat(returned.employeeName()).isEqualTo(responseDto.employeeName());
        assertThat(returned.email()).isEqualTo(responseDto.email());
        assertThat(returned.phone()).isEqualTo(responseDto.phone());

        verify(repository).existsByEmail(dto.email());
        verify(cacheRepository).save(cache);
        verify(mapper).dtoToEmployee(dto);
        verify(mapper).employeeToResponseDto(employee);
        verify(repository).save(employee);
    }

    @Test
    void shouldNotSaveNewEmployeeGivenEmailIsNotUnique(){
        EmployeeDto dto = employeeDto();

        when(repository.existsByEmail(dto.email())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,() -> service.saveEmployee(dto));

        String expectedMessage = "There is already an employee with the email: "+ dto.email();

        assertThat(expectedMessage).isEqualTo(exception.getMessage());
        verify(repository).existsByEmail(dto.email());
    }

    @Test
    void shouldUpdateEmployee(){
        int id = 1;
        EmployeeDto dto = employeeDto();
        Employee oldEmployee = employee2();
        Employee newEmployee = employee1();
        EmployeeCache cache = employeeCache();
        EmployeeResponseDto responseDto = responseDto();

        when(repository.findById(id)).thenReturn(Optional.of(oldEmployee));
        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(mapper.employeeToCache(any())).thenReturn(cache);
        when(cacheRepository.save(cache)).thenReturn(cache);
        when(repository.save(any())).thenReturn(newEmployee);
        when(mapper.employeeToResponseDto(newEmployee)).thenReturn(responseDto);

        EmployeeResponseDto returned = service.updateEmployee(dto,id);

        assertThat(returned.employeeName()).isEqualTo(responseDto.employeeName());
        assertThat(returned.phone()).isEqualTo(responseDto.phone());
        assertThat(returned.email()).isEqualTo(responseDto.email());
        verify(repository).findById(id);
        verify(repository).existsByEmail(dto.email());
        verify(mapper).employeeToCache(any());
        verify(cacheRepository).save(cache);
        verify(repository).save(any());
        verify(mapper).employeeToResponseDto(newEmployee);
    }

    @Test
    void shouldNotUpdateEmployeeWhenOldEmployeeNotFound(){
        EmployeeDto dto = employeeDto();
        int id = 1;

        when(repository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.updateEmployee(dto,id));

        String expectedString = "Employee with id "+ id + " not found";

        assertThat(expectedString).isEqualTo(exception.getMessage());
        verify(repository).findById(id);
    }
    @Test
    void shouldNotUpdateEmployeeWhenEmailAlreadyExists(){
        int id = 1;
        Employee oldEmployee = employee2();
        EmployeeDto dto = employeeDto();

        when(repository.findById(id)).thenReturn(Optional.of(oldEmployee));
        when(repository.existsByEmail(dto.email())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.updateEmployee(dto,id));

        String expectedMessage = "There is already an employee with the " + dto.email() + " email";

        assertThat(expectedMessage).isEqualTo(exception.getMessage());
        verify(repository).findById(id);
        verify(repository).existsByEmail(dto.email());

    }

    @Test
    void shouldDeleteById(){
        when(repository.existsById(anyInt())).thenReturn(true);

        service.deleteById(1);

        verify(repository).existsById(1);
        verify(repository).deleteById(1);
        verify(cacheRepository).deleteById(1);
    }

    @Test
    void shouldNotDeleteByIdWhenEmployeeNotFound(){
        when(repository.existsById(1)).thenReturn(false);

        service.deleteById(1);

        verify(repository).existsById(1);
        verify(cacheRepository).deleteById(1);
    }
}
