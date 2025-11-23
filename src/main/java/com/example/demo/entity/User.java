package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    // getters/setters
}
