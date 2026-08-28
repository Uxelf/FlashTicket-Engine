package com.uxelf.dev.ticketDealer.controller;

import com.uxelf.dev.ticketDealer.dto.reservation.ReservationRequest;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationResponse;
import com.uxelf.dev.ticketDealer.dto.reservation.ReservationsListResponse;
import com.uxelf.dev.ticketDealer.entity.Reservation;
import com.uxelf.dev.ticketDealer.service.ReservationsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/reservations")
@AllArgsConstructor
public class ReservationsController {

    private ReservationsService reservationsService;

    @PostMapping()
    public ResponseEntity<ReservationResponse> reserve(@RequestBody @Valid ReservationRequest request){
        ReservationResponse response = reservationsService
                .reserveSeat(request.getEventId(), request.getUsername(), request.getSeatRow(), request.getSeatNumber());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{username}")
    public ResponseEntity<ReservationsListResponse> getReservations(@PathVariable String username){

        ReservationsListResponse response = reservationsService.getUserReservations(username);

        return ResponseEntity.ok(response);
    }
}
