package com.uxelf.dev.ticketDealer.service;

import com.uxelf.dev.ticketDealer.component.AppConfig;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationConfirmRequest;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationConfirmResponse;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationResponse;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationsListResponse;
import com.uxelf.dev.ticketDealer.entity.*;
import com.uxelf.dev.ticketDealer.enums.SeatStatus;
import com.uxelf.dev.ticketDealer.exception.AppBadRequestException;
import com.uxelf.dev.ticketDealer.exception.AppConflictRequestException;
import com.uxelf.dev.ticketDealer.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationsService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventSeatRepository eventSeatRepository;
    private final ReservationRepository reservationRepository;

    @Value("${app.reservationExpirationMinutes}")
    private int expirationMinutes;

    public ReservationResponse reserveSeat(UUID eventId, String username, int seatRow, int seatNumber){

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
        try{
            eventSeatRepository.save(eventSeat);
        } catch (ObjectOptimisticLockingFailureException e){
            throw new AppConflictRequestException("Seat is not free");
        }

        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(expirationMinutes);

        Reservation reservation = new Reservation();
        reservation.setEventSeat(eventSeat);
        reservation.setUser(userOptional.get());
        reservation.setExpiresAt(expirationTime);
        reservationRepository.save(reservation);

        ReservationResponse response = new ReservationResponse();
        response.setReservationId(reservation.getId());
        response.setUsername(reservation.getUser().getUsername());
        response.setExpiresAt(reservation.getExpiresAt());
        return response;
    }

    public ReservationsListResponse getUserReservations(String username){
        if (!userRepository.existsByUsername(username)){
            throw new AppBadRequestException("Username does not exists");
        }

        List<Reservation> reservationList = reservationRepository.findAllByUserUsername(username);

        ReservationsListResponse response = new ReservationsListResponse();
        List<ReservationsListResponse.ReservationData> reservationDataList = new ArrayList<>();

        for (Reservation reservation : reservationList){
            ReservationsListResponse.ReservationData data = new ReservationsListResponse.ReservationData();
            data.setReservationId(reservation.getId());
            data.setExpiresAt(reservation.getExpiresAt());
            data.setConfirmed(reservation.isConfirmed());
            reservationDataList.add(data);
        }

        response.setReservationDataList(reservationDataList);
        return response;
    }

    @Transactional
    public ReservationConfirmResponse confirmReservation(String username, UUID reservationId){
        if (!userRepository.existsByUsername(username)){
            throw new AppBadRequestException("Username does not exists");
        }

        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationId);
        if (reservationOptional.isEmpty()){
            throw new AppBadRequestException("Reservation does not exists");
        }

        Reservation userReservation = reservationOptional.get();
        if (!Objects.equals(userReservation.getUser().getUsername(), username)){
            throw new AppBadRequestException("User didn't reserve that");
        }

        if (userReservation.isConfirmed()){
            throw new AppConflictRequestException("Reservation is already confirmed");
        }

        userReservation.setConfirmed(true);
        reservationRepository.save(userReservation);

        EventSeat userEventSeat = userReservation.getEventSeat();
        userEventSeat.setSeatStatus(SeatStatus.OCCUPIED);

        ReservationConfirmResponse response = new ReservationConfirmResponse();
        response.setMessage("Reservation confirmed");
        return response;
    }
}
