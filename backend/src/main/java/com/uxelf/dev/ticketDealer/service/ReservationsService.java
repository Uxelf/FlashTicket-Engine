package com.uxelf.dev.ticketDealer.service;

import com.uxelf.dev.ticketDealer.entity.*;
import com.uxelf.dev.ticketDealer.enums.SeatStatus;
import com.uxelf.dev.ticketDealer.exception.AppBadRequestException;
import com.uxelf.dev.ticketDealer.exception.AppConflictRequestException;
import com.uxelf.dev.ticketDealer.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReservationsService {

    private EventRepository eventRepository;
    private UserRepository userRepository;
    private EventSeatRepository eventSeatRepository;
    private ReservationRepository reservationRepository;

    public Reservation reserveSeat(UUID eventId, String username, int seatRow, int seatNumber){

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!eventRepository.existsById(eventId)){
            throw new AppBadRequestException("Event does not exists");
        }
        if (userOptional.isEmpty()){
            throw new AppBadRequestException("Username does not exists");
        }

        Optional<EventSeat> eventSeatOptional = eventSeatRepository.findByEventIdAndSeatRowAndSeatNumber(eventId, seatRow, seatNumber);
        if (eventSeatOptional.isEmpty()){
            throw new AppBadRequestException("Seat does not exists");
        }
        EventSeat eventSeat = eventSeatOptional.get();
        if (eventSeat.getSeatStatus() != SeatStatus.FREE){
            throw new AppConflictRequestException("Seat is not free");
        }

        eventSeat.setSeatStatus(SeatStatus.RESERVED);
        eventSeatRepository.save(eventSeat);

        Reservation reservation = new Reservation();
        reservation.setEventSeat(eventSeat);
        reservation.setUser(userOptional.get());
        reservationRepository.save(reservation);
        return reservation;
    }
}
