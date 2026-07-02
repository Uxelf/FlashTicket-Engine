package com.uxelf.dev.ticketDealer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter @Setter
@AllArgsConstructor
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;
}
