package com.uxelf.dev.ticketDealer.dto.event;

import com.uxelf.dev.ticketDealer.entity.Seat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class EventSeatsResponse {
    List<SeatData> seats;

    @Setter
    @Getter
    public static
    class SeatData{
        UUID id;
        int row;
        int number;
    }
}
