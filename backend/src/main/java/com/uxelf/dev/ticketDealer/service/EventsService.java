package com.uxelf.dev.ticketDealer.service;

import com.uxelf.dev.ticketDealer.dto.event.EventRequest;
import com.uxelf.dev.ticketDealer.entity.Event;
import com.uxelf.dev.ticketDealer.entity.EventSeat;
import com.uxelf.dev.ticketDealer.entity.Room;
import com.uxelf.dev.ticketDealer.entity.Seat;
import com.uxelf.dev.ticketDealer.repository.EventRepository;
import com.uxelf.dev.ticketDealer.repository.EventSeatRepository;
import com.uxelf.dev.ticketDealer.repository.RoomRepository;
import com.uxelf.dev.ticketDealer.repository.SeatRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Getter
@AllArgsConstructor
@Service
public class EventsService {

    private RoomRepository roomRepository;
    private EventRepository eventRepository;
    private SeatRepository seatRepository;
    private EventSeatRepository eventSeatRepository;

    public List<Event> getEvents(){
        return eventRepository.findAll();
    }

    public Event createEvent(EventRequest request) throws Exception{

        Room room = roomRepository.findByNumber(request.getRoom_number());
        if (room == null){
            throw new RuntimeException("Room does not exists");
        }
        if (!eventRepository.findOverlappingEvents(
                request.getRoom_number(),
                request.getStart(),
                request.getEnd()).isEmpty())
            throw new RuntimeException("An event is already happening in that timezone");

        Event event = createEvent(request, room);
        createEventSeats(event);

        return event;
    }

    private Event createEvent(EventRequest request, Room room) {
        Event event = new Event();
        event.setRoom(room);
        event.setEnd_time(request.getEnd());
        event.setStart_time(request.getStart());
        eventRepository.save(event);
        return event;
    }

    private void createEventSeats(Event event){
        List<Seat> seatList = seatRepository.findAllByRoomNumber(event.getRoom().getNumber());
        List<EventSeat> eventSeatList = new ArrayList<>();

        for (Seat seat : seatList){
            EventSeat eventSeat = new EventSeat();
            eventSeat.setEvent(event);
            eventSeat.setSeat(seat);
            eventSeatList.add(eventSeat);
        }

        eventSeatRepository.saveAll(eventSeatList);
    }

    public boolean deleteEvent(UUID id){

        if (id == null)
            return false;

        Optional<Event> event = eventRepository.findById(id);
        if (event.isEmpty())
            return false;

        eventRepository.deleteById(event.get().getId());
        return true;
    }

}
