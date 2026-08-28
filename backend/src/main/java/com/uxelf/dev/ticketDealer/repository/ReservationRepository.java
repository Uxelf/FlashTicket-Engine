package com.uxelf.dev.ticketDealer.repository;

import com.uxelf.dev.ticketDealer.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findAllByUserUsername(String username);
}
