package com.uxelf.dev.ticketDealer.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxelf.dev.ticketDealer.component.AppConfig;
import com.uxelf.dev.ticketDealer.dto.ErrorResponse;
import com.uxelf.dev.ticketDealer.dto.EventRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppConfig appConfig;

    @Container
    final private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void whenRequestIsValid_thenReturnOk() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2060-12-06T14:00:00";
        String end_time = "2060-12-06T14:15:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void whenRequestIsNotValid_thenReturnBadRequest() throws Exception{
        MvcResult result = mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                            "room_number": 3,
                            "start" : "bad",
                            "end" : "2060-12-06T14:15:00"
                            }
                        """))
                        .andExpect(status().isBadRequest())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ErrorResponse response = objectMapper.readValue(json, ErrorResponse.class);

        Assertions.assertFalse(response.getErrors().isEmpty());
        Assertions.assertEquals(1, response.getErrors().size());
    }
}
