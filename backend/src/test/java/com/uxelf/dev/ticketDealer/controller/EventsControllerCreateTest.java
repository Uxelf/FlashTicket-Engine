package com.uxelf.dev.ticketDealer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxelf.dev.ticketDealer.component.AppConfig;
import com.uxelf.dev.ticketDealer.dto.EventRequest;
import com.uxelf.dev.ticketDealer.dto.EventResponse;
import com.uxelf.dev.ticketDealer.entity.EventSeat;
import com.uxelf.dev.ticketDealer.repository.EventSeatRepository;
import org.junit.jupiter.api.*;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
class EventsControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    EventSeatRepository eventSeatRepository;

    @Container
    final private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void init() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2050-12-06T10:00:00";
        String end_time = "2050-12-06T10:15:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Test
    void whenEventIsValid_thenReturnOk() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2060-12-06T14:00:00";
        String end_time = "2060-12-06T14:15:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        MvcResult result = mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.room.number").value(roomNumber))
                .andExpect(jsonPath("$.event.start_time").value(start_time))
                .andExpect(jsonPath("$.event.end_time").value(end_time))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        EventResponse response = objectMapper.readValue(json, EventResponse.class);

        int totalSeats = appConfig.getRooms().getFirst().getSeatsPerRow() * appConfig.getRooms().getFirst().getRows();
        List<EventSeat> eventSeatList = eventSeatRepository.findByEvent(response.getEvent());
        Assertions.assertEquals(totalSeats, eventSeatList.size());
    }

    @Test
    void testEventCreation_overlapsInside() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2050-12-06T10:01:00";
        String end_time = "2050-12-06T10:14:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void testEventCreation_overlapsStart() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2050-12-06T09:00:00";
        String end_time = "2050-12-06T10:14:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void testEventCreation_overlapsEnd() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2050-12-06T10:01:00";
        String end_time = "2050-12-06T10:16:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void testEventCreation_overlapsEncompassing() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2050-12-06T09:00:00";
        String end_time = "2050-12-06T11:00:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void testEventCreation_noOverlap() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2050-12-06T11:00:00";
        String end_time = "2050-12-06T11:10:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.room.number").value(roomNumber))
                .andExpect(jsonPath("$.event.start_time").value(start_time))
                .andExpect(jsonPath("$.event.end_time").value(end_time));
    }

    @Test
    void whenStartIsAfterEnd_thenReturnBadRequest() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2051-12-06T19:00:00";
        String end_time = "2051-12-06T11:00:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void whenRoomDoesNotExist_thenReturnBadRequest() throws Exception{
        int roomNumber = -2;
        String start_time = "2051-12-06T10:00:00";
        String end_time = "2051-12-06T11:00:00";
        EventRequest request = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void whenStartIsNull_thenReturnBadRequest() throws Exception{
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String end_time = "2051-12-06T11:00:00";
        EventRequest request = new EventRequest(
                roomNumber,
                null,
                LocalDateTime.parse(end_time));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
