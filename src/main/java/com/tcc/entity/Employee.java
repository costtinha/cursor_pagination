package com.tcc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Employee {

    @Id
    @GeneratedValue
    private Integer employeeId;
    @Column(length = 100)
    private String employeeName;
    @Column(length = 20,unique = true)
    private String phone;
    @Column(unique = true)
    private String email;


    @OneToMany(mappedBy = "salesRepEmployee")
    @JsonManagedReference
    private List<Customer> customers;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "employee_supervisor")
    private Employee reportsTo;

    @OneToMany(mappedBy = "reportsTo")
    @JsonManagedReference
    private List<Employee> employeeList;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "office_id")
    private Office office;

    public Employee(Integer employeeId, String employeeName, String phone, String email, List<Customer> customers, Employee reportsTo, List<Employee> temporaryName, Office office) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.phone = phone;
        this.email = email;
        this.customers = customers;
        this.reportsTo = reportsTo;
        this.employeeList = temporaryName;
        this.office = office;
    }

    public Employee() {
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Employee getReportsTo() {
        return reportsTo;
    }

    public void setReportsTo(Employee reportsTo) {
        this.reportsTo = reportsTo;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }
}
