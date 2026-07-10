package com.uxelf.dev.ticketDealer.dto;

import com.uxelf.dev.ticketDealer.entity.Event;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EventResponse {
    private String message;
    private String error;
    private Event event;
}
