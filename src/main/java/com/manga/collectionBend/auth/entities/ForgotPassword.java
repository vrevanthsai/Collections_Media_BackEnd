package com.manga.collectionBend.auth.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
// lombok methods - for shortcuts
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ForgotPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fpId;

    @Column(nullable = false) // this column/field values should not be null
    private Integer otp;

    @Column(nullable = false)
    private Date expirationTime;

    @OneToOne // will be associated with that particular user-table who asked for forgot pwd
    @JoinColumn(name = "user_id", nullable = false) // child-table- to map both tables properly and user_id(primaryKey) is default naming for UserTable in MySql
    private UserEntity user;
}
