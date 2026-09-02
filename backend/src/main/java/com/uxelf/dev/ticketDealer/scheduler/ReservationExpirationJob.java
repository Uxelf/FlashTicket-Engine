package com.uxelf.dev.ticketDealer.scheduler;

import com.uxelf.dev.ticketDealer.entity.EventSeat;
import com.uxelf.dev.ticketDealer.entity.Reservation;
import com.uxelf.dev.ticketDealer.enums.SeatStatus;
import com.uxelf.dev.ticketDealer.repository.EventSeatRepository;
import com.uxelf.dev.ticketDealer.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Component
public class ReservationExpirationJob {

    private ReservationRepository reservationRepository;
    private EventSeatRepository eventSeatRepository;

    @Scheduled(fixedRate = 60000)
    public void expireReservations(){
        List<Reservation> expiredReservations = reservationRepository.findAllExpired(LocalDateTime.now());

        for (Reservation reservation : expiredReservations){
            EventSeat eventSeat = reservation.getEventSeat();
            eventSeat.setSeatStatus(SeatStatus.FREE);
            eventSeatRepository.save(eventSeat);
            reservationRepository.delete(reservation);
        }
    }
}
