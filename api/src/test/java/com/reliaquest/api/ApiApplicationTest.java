package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class ApiApplicationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void restTemplateBeanExists() {
        assertNotNull(restTemplate, "RestTemplate bean should be created by Spring context");
    }
}
