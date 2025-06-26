package com.bharath.flightreservation.controllers;

import com.bharath.flightreservation.entities.Flight;
import com.bharath.flightreservation.repos.FlightRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;


@Controller
public class Flightcontroller {

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping("/findFlights")
    public String displayFindFlights() {
        return "findFlights"; // Name of the Thymeleaf template (findFlights.html)
    }
    
    @PostMapping("/findFlights")
    public String findFlights(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("departureDate") @DateTimeFormat(pattern = "MM-dd-yyyy") Date departureDate,
            Model model) {

        List<Flight> flights = flightRepository.findFlights(from, to, departureDate);
        model.addAttribute("flights", flights);
        return "displayFlights"; // Name of the Thymeleaf template (displayFlights.html)
    }

}