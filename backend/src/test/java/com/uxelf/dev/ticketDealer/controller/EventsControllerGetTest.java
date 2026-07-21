package com.uxelf.dev.ticketDealer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxelf.dev.ticketDealer.component.AppConfig;
import com.uxelf.dev.ticketDealer.dto.EventListResponse;
import com.uxelf.dev.ticketDealer.dto.EventRequest;
import com.uxelf.dev.ticketDealer.entity.Event;
import com.uxelf.dev.ticketDealer.repository.EventRepository;
import com.uxelf.dev.ticketDealer.repository.EventSeatRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
public class EventsControllerGetTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    EventRepository eventRepository;

    @Container
    final private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @BeforeEach
    void cleanDb(){
        eventRepository.deleteAll();
    }


    @Test
    void whenNoEvents_thenReturnEmptyList() throws Exception{
        MvcResult result = mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        EventListResponse response = objectMapper.readValue(json, EventListResponse.class);

        Assertions.assertTrue(response.getEvents().isEmpty());
    }

    @Test
    void whenEvents_thenReturnAllEventsList() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time_1 = "2050-12-06T10:00:00";
        String end_time_1 = "2050-12-06T10:15:00";
        String start_time_2 = "2051-12-06T10:00:00";
        String end_time_2 = "2051-12-06T10:15:00";
        String start_time_3 = "2052-12-06T10:00:00";
        String end_time_3 = "2052-12-06T10:15:00";

        EventRequest request1 = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time_1),
                LocalDateTime.parse(end_time_1));
        EventRequest request2 = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time_2),
                LocalDateTime.parse(end_time_2));
        EventRequest request3 = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time_3),
                LocalDateTime.parse(end_time_3));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)));

        MvcResult result = mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        EventListResponse response = objectMapper.readValue(json, EventListResponse.class);

        Assertions.assertEquals(3, response.getEvents().size());
    }
}
