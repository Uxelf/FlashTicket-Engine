package com.uxelf.dev.ticketDealer.dto.reservation;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ReservationsListResponse {
    private List<ReservationData> reservationDataList;

    @Getter
    @Setter
    public static class ReservationData{
        private UUID reservationId;
        private LocalDateTime expiresAt;
        private boolean confirmed;
    }
}
