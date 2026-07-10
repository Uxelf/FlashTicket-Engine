package com.uxelf.dev.ticketDealer.component;

import com.uxelf.dev.ticketDealer.entity.Room;
import com.uxelf.dev.ticketDealer.entity.Seat;
import com.uxelf.dev.ticketDealer.repository.RoomRepository;
import com.uxelf.dev.ticketDealer.repository.SeatRepository;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Testcontainers
public class DataInitializerTest {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private DataInitializer dataInitializer;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private SeatRepository seatRepository;

    @Container
    final private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @Test
    void whenAppStarts_thenRoomsAreCreated() throws Exception{
        List<Integer> numbers = appConfig.getRooms()
                .stream().map(AppConfig.RoomProperties::getNumber).toList();

        for (int num : numbers){
            boolean roomExists = roomRepository.existsByNumber(num);
            Assertions.assertTrue(roomExists);
        }
    }

    @Test
    void whenAppStartsTwice_thenRoomsAreNotDuplicated() throws Exception{
        List<Integer> numbers = appConfig.getRooms()
                .stream().map(AppConfig.RoomProperties::getNumber).toList();

        dataInitializer.run();

        for (int num : numbers){
            Assertions.assertEquals(1, roomRepository.findAllByNumber(num).size());
        }
    }

    @Test
    void testSeatCreation() throws Exception{
        List<AppConfig.RoomProperties> roomPropertiesList = appConfig.getRooms();
        for (AppConfig.RoomProperties roomProperty : roomPropertiesList){
            Room room = roomRepository.findByNumber(roomProperty.getNumber());
            Assertions.assertNotNull(room);

            if (roomProperty.getRows() != 0 && roomProperty.getSeatsPerRow() != 0)
                Assertions.assertFalse(seatRepository.findAllByRoomId(room.getId()).isEmpty());
        }
    }

    @Test
    void testSeatCreationNoDuplicates() throws Exception{
        List<AppConfig.RoomProperties> roomPropertiesList = appConfig.getRooms();
        List<Integer> seatsPerRoom = new ArrayList<>();

        for (AppConfig.RoomProperties roomProperty : roomPropertiesList){
            Room room = roomRepository.findByNumber(roomProperty.getNumber());
            Assertions.assertNotNull(room);
            seatsPerRoom.add(seatRepository.findAllByRoomId(room.getId()).size());
        }

        dataInitializer.run();

        int i = 0;
        for (AppConfig.RoomProperties roomProperty : roomPropertiesList){
            Room room = roomRepository.findByNumber(roomProperty.getNumber());
            Assertions.assertNotNull(room);
            Assertions.assertEquals(
                    seatsPerRoom.get(i),
                    seatRepository.findAllByRoomId(room.getId()).size()
            );
            i++;
        }
    }

}
