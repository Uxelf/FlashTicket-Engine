package com.uxelf.dev.ticketDealer.dto.reservation;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReservationResponse {
    private UUID reservationId;
    private String username;
    private LocalDateTime expiresAt;
}
