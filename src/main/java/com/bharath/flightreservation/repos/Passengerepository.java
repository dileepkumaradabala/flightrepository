package com.bharath.flightreservation.repos;

import com.bharath.flightreservation.entities.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  Passengerepository extends JpaRepository<Passenger, Long> {
    // You can add custom query methods here if needed
}