package com.tcc.integration.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.dtos.EmployeeDto;
import com.tcc.persistance.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmployeeIntegrationH2Test {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        repository.deleteAll();
    }

    @Test
    void shouldCreateAndUpdateSuccessfully() throws  Exception{
        EmployeeDto dto = new EmployeeDto("Ronaldo","555-111","ronaldo@email",1,1);
        EmployeeDto updatedDto = new EmployeeDto("Anderson","333-222","anderson@email",1,1);
        mockMvc.perform(post("/public/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("Ronaldo"))
                .andExpect(jsonPath("$.email").value("ronaldo@email"));
        assertThat(repository.findAll()).hasSize(1);
        int id = repository.findAll().getFirst().getEmployeeId();

        mockMvc.perform(put("/public/api/employees/{id}",id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("Anderson"))
                .andExpect(jsonPath("$.email").value("anderson@email"));

        var employee = repository.findById(id).orElseThrow();

        assertThat(employee.getEmployeeName()).isEqualTo("Anderson");
        assertThat(employee.getEmail()).isEqualTo("anderson@email");

    }

    @Test
    void shouldCreateAndFetchSuccessfully() throws Exception{
        EmployeeDto dto = new EmployeeDto("Ronaldo","555-111","ronaldo@email",1,1);
        mockMvc.perform(post("/public/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("Ronaldo"))
                .andExpect(jsonPath("$.email").value("ronaldo@email"));

        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findAll().getFirst().getEmployeeName()).isEqualTo("Ronaldo");
        int id = repository.findAll().getFirst().getEmployeeId();

        mockMvc.perform(get("public/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("Ronaldo"))
                .andExpect(jsonPath("$[0].email").value("ronaldo@email"));

        mockMvc.perform(get("public/api/employees/{id}",id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("Ronaldo"))
                .andExpect(jsonPath("$.email").value("ronaldo@email"));
    }


}
