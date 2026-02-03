package com.tcc.unitTesting.service;

import com.tcc.cache.CustomerCacheRepository;
import com.tcc.dtos.CustomerDto;
import com.tcc.dtos.CustomerResponseDto;
import com.tcc.entity.Customer;
import com.tcc.entity.CustomerCache;
import com.tcc.entity.Employee;
import com.tcc.exception.ResourceNotFoundException;
import com.tcc.persistance.CustomerRepository;
import com.tcc.service.customerService.CustomerMapper;
import com.tcc.service.customerService.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CustomerServiceTest {
    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerCacheRepository cacheRepository;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private CustomerService service;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    private Customer customer1(){
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setName("Carlos");
        customer.setCountry("Brazil");
        customer.setPhone("99888");
        customer.setState("RJ");
        customer.setPostalCode("555");
        customer.setAddress("Drakes passage");
        Employee employee = new Employee();
        employee.setEmployeeId(1);
        customer.setSalesRepEmployee(employee);
        customer.setCity("Niteroi");
        return customer;
    }
    private Customer customer2(){
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setName("Lucia");
        customer.setCountry("Brazil");
        customer.setPhone("8881");
        customer.setState("RJ");
        customer.setPostalCode("555");
        customer.setAddress("Drakes passage");
        Employee employee = new Employee();
        employee.setEmployeeId(2);
        customer.setSalesRepEmployee(employee);
        customer.setCity("Niteroi");
        return customer;
    }

    private CustomerCache customerCache(){
        CustomerCache customer = new CustomerCache();
        customer.setCustomerId(1);
        customer.setName("Carlos");
        customer.setCountry("Brazil");
        customer.setPhone("99888");
        customer.setState("RJ");
        customer.setPostalCode("555");
        customer.setAddress("Drakes passage");
        customer.setSalesRepEmployee(1);
        customer.setCity("Niteroi");
        return customer;
    }

    private CustomerDto customerDto(){
        return new CustomerDto(1,"Carlos","99888","RJ","555","Brazil");
    }
    private CustomerResponseDto responseDto(){
        return new CustomerResponseDto("Carlos","99888","RJ","Brazil");
    }

    @Test
    void shouldReturnAllCustomers(){
        Customer customer1 = customer1();
        Customer customer2 = customer2();
        List<Customer> mockedList = List.of(customer1,customer2);

        when(repository.findAll()).thenReturn(mockedList);
        when(mapper.customerToCache(any(Customer.class)))
                .thenAnswer(invocation -> {
                    Customer customer = invocation.getArgument(0);
                    CustomerCache cache = new CustomerCache();
                    cache.setAddress(customer.getAddress());
                    cache.setCity(customer.getCity());
                    cache.setPhone(customer.getPhone());
                    cache.setCustomerId(customer.getCustomerId());
                    cache.setCountry(customer.getCountry());
                    cache.setState(customer.getState());
                    cache.setName(customer.getName());
                    cache.setPostalCode(customer.getPostalCode());
                    cache.setSalesRepEmployee(customer.getSalesRepEmployee().getEmployeeId());
                    return cache;
                });
        when(mapper.customerToResponseDto(any(Customer.class)))
                .thenAnswer(invocation -> {
                    Customer customer = invocation.getArgument(0);
                    return new CustomerResponseDto(customer.getName(),customer.getPhone(),customer.getState(),customer.getCountry());
        });

        List<CustomerResponseDto> returned = service.findAllCustomers();

        assertThat(returned).hasSize(2).isNotNull();
        assertThat(returned.getFirst().name()).isEqualTo("Carlos");
        assertThat(returned.get(1).name()).isEqualTo("Lucia");
        verify(repository).findAll();
        verify(mapper,times(2)).customerToCache(any(Customer.class));
        verify(mapper,times(2)).customerToResponseDto(any(Customer.class));
    }

    @Test
    void shouldFindCustomerByIdCacheRepository(){
            int id = 1;
            CustomerCache cache = customerCache();
            CustomerResponseDto responseDto = responseDto();

            when(cacheRepository.findById(id)).thenReturn(Optional.of(cache));
            when(mapper.cacheToCustomerResponseDto(cache)).thenReturn(responseDto);

            CustomerResponseDto returned = service.findCustomersById(id);

            assertThat(returned.name()).isEqualTo("Carlos");
            assertThat(returned.phone()).isEqualTo("99888");
            assertThat(returned.state()).isEqualTo("RJ");

            verify(cacheRepository).findById(id);
            verify(mapper).cacheToCustomerResponseDto(cache);
    }

    @Test
    void shouldReturnCustomerAfterCacheFailed(){
        int id =1;
        Customer customer = customer1();
        CustomerCache cache = customerCache();
        CustomerResponseDto responseDto = responseDto();

        when(cacheRepository.findById(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.of(customer));
        when(mapper.customerToCache(customer)).thenReturn(cache);
        when(mapper.customerToResponseDto(customer)).thenReturn(responseDto);

        CustomerResponseDto returned = service.findCustomersById(id);

        assertThat(returned.name()).isEqualTo("Carlos");
        assertThat(returned.phone()).isEqualTo("99888");
        assertThat(returned.state()).isEqualTo("RJ");
        verify(cacheRepository).findById(id);
        verify(repository).findById(id);
        verify(mapper).customerToCache(customer);
        verify(mapper).customerToResponseDto(customer);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1,0,99})
    void shouldNotFindCustomerById(int invalidId){
        when(cacheRepository.findById(invalidId)).thenReturn(Optional.empty());
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findCustomersById(invalidId));

        String expectedMessage = "Customer with id "+ invalidId + " not found";

        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        verify(cacheRepository).findById(invalidId);
        verify(repository).findById(invalidId);
    }

    @Test
    void shouldSaveNewCustomer(){
        Customer customer = customer1();
        CustomerDto dto = customerDto();
        CustomerCache cache = customerCache();
        CustomerResponseDto responseDto = responseDto();

        when(mapper.customerDtoToCustomer(dto)).thenReturn(customer);
        when(repository.save(customer)).thenReturn(customer);
        when(mapper.customerToCache(customer)).thenReturn(cache);
        when(cacheRepository.save(cache)).thenReturn(cache);
        when(mapper.customerToResponseDto(customer)).thenReturn(responseDto);

        CustomerResponseDto returned = service.saveCustomer(dto);

        assertThat(returned.state()).isEqualTo("RJ");
        assertThat(returned.name()).isEqualTo("Carlos");
        verify(mapper).customerDtoToCustomer(dto);
        verify(repository).save(customer);
        verify(mapper).customerToCache(customer);
        verify(cacheRepository).save(cache);
        verify(mapper).customerToResponseDto(customer);
    }

    @Test
    void shouldUpdateCustomer(){
        int id = 1;
        CustomerDto dto = customerDto();
        Customer oldCustomer = new Customer();
        oldCustomer.setCustomerId(1);

        when(repository.findById(id)).thenReturn(Optional.of(oldCustomer));
        when(repository.save(any())).thenReturn(oldCustomer);
        when(mapper.customerToResponseDto(oldCustomer))
                .thenAnswer(invocation -> {
                    Customer customer = invocation.getArgument(0);
                    return new CustomerResponseDto(customer.getName(),
                            customer.getPhone(),
                            customer.getState(),
                            customer.getCountry());
                });

        CustomerResponseDto returned = service.updateCustomer(dto,id);

        assertThat(returned.name()).isEqualTo("Carlos");
        assertThat(returned.state()).isEqualTo("RJ");
        assertThat(returned.phone()).isEqualTo("99888");
        assertThat(returned.country()).isEqualTo("Brazil");
        verify(repository).findById(id);
        verify(repository).save(any());
        verify(mapper).customerToResponseDto(oldCustomer);

    }

    @ParameterizedTest
    @ValueSource(ints = {-1,0,99})
    void shouldNotUpdateCustomerWhenInvalidId(int id){
        CustomerDto dto = customerDto();
        when(repository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.updateCustomer(dto,id));

        String expectedMessage = "Customer with id " + id + " not found";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        verify(repository).findById(id);

    }

    @Test
    void shouldDeleteCustomerById(){

        when(repository.existsById(anyInt())).thenReturn(true);

        service.deleteCustomer(1);

        verify(repository).existsById(1);
        verify(repository).deleteById(1);
        verify(cacheRepository).deleteById(1);
    }

    @Test
    void shouldNotDeleteByIdWhenResourceNotFound(){
        when(repository.existsById(anyInt())).thenReturn(false);

        service.deleteCustomer(1);

        verify(repository).existsById(1);
        verify(cacheRepository).deleteById(1);
    }
}
