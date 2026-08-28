package com.uxelf.dev.ticketDealer.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReservationRequest {
    private UUID eventId;
    private String username;
    private int seatRow;
    private int seatNumber;
}
