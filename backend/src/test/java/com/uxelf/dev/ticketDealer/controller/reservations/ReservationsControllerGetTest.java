package com.uxelf.dev.ticketDealer.controller.reservations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxelf.dev.ticketDealer.component.AppConfig;
import com.uxelf.dev.ticketDealer.dto.event.EventRequest;
import com.uxelf.dev.ticketDealer.dto.event.EventResponse;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationRequest;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationsListResponse;
import com.uxelf.dev.ticketDealer.dto.user.UserRequest;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
public class ReservationsControllerGetTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppConfig appConfig;

    private static UUID mainEventId;
    private final String username = "Dummy";

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

        /// Skip if already initialized
        if (mainEventId != null)
            return;

        /// Create event
        int roomNumber = appConfig.getRooms().stream().toList().getFirst().getNumber();
        String start_time = "2060-12-06T14:00:00";
        String end_time = "2060-12-06T14:15:00";
        EventRequest eventRequest = new EventRequest(
                roomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));

        MvcResult result = mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        EventResponse response = objectMapper.readValue(json, EventResponse.class);
        mainEventId = response.getEvent().getId();

        /// Create user
        UserRequest userRequest = new UserRequest(username);
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());


        ///  Reserve seats
        AppConfig.RoomProperties roomProperties = appConfig.getRooms().stream().toList().getFirst();
        Assertions.assertTrue(roomProperties.getRows() > 0 && roomProperties.getSeatsPerRow() > 0);

        for (int i = 0; i < roomProperties.getSeatsPerRow(); i++) {
            ReservationRequest request = new ReservationRequest(
                    mainEventId, username, 1, i + 1
            );
            mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void whenUsernameIsValid_thenReturnReservationsList() throws Exception{
        AppConfig.RoomProperties roomProperties = appConfig.getRooms().stream().toList().getFirst();

        MvcResult result = mockMvc.perform(get("/api/reservations/%s".formatted(username)))
                .andExpect(status().isOk()).andReturn();
        String json = result.getResponse().getContentAsString();
        ReservationsListResponse response = objectMapper.readValue(json, ReservationsListResponse.class);

        Assertions.assertEquals(roomProperties.getSeatsPerRow(), response.getReservationDataList().size());
    }

    @Test
    void whenUsernameIsNotValid_thenReturnBadRequest() throws Exception{
        AppConfig.RoomProperties roomProperties = appConfig.getRooms().stream().toList().getFirst();

        mockMvc.perform(get("/api/reservations/%s%s".formatted(username, "bad")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenUserHasNoReservations_thenReturnEmptyList() throws Exception{
        String newUsername = "%s%s".formatted(username, "New");
        UserRequest userRequest = new UserRequest(newUsername);
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/reservations/%s".formatted(newUsername)))
                .andExpect(status().isOk()).andReturn();
        String json = result.getResponse().getContentAsString();
        ReservationsListResponse response = objectMapper.readValue(json, ReservationsListResponse.class);

        Assertions.assertEquals(0, response.getReservationDataList().size());
    }
}
