package com.bharath.flightreservation.entities;
import jakarta.persistence.*;

import jakarta.persistence.GeneratedValue;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;



@Entity
@Data
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flightNumber;
    private String operatingAirlines;
    private String departureCity;
    private String arrivalCity;
    private Date dateOfDeparture;
    private Time estimatedDepartureTime;
    // Getters and Setters
}