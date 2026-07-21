package com.uxelf.dev.ticketDealer.dto;

import com.uxelf.dev.ticketDealer.entity.Event;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EventListResponse {
    List<Event> events;
}
