package com.tcc.service.employeeService;


import com.tcc.dtos.EmployeeDto;
import com.tcc.dtos.EmployeeResponseDto;
import com.tcc.entity.Customer;
import com.tcc.entity.Employee;
import com.tcc.entity.EmployeeCache;
import com.tcc.entity.Office;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.stream.Collectors;


@Component
public class EmployeeMapper {
    public EmployeeResponseDto employeeToResponseDto(Employee employee) {
        return new EmployeeResponseDto(employee.getEmployeeName(),
                employee.getPhone(),employee.getEmail(),
                employee.getOffice().getOfficeId());

    }

    public Employee dtoToEmployee(EmployeeDto dto){
        Employee employee = new Employee();
        employee.setEmployeeName(dto.employeeName());
        employee.setEmail(dto.email());
        Office office = new Office();
        office.setOfficeId(dto.office());
        employee.setOffice(office);
        employee.setPhone(dto.phone());
        Employee chef = new Employee();
        chef.setEmployeeId(dto.reportsTo());
        employee.setReportsTo(chef);
        employee.setEmployeeList(Collections.emptyList());
        employee.setCustomers(Collections.emptyList());
        return employee;
    }
    public EmployeeResponseDto cacheToEmployeeResponseDto(EmployeeCache cache){
        return new EmployeeResponseDto(cache.getEmployeeName(),cache.getPhone(), cache.getEmail(),cache.getOffice());
    }

    public EmployeeCache employeeToCache(Employee employee){
        EmployeeCache cache = new EmployeeCache();
        cache.setEmail(employee.getEmail());
        cache.setEmployeeId(employee.getEmployeeId());
        cache.setOffice(employee.getOffice().getOfficeId());
        cache.setEmployeeName(employee.getEmployeeName());
        cache.setPhone(employee.getPhone());
        cache.setReportsTo(employee.getReportsTo().getEmployeeId());
        cache.setSubordinary(employee.getEmployeeList() != null ?
                employee.getEmployeeList().stream()
                        .map(Employee::getEmployeeId)
                        .collect(Collectors.toList()) : Collections.emptyList());
        cache.setCustomerId(employee.getCustomers()
                .stream()
                .map(Customer::getCustomerId)
                .collect(Collectors.toList()));
        return cache;
    }

    public Employee cacheToEmployee(EmployeeCache cache){
        Employee employee = new Employee();
        employee.setEmployeeName(cache.getEmployeeName());
        employee.setPhone(cache.getPhone());
        Office office = new Office();
        office.setOfficeId(cache.getOffice());
        employee.setOffice(office);
        employee.setEmail(cache.getEmail());
        Employee reportsTo = new Employee();
        reportsTo.setEmployeeId(cache.getReportsTo());
        employee.setReportsTo(reportsTo);
        employee.setEmployeeList(cache.getSubordinary() != null ?
                cache.getSubordinary()
                        .stream()
                        .map(id ->{
                            Employee emp = new Employee();
                            emp.setEmployeeId(id);
                            return emp;
                        })
                        .collect(Collectors.toList()) : Collections.emptyList());
        employee.setCustomers(cache.getCustomerId() != null ?
                cache.getCustomerId()
                        .stream()
                        .map(id ->{
                            Customer customer = new Customer();
                            customer.setCustomerId(id);
                            return customer;
                        })
                        .collect(Collectors.toList()): Collections.emptyList());
        return employee;

    }

}
