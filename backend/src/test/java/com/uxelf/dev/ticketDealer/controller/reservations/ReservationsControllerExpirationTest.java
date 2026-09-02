package com.uxelf.dev.ticketDealer.controller.reservations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxelf.dev.ticketDealer.component.AppConfig;
import com.uxelf.dev.ticketDealer.dto.event.EventRequest;
import com.uxelf.dev.ticketDealer.dto.event.EventResponse;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationConfirmRequest;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationRequest;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationResponse;
import com.uxelf.dev.ticketDealer.dto.user.UserRequest;
import com.uxelf.dev.ticketDealer.entity.EventSeat;
import com.uxelf.dev.ticketDealer.entity.Reservation;
import com.uxelf.dev.ticketDealer.enums.SeatStatus;
import com.uxelf.dev.ticketDealer.repository.EventSeatRepository;
import com.uxelf.dev.ticketDealer.repository.ReservationRepository;
import com.uxelf.dev.ticketDealer.scheduler.ReservationExpirationJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = "app.reservationExpirationMinutes=0")
public class ReservationsControllerExpirationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    ReservationExpirationJob reservationExpirationJob;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private EventSeatRepository eventSeatRepository;

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

    }

    @BeforeEach
    void cleanDb() {
        List<Reservation> reservationList = reservationRepository.findAll();
        for (Reservation reservation : reservationList){
            EventSeat eventSeat = reservation.getEventSeat();
            eventSeat.setSeatStatus(SeatStatus.FREE);
            eventSeatRepository.save(eventSeat);
        }

        reservationRepository.deleteAll();
    }

    @Test
    void whenReservationIsConfirmed_thenReservationIsNotRemoved() throws Exception{
        AppConfig.RoomProperties roomProperties = appConfig.getRooms().stream().toList().getFirst();
        Assertions.assertTrue(roomProperties.getRows() > 0 && roomProperties.getSeatsPerRow() > 0);
        ReservationRequest reservationRequest = new ReservationRequest(
                mainEventId, username, 1, 1
        );
        MvcResult reservationResult = mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk()).andReturn();
        String json = reservationResult.getResponse().getContentAsString();
        ReservationResponse reservationResponse = objectMapper.readValue(json, ReservationResponse.class);

        ReservationConfirmRequest reservationConfirmRequest = new ReservationConfirmRequest(username, reservationResponse.getReservationId());
        mockMvc.perform(post("/api/reservations/confirm").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationConfirmRequest)))
                .andExpect(status().isOk());

        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationResponse.getReservationId());
        Assertions.assertTrue(reservationOptional.isPresent());

        reservationExpirationJob.expireReservations();
        Optional<Reservation> reservationOptionalAfterExpiration = reservationRepository.findById(reservationResponse.getReservationId());
        Assertions.assertTrue(reservationOptionalAfterExpiration.isPresent());
    }

    @Test
    void whenReservationIsNotConfirmed_thenReservationIsRemoved() throws Exception{
        AppConfig.RoomProperties roomProperties = appConfig.getRooms().stream().toList().getFirst();
        Assertions.assertTrue(roomProperties.getRows() > 0 && roomProperties.getSeatsPerRow() > 0);
        ReservationRequest reservationRequest = new ReservationRequest(
                mainEventId, username, 1, 1
        );
        MvcResult reservationResult = mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk()).andReturn();
        String json = reservationResult.getResponse().getContentAsString();
        ReservationResponse reservationResponse = objectMapper.readValue(json, ReservationResponse.class);

        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationResponse.getReservationId());
        Assertions.assertTrue(reservationOptional.isPresent());

        reservationExpirationJob.expireReservations();
        Optional<Reservation> reservationOptionalAfterExpiration = reservationRepository.findById(reservationResponse.getReservationId());
        Assertions.assertTrue(reservationOptionalAfterExpiration.isEmpty());
    }
}
