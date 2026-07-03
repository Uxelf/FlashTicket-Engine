package com.uxelf.dev.ticketDealer.entity;

import com.uxelf.dev.ticketDealer.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table (name = "event_seat", uniqueConstraints = @UniqueConstraint(columnNames = {
        "event_id", "seat_id"
}))
@Getter @Setter
public class EventSeat {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn ( name = "event_id", referencedColumnName = "id")
    private Event event;

    @ManyToOne
    @JoinColumn (name = "seat_id", referencedColumnName = "id")
    private Seat seat;

    @Enumerated (EnumType.STRING)
    private SeatStatus seatStatus;
}
