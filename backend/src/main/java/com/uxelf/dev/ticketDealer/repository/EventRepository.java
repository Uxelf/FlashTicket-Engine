package com.uxelf.dev.ticketDealer.repository;

import com.uxelf.dev.ticketDealer.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("select e from Event e where e.room.number = ?1 and e.end_time >= ?2 and e.start_time <= ?3")
    List<Event> findOverlappingEvents(int room_number, LocalDateTime start_time, LocalDateTime end_time);
}
