package com.tcc.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "office_entity")
public class Office {

    @Id
    @GeneratedValue
    private Integer officeId;

    @Column(nullable = false,length = 100)
    private String officeName;

    @Column(unique = true,nullable = false,insertable = false)
    private String email;

    private String phone;

    @OneToMany(mappedBy = "employeeId",fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Employee> employees;

    public Office(Integer officeId, String officeName, String email, String phone, List<Employee> employees) {
        this.officeId = officeId;
        this.officeName = officeName;
        this.email = email;
        this.phone = phone;
        this.employees = employees;
    }



    public Office() {
    }

    public Integer getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Integer officeId) {
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

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
