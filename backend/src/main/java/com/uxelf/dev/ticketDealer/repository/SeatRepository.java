package com.uxelf.dev.ticketDealer.repository;

import com.uxelf.dev.ticketDealer.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findAllByRoomId(UUID id);
    List<Seat> findAllByRoomNumber(int number);
}
