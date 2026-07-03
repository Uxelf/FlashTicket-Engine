package com.uxelf.dev.ticketDealer.component;

import com.uxelf.dev.ticketDealer.repository.RoomRepository;
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

import java.util.List;

@SpringBootTest
@Testcontainers
public class DataInitializerTest {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private DataInitializer dataInitializer;

    @Container
    final private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @Test
    void testRoomCreation() throws Exception{
        List<Integer> numbers = appConfig.getRooms()
                .stream().map(AppConfig.RoomProperties::getNumber).toList();

        for (int num : numbers){
            boolean roomExists = roomRepository.existsByNumber(num);
            Assertions.assertTrue(roomExists);
        }
    }

    @Test
    void testRoomCreationNoDuplicates() throws Exception{
        List<Integer> numbers = appConfig.getRooms()
                .stream().map(AppConfig.RoomProperties::getNumber).toList();

        dataInitializer.run();

        for (int num : numbers){
            Assertions.assertEquals(1, roomRepository.findAllByNumber(num).size());
        }
    }

}
