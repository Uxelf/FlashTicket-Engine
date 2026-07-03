package com.uxelf.dev.ticketDealer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table (name = "users")
@Getter @Setter
public class User {

    @Id
    @GeneratedValue ( strategy = GenerationType.UUID)
    @Column (nullable = false, updatable = false)
    private UUID id;

    @Column (nullable = false, unique = true)
    private String username;
}
