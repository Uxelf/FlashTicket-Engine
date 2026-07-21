package com.uxelf.dev.ticketDealer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table (name = "event")
@Getter @Setter
public class Event {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn (name = "room_id", referencedColumnName = "id")
    private Room room;

    private LocalDateTime start_time;
    private LocalDateTime end_time;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventSeat> eventSeats;
}
