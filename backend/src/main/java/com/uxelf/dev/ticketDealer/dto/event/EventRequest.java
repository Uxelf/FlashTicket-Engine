package com.uxelf.dev.ticketDealer.dto.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EventRequest {
    @NotNull
    private int room_number;

    @Future (message = "start has to be a future date")
    @NotNull
    private LocalDateTime start;

    @Future (message = "end has to be a future date")
    @NotNull
    private LocalDateTime end;
}
