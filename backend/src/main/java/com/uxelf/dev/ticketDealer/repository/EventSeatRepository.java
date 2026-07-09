package com.uxelf.dev.ticketDealer.repository;

import com.uxelf.dev.ticketDealer.entity.EventSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventSeatRepository extends JpaRepository<EventSeat, UUID> {

}
