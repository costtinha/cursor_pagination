package com.tcc.integration.controller;

import com.tcc.dtos.CustomerDto;
import com.tcc.persistance.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CustomerIntegrationH2Test {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp(){
        repository.deleteAll();
    }

    @Test
    void shouldCreateAndFetch() throws Exception{
        CustomerDto dto = new CustomerDto(1,"diego","555-333","rj","999","Brasil");
        mockMvc.perform(post("/public/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("diego"))
                .andExpect(jsonPath("$.state").value("rj"));

        assertThat(repository.findAll().getFirst().getName()).isEqualTo("diego");
        assertThat(repository.findAll().getFirst().getName()).isEqualTo("rj");

        mockMvc.perform(get("/public/api/customers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("diego"))
                .andExpect(jsonPath("$[0].state").value("rj"));
    }

    @Test
    void shouldFindByIdAndUpdate() throws Exception{
        CustomerDto dto = new CustomerDto(1,"diego","555-333","rj","999","Brasil");
        CustomerDto newDto = new CustomerDto(1,"mario","777-888","sp","111","Brasil");
        mockMvc.perform(post("/public/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("diego"))
                .andExpect(jsonPath("$.state").value("rj"));
        int id = repository.findAll().getFirst().getCustomerId();
        mockMvc.perform(put("/public/api/customers/{id}",id)
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("mario"))
                .andExpect(jsonPath("$.state").value("sp"));

        mockMvc.perform(get("/public/api/customer/{id}",id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("mario"))
                .andExpect(jsonPath("$.state").value("sp"));
    }
}
