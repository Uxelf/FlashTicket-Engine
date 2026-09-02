package com.uxelf.dev.ticketDealer.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReservationConfirmRequest {
    private String username;
    private UUID reservationId;
}
