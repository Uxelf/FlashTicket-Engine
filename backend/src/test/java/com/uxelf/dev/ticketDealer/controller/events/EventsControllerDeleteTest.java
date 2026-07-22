package com.uxelf.dev.ticketDealer.controller.events;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxelf.dev.ticketDealer.component.AppConfig;
import com.uxelf.dev.ticketDealer.dto.event.EventRequest;
import com.uxelf.dev.ticketDealer.entity.Event;
import com.uxelf.dev.ticketDealer.repository.EventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
public class EventsControllerDeleteTest {

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

    @Test
    void whenEventExists_thenReturnOk() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2050-12-06T10:00:00";
        String end_time = "2050-12-06T10:15:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        Optional<Event> eventOptional = eventRepository.findAll().stream().findFirst();
        Assertions.assertTrue(eventOptional.isPresent());

        Event event = eventOptional.get();

        mockMvc.perform(delete("/api/events/" + event.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void whenEventIdIsNotValid_thenReturnBadRequest() throws Exception{
        mockMvc.perform(delete("/api/events/94118586-60ce-4a8e-9d48"))
                .andExpect(status().isBadRequest());
    }
}
