package com.bharath.flightreservation.entities;

import jakarta.persistence.*;
import lombok.Data;



@Entity
@Data
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

   
}