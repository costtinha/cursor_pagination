package com.tcc.integration.controller;

import com.tcc.dtos.EmployeeDto;
import com.tcc.dtos.EmployeeResponseDto;
import com.tcc.persistance.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class EmployeeIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmployeeRepository repository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testDb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url",postgres::getJdbcUrl);
        registry.add("spring.datasource.username",postgres::getUsername);
        registry.add("spring.datasource.password",postgres::getPassword);
        registry.add("spring.data.redis.host",redis::getHost);
        registry.add("spring.data.redis.port",() -> redis.getMappedPort(6379));

    }

    @BeforeEach
    void setUp(){
        repository.deleteAll();
    }

    @Test
    void shouldCreateAndFetchSuccessFully(){
        EmployeeDto dto = new EmployeeDto("Ronaldo","555-111","ronaldo@email",1,1);

        ResponseEntity<EmployeeResponseDto> createResponse = restTemplate.postForEntity(
                "http://localhost:"+port+"/public/api/employees",
                dto,
                EmployeeResponseDto.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findAll().getFirst().getEmail()).isEqualTo("ronaldo@email");

        ResponseEntity<EmployeeResponseDto[]> getResponse = restTemplate.getForEntity(
                "http://localhost:"+port+"/public/api/employees",
                EmployeeResponseDto[].class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotEmpty();
        assertThat(getResponse.getBody()[0].employeeName()).isEqualTo("Ronaldo");

    }

    @Test
    void shouldFindByIdAndUpdate(){
        EmployeeDto dto = new EmployeeDto("Ronaldo","555-111","ronaldo@email",1,1);
        EmployeeDto updatedDto = new EmployeeDto("Anderson","333-222","anderson@email",1,1);
        int id = 1;

        ResponseEntity<EmployeeResponseDto> createResponse = restTemplate.postForEntity(
                "http://localhost:"+port+"/public/api/employees",
                dto,
                EmployeeResponseDto.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findAll().getFirst().getEmployeeName()).isEqualTo("Ronaldo");

        ResponseEntity<EmployeeResponseDto> updateResponse = restTemplate.exchange(
                "http://localhost:"+port+"/public/api/employees/"+id,
                HttpMethod.PUT,
                new HttpEntity<>(updatedDto),
                EmployeeResponseDto.class

        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().employeeName()).isEqualTo("Anderson");

        var employee = repository.findById(id).orElseThrow();
        assertThat(employee.getEmployeeName()).isEqualTo("Anderson");
        assertThat(employee.getEmail()).isEqualTo("anderson@email");
    }
}
