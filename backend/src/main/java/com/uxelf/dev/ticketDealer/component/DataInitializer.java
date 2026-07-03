package com.uxelf.dev.ticketDealer.component;

import com.uxelf.dev.ticketDealer.entity.Room;
import com.uxelf.dev.ticketDealer.repository.RoomRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final AppConfig appConfig;

    @Override
    public void run(String... args){

        List<Integer> roomNumbers = appConfig.getRooms()
                .stream().map(AppConfig.RoomProperties::getNumber).toList();

        for (int num : roomNumbers){
            if (!roomRepository.existsByNumber(num)){
                Room newRoom = new Room();
                newRoom.setNumber(num);
                roomRepository.save(newRoom);
            }
        }
    }
}
