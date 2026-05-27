package com.bharath.flightreservation.controllers;

import com.bharath.flightreservation.dtos.ReservationRequest;
import com.bharath.flightreservation.entities.Flight;
import com.bharath.flightreservation.entities.Reservation;
import com.bharath.flightreservation.repos.FlightRepository;
import com.bharath.flightreservation.services.ReservationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for Reservationcontroller (Thymeleaf MVC controller) using
 * @WebMvcTest + MockMvc.
 *
 * Both endpoints return view names; we verify the view name and the model
 * attributes that are added so the templates can render.
 */
@WebMvcTest(Reservationcontroller.class)
class ReservationcontrollerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightRepository flightRepository;

    @MockBean
    private ReservationService reservationService;

    private Flight sampleFlight;

    @BeforeEach
    void setUp() {
        sampleFlight = new Flight();
        sampleFlight.setId(5L);
        sampleFlight.setFlightNumber("UA300");
        sampleFlight.setDepartureCity("Chicago");
        sampleFlight.setArrivalCity("Miami");
    }

    // -----------------------------------------------------------------------
    // GET /showCompleteReservation?flightId={id}
    // -----------------------------------------------------------------------

    @Test
    void showCompleteReservation_rendersCompleteReservationView() throws Exception {
        // Arrange
        when(flightRepository.findById(5L)).thenReturn(Optional.of(sampleFlight));

        // Act & Assert
        mockMvc.perform(get("/showCompleteReservation").param("flightId", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("completeReservation"));
    }

    @Test
    void showCompleteReservation_addsFlightToModel_whenFlightExists() throws Exception {
        // Arrange
        when(flightRepository.findById(5L)).thenReturn(Optional.of(sampleFlight));

        // Act & Assert
        mockMvc.perform(get("/showCompleteReservation").param("flightId", "5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("flight"))
                .andExpect(model().attribute("flight", sampleFlight));
    }

    @Test
    void showCompleteReservation_throwsTemplateException_whenFlightNotFound() {
        /*
         * BUG DOCUMENTATION TEST — SECONDARY BUG IN TEMPLATE:
         *
         * The controller uses flightRepository.findById(flightId).orElse(null),
         * which is safe at the controller level — but the Thymeleaf template
         * completeReservation.html unconditionally accesses ${flight.operatingAirlines}
         * (and other fields) with no null-guard (e.g. th:if="${flight != null}").
         *
         * When the flight ID does not exist, the model receives a null "flight"
         * and Thymeleaf throws TemplateProcessingException: "Exception evaluating
         * SpringEL expression: 'flight.operatingAirlines'".
         *
         * Fix options:
         *   1. Add th:if="${flight != null}" guards in completeReservation.html, OR
         *   2. Throw a 404 ResponseStatusException in the controller when flight is null.
         *
         * After fixing, replace this test with one that expects status().isNotFound()
         * or a graceful "flight not found" view.
         */
        when(flightRepository.findById(99L)).thenReturn(Optional.empty());

        // The TemplateProcessingException propagates through MockMvc as an Exception
        assertThrows(Exception.class,
                () -> mockMvc.perform(get("/showCompleteReservation").param("flightId", "99")));
    }

    @Test
    void showCompleteReservation_queriesRepositoryWithCorrectFlightId() throws Exception {
        // Arrange
        when(flightRepository.findById(5L)).thenReturn(Optional.of(sampleFlight));

        // Act
        mockMvc.perform(get("/showCompleteReservation").param("flightId", "5"));

        // Assert
        verify(flightRepository).findById(5L);
    }

    // -----------------------------------------------------------------------
    // POST /completeReservation
    // -----------------------------------------------------------------------

    @Test
    void completeReservation_rendersReservationConfirmationView() throws Exception {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(77L);
        when(reservationService.bookFlight(any(ReservationRequest.class))).thenReturn(reservation);

        // Act & Assert
        mockMvc.perform(post("/completeReservation")
                        .param("flightId", "5")
                        .param("firstName", "Alice")
                        .param("lastName", "Smith")
                        .param("email", "alice@example.com")
                        .param("phone", "555-1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservationConfirmation"));
    }

    @Test
    void completeReservation_addsSuccessMessageWithReservationIdToModel() throws Exception {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(77L);
        when(reservationService.bookFlight(any(ReservationRequest.class))).thenReturn(reservation);

        // Act & Assert
        mockMvc.perform(post("/completeReservation")
                        .param("flightId", "5")
                        .param("firstName", "Alice")
                        .param("lastName", "Smith")
                        .param("email", "alice@example.com")
                        .param("phone", "555-1234"))
                .andExpect(model().attributeExists("msg"))
                .andExpect(model().attribute("msg",
                        "Reservation created successfully! Your reservation ID is: 77"));
    }

    @Test
    void completeReservation_delegatesToReservationService() throws Exception {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(10L);
        when(reservationService.bookFlight(any(ReservationRequest.class))).thenReturn(reservation);

        // Act
        mockMvc.perform(post("/completeReservation")
                .param("flightId", "5")
                .param("firstName", "Bob")
                .param("lastName", "Jones")
                .param("email", "bob@example.com")
                .param("phone", "555-5678"));

        // Assert: bookFlight() must be called exactly once
        verify(reservationService, times(1)).bookFlight(any(ReservationRequest.class));
    }
}
