package com.tcc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash(value = "Office",timeToLive = 3600)
public class OfficeCache {
    @Id
    private int officeId;

    private String officeName;
    private String email;
    private String phone;

    private List<Integer> employees;

    public OfficeCache() {
    }

    public OfficeCache(int officeId, String officeName, String email, String phone, List<Integer> employees) {
        this.officeId = officeId;
        this.officeName = officeName;
        this.email = email;
        this.phone = phone;
        this.employees = employees;
    }

    public int getOfficeId() {
        return officeId;
    }

    public void setOfficeId(int officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Integer> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Integer> employees) {
        this.employees = employees;
    }
}
