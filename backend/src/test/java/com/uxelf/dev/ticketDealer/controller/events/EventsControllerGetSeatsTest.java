package com.uxelf.dev.ticketDealer.controller.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxelf.dev.ticketDealer.component.AppConfig;
import com.uxelf.dev.ticketDealer.dto.event.EventListResponse;
import com.uxelf.dev.ticketDealer.dto.event.EventRequest;
import com.uxelf.dev.ticketDealer.dto.event.EventResponse;
import com.uxelf.dev.ticketDealer.dto.event.EventSeatsResponse;
import com.uxelf.dev.ticketDealer.entity.Event;
import com.uxelf.dev.ticketDealer.entity.Room;
import com.uxelf.dev.ticketDealer.repository.EventRepository;
import com.uxelf.dev.ticketDealer.repository.RoomRepository;
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
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class EventsControllerGetSeatsTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    RoomRepository roomRepository;

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
    void whenEventDoesNotExist_thenReturnBadRequest() throws Exception{
        mockMvc.perform(get("/api/events/1234/seats"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenEventHasSeats_thenReturnSeatsList() throws Exception{
        //Create an event
        AppConfig.RoomProperties roomProperty = appConfig.getRooms().stream()
                .filter(roomProperties -> (roomProperties.getRows() * roomProperties.getSeatsPerRow() > 0))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There are no valid rooms, configure a room more than 0 seats"));

        String start_time = "2050-12-06T10:00:00";
        String end_time = "2050-12-06T10:15:00";
        EventRequest request = new EventRequest(
                roomProperty.getNumber(),
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));
        MvcResult eventsResult = mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String eventsJson = eventsResult.getResponse().getContentAsString();
        EventResponse eventResponse = objectMapper.readValue(eventsJson, EventResponse.class);

        //Get seats from that event
        UUID eventId = eventResponse.getEvent().getId();
        MvcResult seatsResult = mockMvc.perform(get("/api/events/%s/seats".formatted(eventId)))
                .andExpect(status().isOk())
                .andReturn();
        String seatsJson = seatsResult.getResponse().getContentAsString();
        EventSeatsResponse seatsResponse = objectMapper.readValue(seatsJson, EventSeatsResponse.class);
        int roomSeatsNumber = roomProperty.getRows() * roomProperty.getSeatsPerRow();
        int eventSeatsNumber = seatsResponse.getSeats().size();
        Assertions.assertEquals(roomSeatsNumber, eventSeatsNumber);
    }

    @Test
    void whenEventDoesNotHaveSeats_thenReturnEmptySeatsList() throws Exception{
        //Create new empty room
        int newRoomNumber = 0;
        List<Integer> roomsNumbers = appConfig.getRooms().stream().map(AppConfig.RoomProperties::getNumber).toList();
        while (roomsNumbers.contains(newRoomNumber)){
            newRoomNumber++;
        }

        Room newRoom = new Room();
        newRoom.setNumber(newRoomNumber);
        roomRepository.save(newRoom);

        //Create event for that room
        String start_time = "2050-12-06T10:00:00";
        String end_time = "2050-12-06T10:15:00";
        EventRequest request = new EventRequest(
                newRoomNumber,
                LocalDateTime.parse(start_time),
                LocalDateTime.parse(end_time));
        MvcResult eventsResult = mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();;

        String eventsJson = eventsResult.getResponse().getContentAsString();
        EventResponse eventResponse = objectMapper.readValue(eventsJson, EventResponse.class);

        //Get seats from that event
        UUID eventId = eventResponse.getEvent().getId();
        MvcResult seatsResult = mockMvc.perform(get("/api/events/%s/seats".formatted(eventId)))
                .andExpect(status().isOk())
                .andReturn();
        String seatsJson = seatsResult.getResponse().getContentAsString();
        EventSeatsResponse seatsResponse = objectMapper.readValue(seatsJson, EventSeatsResponse.class);
        int eventSeatsNumber = seatsResponse.getSeats().size();
        Assertions.assertEquals(0, eventSeatsNumber);
    }
}
