package com.bharath.flightreservation.entities;
import jakarta.persistence.*;
import lombok.Data;




@Entity
@Data
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean checkedIn;

    private int numberOfBags;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    
}