package com.bharath.flightreservation.repos;

import com.bharath.flightreservation.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Reservationrepository extends JpaRepository<Reservation, Long> {
    // You can add custom query methods here if needed
}