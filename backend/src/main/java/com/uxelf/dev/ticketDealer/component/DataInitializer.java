package com.uxelf.dev.ticketDealer.component;

import com.uxelf.dev.ticketDealer.entity.Room;
import com.uxelf.dev.ticketDealer.entity.Seat;
import com.uxelf.dev.ticketDealer.repository.RoomRepository;
import com.uxelf.dev.ticketDealer.repository.SeatRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppConfig appConfig;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;

    @Override
    public void run(String... args){
        createRooms();
        createSeats();

    }

    private void createRooms() {
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


    private void createSeats() {
        List<AppConfig.RoomProperties> configRooms = appConfig.getRooms();
        for (AppConfig.RoomProperties configRoom : configRooms ){
            Room room = roomRepository.findByNumber(configRoom.getNumber());
            if (room != null){
                if (seatRepository.findAllByRoomId(room.getId()).isEmpty()){
                    int rows = configRoom.getRows();
                    int seatsPerRow = configRoom.getSeatsPerRow();
                    List<Seat> seatList = new ArrayList<>();
                    for (int r = 0; r < rows; r++){
                        for (int s = 0; s < seatsPerRow; s++){
                            Seat newSeat = new Seat();
                            newSeat.setRoom(room);
                            newSeat.setRow(r + 1);
                            newSeat.setNumber(s + 1);
                            seatList.add(newSeat);
                        }
                    }
                    seatRepository.saveAll(seatList);
                }
            }
        }
    }
}
