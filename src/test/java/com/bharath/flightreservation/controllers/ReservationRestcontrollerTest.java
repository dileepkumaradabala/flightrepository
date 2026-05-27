package com.bharath.flightreservation.controllers;

import com.bharath.flightreservation.entities.Flight;
import com.bharath.flightreservation.entities.Passenger;
import com.bharath.flightreservation.entities.Reservation;
import com.bharath.flightreservation.dtos.ReservationUpdaterequest;
import com.bharath.flightreservation.repos.Reservationrepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Slice tests for ReservationRestcontroller using @WebMvcTest + MockMvc.
 *
 * KNOWN BUG (documented here):
 *   Both findReservation() and updateReservation() call Optional.get() without
 *   checking isPresent(). When the repository returns Optional.empty() the
 *   NoSuchElementException is unhandled, bubbling up as HTTP 500 instead of
 *   a proper 404. The tests below named *_whenReservationNotFound explicitly
 *   demonstrate this bug: they assert the actual (broken) behaviour of HTTP 500
 *   so that the test suite fails visibly if the bug is fixed without updating the tests.
 *   Once a proper @ExceptionHandler / ResponseEntityExceptionHandler is added,
 *   update those tests to expect HTTP 404.
 */
@WebMvcTest(ReservationRestcontroller.class)
class ReservationRestcontrollerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Reservationrepository reservationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Reservation sampleReservation;

    @BeforeEach
    void setUp() {
        Flight flight = new Flight();
        flight.setId(10L);
        flight.setFlightNumber("DL200");

        Passenger passenger = new Passenger();
        passenger.setId(20L);
        passenger.setFirstName("John");
        passenger.setLastName("Doe");

        sampleReservation = new Reservation();
        sampleReservation.setId(1L);
        sampleReservation.setFlight(flight);
        sampleReservation.setPassenger(passenger);
        sampleReservation.setCheckedIn(false);
        sampleReservation.setNumberOfBags(0);
    }

    // -----------------------------------------------------------------------
    // GET /reservations/{id}  — findReservation()
    // -----------------------------------------------------------------------

    @Test
    void findReservation_returnsReservationJson_whenFound() throws Exception {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));

        // Act & Assert
        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.checkedIn").value(false));
    }

    @Test
    void findReservation_returnsCorrectFlightData_whenFound() throws Exception {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));

        // Act & Assert
        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flight.flightNumber").value("DL200"));
    }

    @Test
    void findReservation_throwsNoSuchElementException_whenReservationNotFound() {
        /*
         * BUG DOCUMENTATION TEST:
         * When Optional is empty, Optional.get() throws NoSuchElementException.
         * Because there is no @ExceptionHandler for NoSuchElementException, Spring
         * MockMvc (in @WebMvcTest slice mode) re-throws it wrapped in a
         * ServletException rather than converting it to an HTTP response.
         *
         * The correct fix is to replace:
         *   reservationRepository.findById(id).get()
         * with:
         *   reservationRepository.findById(id)
         *       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
         *
         * After that fix, update this test to: andExpect(status().isNotFound())
         */
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // The unhandled NoSuchElementException propagates through MockMvc as a
        // wrapped Exception — asserting on it proves the bug is present.
        assertThrows(Exception.class,
                () -> mockMvc.perform(get("/reservations/999")));
    }

    @Test
    void findReservation_callsRepositoryWithCorrectId() throws Exception {
        // Arrange
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(sampleReservation));

        // Act
        mockMvc.perform(get("/reservations/5"));

        // Assert
        verify(reservationRepository).findById(5L);
    }

    // -----------------------------------------------------------------------
    // POST /reservations  — updateReservation()
    // -----------------------------------------------------------------------

    @Test
    void updateReservation_updatesCheckedInAndBags_whenReservationExists() throws Exception {
        // Arrange
        ReservationUpdaterequest updateRequest = new ReservationUpdaterequest();
        updateRequest.setId(1L);
        updateRequest.setCheckedIn(true);
        updateRequest.setNumberOfBags(2);

        Reservation updatedReservation = new Reservation();
        updatedReservation.setId(1L);
        updatedReservation.setCheckedIn(true);
        updatedReservation.setNumberOfBags(2);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(updatedReservation);

        // Act & Assert
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(true))
                .andExpect(jsonPath("$.numberOfBags").value(2));
    }

    @Test
    void updateReservation_persistsUpdatedReservationViaRepository() throws Exception {
        // Arrange
        ReservationUpdaterequest updateRequest = new ReservationUpdaterequest();
        updateRequest.setId(1L);
        updateRequest.setCheckedIn(true);
        updateRequest.setNumberOfBags(3);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(sampleReservation);

        // Act
        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // Assert: save() must be called to persist the change
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void updateReservation_throwsNoSuchElementException_whenReservationNotFound() throws Exception {
        /*
         * BUG DOCUMENTATION TEST:
         * Same unchecked Optional.get() bug as in findReservation().
         * The unhandled NoSuchElementException propagates through MockMvc as a
         * wrapped Exception.
         *
         * The correct fix is to replace:
         *   reservationRepository.findById(request.getId()).get()
         * with:
         *   reservationRepository.findById(request.getId())
         *       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
         *
         * After that fix, update this test to: andExpect(status().isNotFound())
         */
        ReservationUpdaterequest updateRequest = new ReservationUpdaterequest();
        updateRequest.setId(999L);
        updateRequest.setCheckedIn(true);
        updateRequest.setNumberOfBags(1);

        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))));
    }

    @Test
    void updateReservation_setsCheckedInFalseWhenRequestIsFalse() throws Exception {
        // Arrange: start with checkedIn=true, request sets it back to false
        sampleReservation.setCheckedIn(true);

        ReservationUpdaterequest updateRequest = new ReservationUpdaterequest();
        updateRequest.setId(1L);
        updateRequest.setCheckedIn(false);
        updateRequest.setNumberOfBags(0);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(false));
    }
}
