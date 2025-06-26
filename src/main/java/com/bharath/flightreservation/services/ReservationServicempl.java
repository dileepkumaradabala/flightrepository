package com.bharath.flightreservation.services;

import com.bharath.flightreservation.dtos.ReservationRequest;
import com.bharath.flightreservation.entities.Reservation;
import com.bharath.flightreservation.entities.Passenger;
import com.bharath.flightreservation.entities.Flight;
import com.bharath.flightreservation.repos.FlightRepository;
import com.bharath.flightreservation.repos.Passengerepository;
import com.bharath.flightreservation.repos.Reservationrepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class ReservationServicempl implements ReservationService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private Passengerepository passengerRepository;

    @Autowired
    private Reservationrepository reservationRepository;

    @Override
    public Reservation bookFlight(ReservationRequest request) {
        // Fetch flight
        Flight flight = flightRepository.findById(request.getFlightId()).orElseThrow();

        // Create passenger
        Passenger passenger = new Passenger();
        passenger.setFirstName(request.getFirstName());
        passenger.setLastName(request.getLastName());
        passenger.setEmail(request.getEmail());
        passenger.setPhone(request.getPhone());
        passengerRepository.save(passenger);

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setFlight(flight);
        reservation.setPassenger(passenger);
        reservation.setCheckedIn(false);
        reservationRepository.save(reservation);

        return reservation;
    }
}