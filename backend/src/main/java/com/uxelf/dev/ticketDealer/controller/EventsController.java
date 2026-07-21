package com.uxelf.dev.ticketDealer.controller;

import com.uxelf.dev.ticketDealer.dto.EventListResponse;
import com.uxelf.dev.ticketDealer.dto.EventRequest;
import com.uxelf.dev.ticketDealer.dto.EventResponse;
import com.uxelf.dev.ticketDealer.entity.Event;
import com.uxelf.dev.ticketDealer.service.EventsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
public class EventsController {

    private EventsService eventsService;


    @GetMapping
    public ResponseEntity<EventListResponse> getEvents(){
        List<Event> eventList = eventsService.getEvents();
        EventListResponse eventListResponse = new EventListResponse();
        eventListResponse.setEvents(eventList);
        return ResponseEntity.ok(eventListResponse);
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody @Valid EventRequest request){

        EventResponse eventResponse = new EventResponse();
        try{
            if (request.getStart().isAfter(request.getEnd())
            || request.getStart().isEqual(request.getEnd()))
                throw new RuntimeException("Dates have to be in the future and can't be the same");
            eventResponse.setEvent(eventsService.createEvent(request));
        }
        catch (Exception e){
            eventResponse.setError(HttpStatus.BAD_REQUEST.toString());
            eventResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(eventResponse);
        }
        eventResponse.setMessage("Ok");
        return ResponseEntity.ok(eventResponse);
    }
}
