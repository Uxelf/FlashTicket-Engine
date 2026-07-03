package com.uxelf.dev.ticketDealer.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table (name = "reservation", uniqueConstraints = @UniqueConstraint(columnNames = {
        "event_seat_id", "user_id"
}))
public class Reservation {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn (name = "event_seat_id", referencedColumnName = "id")
    private EventSeat eventSeat;

    @ManyToOne
    @JoinColumn (name = "user_id", referencedColumnName = "id")
    private User user;

    private LocalDateTime expires_at;
}
