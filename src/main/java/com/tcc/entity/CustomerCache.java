package com.tcc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "Customer",timeToLive = 3600)
public class CustomerCache {

    @Id
    private Integer customerId;
    private int salesRepEmployee;
    private String name;
    private String phone;
    private String address;
    private String city;
    private String State;
    private String postalCode;
    private String Country;

    public CustomerCache() {
    }

    public CustomerCache(Integer customerId, int salesRepEmployee, String name, String phone, String address, String city, String state, String postalCode, String country) {
        this.customerId = customerId;
        this.salesRepEmployee = salesRepEmployee;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.city = city;
        State = state;
        this.postalCode = postalCode;
        Country = country;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public int getSalesRepEmployee() {
        return salesRepEmployee;
    }

    public void setSalesRepEmployee(int salesRepEmployee) {
        this.salesRepEmployee = salesRepEmployee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }
}
