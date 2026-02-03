package com.tcc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash(value = "Employee",timeToLive = 3600)
public class EmployeeCache {
    @Id
    private Integer employeeId;
    private String employeeName;
    private String phone;
    private String email;
    private int reportsTo;
    private List<Integer> subordinary;
    private int office;
    private List<Integer> customerId;

    public EmployeeCache() {
    }

    public EmployeeCache(Integer employeeId, String employeeName, String phone, String email, int reportsTo, List<Integer> subordinary, int office, List<Integer> customerId) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.phone = phone;
        this.email = email;
        this.reportsTo = reportsTo;
        this.subordinary = subordinary;
        this.office = office;
        this.customerId = customerId;
    }

    public List<Integer> getCustomerId() {
        return customerId;
    }

    public void setCustomerId(List<Integer> customerId) {
        this.customerId = customerId;
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

    public int getReportsTo() {
        return reportsTo;
    }

    public void setReportsTo(int reportsTo) {
        this.reportsTo = reportsTo;
    }

    public List<Integer> getSubordinary() {
        return subordinary;
    }

    public void setSubordinary(List<Integer> subordinary) {
        this.subordinary = subordinary;
    }

    public int getOffice() {
        return office;
    }

    public void setOffice(int office) {
        this.office = office;
    }
}
