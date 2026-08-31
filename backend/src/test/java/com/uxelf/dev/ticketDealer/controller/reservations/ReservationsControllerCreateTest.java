package com.uxelf.dev.ticketDealer.controller.reservations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxelf.dev.ticketDealer.component.AppConfig;
import com.uxelf.dev.ticketDealer.dto.event.EventRequest;
import com.uxelf.dev.ticketDealer.dto.event.EventResponse;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationRequest;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationResponse;
import com.uxelf.dev.ticketDealer.dto.user.UserRequest;
import com.uxelf.dev.ticketDealer.entity.Event;
import com.uxelf.dev.ticketDealer.entity.EventSeat;
import com.uxelf.dev.ticketDealer.entity.Reservation;
import com.uxelf.dev.ticketDealer.enums.SeatStatus;
import com.uxelf.dev.ticketDealer.repository.EventSeatRepository;
import com.uxelf.dev.ticketDealer.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
public class ReservationsControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppConfig appConfig;

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
    void whenReservationIsValid_thenReturnOk() throws Exception{
        AppConfig.RoomProperties roomProperties = appConfig.getRooms().stream().toList().getFirst();
        Assertions.assertTrue(roomProperties.getRows() > 0 && roomProperties.getSeatsPerRow() > 0);

        ReservationRequest request = new ReservationRequest(
                mainEventId, username, 1, 1
        );

        MvcResult result = mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        ReservationResponse response = objectMapper.readValue(json, ReservationResponse.class);

        List<Reservation> reservationList = reservationRepository.findAll();
        Assertions.assertFalse(reservationList.isEmpty());

        Reservation dbReservation = reservationList.getFirst();
        Assertions.assertEquals(dbReservation.getId(), response.getReservationId());
        Assertions.assertEquals(username, dbReservation.getUser().getUsername());
        Assertions.assertEquals(SeatStatus.RESERVED, dbReservation.getEventSeat().getSeatStatus());
    }

    @Test
    void whenReservationIsNotValid_thenReturnBadRequest() throws Exception{
        AppConfig.RoomProperties roomProperties = appConfig.getRooms().stream().toList().getFirst();
        Assertions.assertTrue(roomProperties.getRows() > 0 && roomProperties.getSeatsPerRow() > 0);

        ReservationRequest request = new ReservationRequest(
                mainEventId, username, 1, 1
        );

        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ReservationRequest invalidEventIdRequest = new ReservationRequest(
                UUID.randomUUID(), username, 1, 1
        );
        ReservationRequest invalidUsernameRequest = new ReservationRequest(
                mainEventId, "%s%s".formatted(username, "bad"), 1, 1
        );
        ReservationRequest invalidRowSeatRequest = new ReservationRequest(
                mainEventId, username, roomProperties.getRows() + 2, 1
        );
        ReservationRequest invalidNumberSeatRequest = new ReservationRequest(
                mainEventId, username, 1, roomProperties.getSeatsPerRow() + 2
        );
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEventIdRequest)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUsernameRequest)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRowSeatRequest)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNumberSeatRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenSeatIsTaken_thenReturnConflict() throws Exception{
        AppConfig.RoomProperties roomProperties = appConfig.getRooms().stream().toList().getFirst();
        Assertions.assertTrue(roomProperties.getRows() > 0 && roomProperties.getSeatsPerRow() > 0);

        ReservationRequest request = new ReservationRequest(
                mainEventId, username, 1, 1
        );

        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenMultipleSimultaneousReservations_thenOnlyOneWorks() throws Exception{
        int threadsCount = 1000;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadsCount);
        List<HttpStatus> requestResults = Collections.synchronizedList(new ArrayList<>());

        AppConfig.RoomProperties roomProperties = appConfig.getRooms().stream().toList().getFirst();
        Assertions.assertTrue(roomProperties.getRows() > 0 && roomProperties.getSeatsPerRow() > 0);
        ReservationRequest request = new ReservationRequest(
                mainEventId, username, 1, 1
        );

        try (ExecutorService executor = Executors.newFixedThreadPool(threadsCount)) {
            for (int i = 0; i < threadsCount; i++){
                executor.submit(new WaitingReserver(startLatch, endLatch, request, requestResults));
            }
            startLatch.countDown();
            endLatch.await();
        }
        List<Reservation> reservationList = reservationRepository.findAll();
        int okResults = requestResults.stream().filter(value -> value == HttpStatus.OK).toList().size();
        int conflictResults = requestResults.stream().filter(value -> value == HttpStatus.CONFLICT).toList().size();
        Assertions.assertEquals(1, reservationList.size());
        Assertions.assertEquals(threadsCount, requestResults.size());
        Assertions.assertEquals(1, okResults);
        Assertions.assertEquals(threadsCount - 1, conflictResults);

    }

    @AllArgsConstructor
    public class WaitingReserver implements Runnable{

        private CountDownLatch startLatch;
        private CountDownLatch endLatch;
        private ReservationRequest request;
        private List<HttpStatus> requestResults;


        @Override
        public void run() {
            try {
                startLatch.await();
                HttpStatus requestStatus = SendRequest();
                requestResults.add(requestStatus);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                endLatch.countDown();
            }
        }

        private HttpStatus SendRequest() throws Exception {
            int status = mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andReturn().getResponse().getStatus();
            return HttpStatus.valueOf(status);
        }
    }
}
