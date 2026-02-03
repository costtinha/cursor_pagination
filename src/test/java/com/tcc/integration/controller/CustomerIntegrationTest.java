package com.tcc.integration.controller;

import com.tcc.dtos.CustomerDto;
import com.tcc.dtos.CustomerResponseDto;
import com.tcc.persistance.CustomerRepository;
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
public class CustomerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testDb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url",postgres::getJdbcUrl);
        registry.add("spring.datasource.username",postgres::getUsername);
        registry.add("spring.datasource.password",postgres::getPassword);
        registry.add("spring.data.redis.host",redis::getHost);
        registry.add("spring.data.redis.port",() -> redis.getMappedPort(6379));
    }

    @Test
    void shouldCreateAndFetch(){
        CustomerDto dto = new CustomerDto(1,"diego","555-333","rj","999","Brasil");
        ResponseEntity<CustomerResponseDto> createResponse = restTemplate.postForEntity(
                "http://localhost:"+port+"/public/api/customers",
                dto,
                CustomerResponseDto.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findAll().getFirst().getName()).isEqualTo("diego");

        ResponseEntity<CustomerResponseDto[]> getResponse = restTemplate.getForEntity(
                "http://localhost:"+port+"/public/api/customers",
                CustomerResponseDto[].class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotEmpty();
        assertThat(getResponse.getBody()[0].name()).isEqualTo("diego");
    }

    @Test
    void shouldFindByIdAndUpdate(){
        CustomerDto dto = new CustomerDto(1,"diego","555-333","rj","999","Brasil");
        CustomerDto newDto = new CustomerDto(1,"mario","777-888","sp","111","Brasil");
        ResponseEntity<CustomerResponseDto> postResponse = restTemplate.postForEntity(
                "http://localhost:"+port+"/public/api/customers",
                dto,
                CustomerResponseDto.class
        );
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(repository.findAll().getFirst().getName()).isEqualTo("diego");
        int id = repository.findAll().getFirst().getCustomerId();

        ResponseEntity<CustomerResponseDto> putResponse = restTemplate.exchange(
                "http://localhost:"+port+"/public/api/customers/"+id,
                HttpMethod.PUT,
                new HttpEntity<>(newDto),
                CustomerResponseDto.class
        );

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isNotNull();
        assertThat(putResponse.getBody().name()).isEqualTo("mario");
        var customer = repository.findById(id).orElseThrow();
        assertThat(customer.getName()).isEqualTo("mario");
        assertThat(customer.getState()).isEqualTo("sp");

    }
}
