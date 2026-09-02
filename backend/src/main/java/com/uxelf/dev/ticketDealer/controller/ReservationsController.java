package com.uxelf.dev.ticketDealer.controller;

import com.uxelf.dev.ticketDealer.dto.reservation.*;
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

    @PostMapping("/confirm")
    public ResponseEntity<ReservationConfirmResponse> confirmReservation(@RequestBody @Valid ReservationConfirmRequest request){
        ReservationConfirmResponse response = reservationsService
                .confirmReservation(request.getUsername(), request.getReservationId());
        return ResponseEntity.ok(response);
    }
}
