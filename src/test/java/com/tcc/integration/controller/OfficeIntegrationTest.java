package com.tcc.integration.controller;


import com.tcc.dtos.OfficeDto;
import com.tcc.persistance.OfficeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
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
public class OfficeIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private OfficeRepository officeRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
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

    @BeforeEach
    void setUp(){
        officeRepository.deleteAll();
    }

    @Test
    void shouldCreateAndFetchOfficeSuccessfully(){
        OfficeDto dto = new OfficeDto("Escritorio 1","central@empresa.org","555-123");
        ResponseEntity<OfficeDto> createResponse = testRestTemplate.postForEntity(
                "http://localhost:"+port+"/public/api/offices",
                dto,
                OfficeDto.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(officeRepository.findAll()).hasSize(1);
        assertThat(officeRepository.findAll().getFirst().getEmail()).isEqualTo("central@empresa.org");

        ResponseEntity<OfficeDto[]> getResponse = testRestTemplate.getForEntity(
                "http://localhost:"+port+"/public/api/offices",
                OfficeDto[].class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotEmpty();
        assertThat(getResponse.getBody()[0].officeName()).isEqualTo("Escritorio 1");
    }

}
