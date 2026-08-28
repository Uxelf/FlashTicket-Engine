package com.uxelf.dev.ticketDealer.repository;

import com.uxelf.dev.ticketDealer.entity.Event;
import com.uxelf.dev.ticketDealer.entity.EventSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventSeatRepository extends JpaRepository<EventSeat, UUID> {
    List<EventSeat> findByEvent(Event event);
    @Query("SELECT es FROM EventSeat es WHERE es.event.id = ?1 AND es.seat.row = ?2 AND es.seat.number = ?3")
    Optional<EventSeat> findByEventIdAndSeatRowAndSeatNumber(UUID eventId, int row, int number);
}
