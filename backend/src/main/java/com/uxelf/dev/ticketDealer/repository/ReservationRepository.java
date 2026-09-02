package com.uxelf.dev.ticketDealer.repository;

import com.uxelf.dev.ticketDealer.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findAllByUserUsername(String username);

    @Query("SELECT r FROM Reservation r WHERE r.confirmed = false AND r.expiresAt <= ?1")
    List<Reservation> findAllExpired(LocalDateTime now);
}
