package com.uxelf.dev.ticketDealer.controller;

import com.uxelf.dev.ticketDealer.dto.reservation.ReservationRequest;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationResponse;
import com.uxelf.dev.ticketDealer.entity.Reservation;
import com.uxelf.dev.ticketDealer.service.ReservationsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/reservations")
@AllArgsConstructor
public class ReservationsController {

    private ReservationsService reservationsService;

    @PostMapping()
    public ResponseEntity<ReservationResponse> reserve(@RequestBody @Valid ReservationRequest request){
        Reservation reservation = reservationsService
                .reserveSeat(request.getEventId(), request.getUsername(), request.getSeatRow(), request.getSeatNumber());

        ReservationResponse response = new ReservationResponse();
        response.setReservationId(reservation.getId());
        response.setUsername(reservation.getUser().getUsername());
        response.setExpiresAt(reservation.getExpiresAt());

        return ResponseEntity.ok(response);
    }
}
