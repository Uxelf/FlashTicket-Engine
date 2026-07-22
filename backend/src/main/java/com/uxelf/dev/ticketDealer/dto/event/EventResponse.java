package com.uxelf.dev.ticketDealer.dto.event;

import com.uxelf.dev.ticketDealer.entity.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventResponse {
    private String message;
    private String error;
    private Event event;
}
