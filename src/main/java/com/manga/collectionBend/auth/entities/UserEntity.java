package com.manga.collectionBend.auth.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String name;

//    this var contains unique naming value - only one unique name exists for each user
    private String username;

    private String email;

    private String password;
}
