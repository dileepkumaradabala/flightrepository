package com.bharath.flightreservation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bharath.flightreservation.dtos.ReservationUpdaterequest;
import com.bharath.flightreservation.entities.Reservation;
import com.bharath.flightreservation.repos.Reservationrepository;

@RestController
@CrossOrigin(origins = "*")
public class ReservationRestcontroller {

	@Autowired
	Reservationrepository reservationRepository;

	@GetMapping("/reservations/{id}")
	public Reservation findReservation(@PathVariable("id") Long id) {
		return reservationRepository.findById(id).get();

	}

	@PostMapping("/reservations")
	public Reservation updateReservation(@RequestBody ReservationUpdaterequest request) {
		Reservation reservation = reservationRepository.findById(request.getId()).get();
		reservation.setNumberOfBags(request.getNumberOfBags());
		reservation.setCheckedIn(request.getCheckedIn());
		return reservationRepository.save(reservation);

	}

}
