package com.tcc.service.customerService;


import com.tcc.dtos.CustomerDto;
import com.tcc.dtos.CustomerResponseDto;
import com.tcc.entity.Customer;
import com.tcc.entity.CustomerCache;
import com.tcc.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public CustomerResponseDto customerToResponseDto(Customer customer){
        return new CustomerResponseDto(customer.getName(), customer.getPhone(), customer.getState(), customer.getCountry());
    }
    public CustomerCache customerToCache(Customer customer){
        CustomerCache cache = new CustomerCache();
        cache.setCustomerId(customer.getCustomerId());
        cache.setAddress(customer.getAddress());
        cache.setCity(customer.getCity());
        cache.setName(customer.getName());
        cache.setCountry(customer.getCountry());
        cache.setPhone(customer.getPhone());
        cache.setPostalCode(customer.getPostalCode());
        cache.setState(customer.getState());
        cache.setSalesRepEmployee(customer.getSalesRepEmployee().getEmployeeId());
        return cache;
    }
    public CustomerResponseDto cacheToCustomerResponseDto(CustomerCache cache){
        return new CustomerResponseDto(cache.getName(), cache.getPhone(), cache.getState(), cache.getCountry());
    }

    public Customer customerDtoToCustomer(CustomerDto dto){
        Customer customer = new Customer();
        Employee employee = new Employee();
        employee.setEmployeeId(dto.salesRepEmployee());
        customer.setSalesRepEmployee(employee);
        customer.setName(dto.name());
        customer.setCountry(dto.country());
        customer.setPhone(dto.phone());
        customer.setState(dto.state());
        customer.setPostalCode(dto.postalCode());
        return customer;
    }
    public Customer cacheToCustomer(CustomerCache cache){
        Customer c = new Customer();
        c.setPostalCode(cache.getPostalCode());
        c.setState(cache.getState());
        c.setName(cache.getName());
        c.setCountry(cache.getCountry());
        Employee employee = new Employee();
        employee.setEmployeeId(cache.getSalesRepEmployee());
        c.setSalesRepEmployee(employee);
        c.setCustomerId(cache.getCustomerId());
        c.setCity(cache.getCity());
        c.setAddress(cache.getAddress());
        c.setPhone(cache.getPhone());
        return c;
    }
}
