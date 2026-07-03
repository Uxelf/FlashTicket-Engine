package com.uxelf.dev.ticketDealer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter @Setter
@Table (name = "seat", uniqueConstraints = {@UniqueConstraint(columnNames = {
        "room_id", "row", "number"
})})
public class Seat {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private Room room;

    private int row;
    private int number;

}
