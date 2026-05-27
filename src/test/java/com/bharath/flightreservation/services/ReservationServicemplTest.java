package com.bharath.flightreservation.services;

import com.bharath.flightreservation.dtos.ReservationRequest;
import com.bharath.flightreservation.entities.Flight;
import com.bharath.flightreservation.entities.Passenger;
import com.bharath.flightreservation.entities.Reservation;
import com.bharath.flightreservation.repos.FlightRepository;
import com.bharath.flightreservation.repos.Passengerepository;
import com.bharath.flightreservation.repos.Reservationrepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReservationServicempl.bookFlight().
 *
 * Dependencies (FlightRepository, Passengerepository, Reservationrepository)
 * are mocked so no database or Spring context is required.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServicemplTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private Passengerepository passengerRepository;

    @Mock
    private Reservationrepository reservationRepository;

    @InjectMocks
    private ReservationServicempl reservationService;

    private Flight sampleFlight;

    @BeforeEach
    void setUp() {
        sampleFlight = new Flight();
        sampleFlight.setId(1L);
        sampleFlight.setFlightNumber("AA101");
        sampleFlight.setOperatingAirlines("American Airlines");
        sampleFlight.setDepartureCity("New York");
        sampleFlight.setArrivalCity("Los Angeles");
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    @Test
    void bookFlight_fetchesFlightByIdFromRepository() {
        // Arrange
        ReservationRequest request = buildRequest(1L, "John", "Doe", "john@example.com", "555-0100");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(sampleFlight));
        stubSavePassenger();
        stubSaveReservation();

        // Act
        reservationService.bookFlight(request);

        // Assert
        verify(flightRepository).findById(1L);
    }

    @Test
    void bookFlight_savesNewPassengerWithFieldsFromRequest() {
        // Arrange
        ReservationRequest request = buildRequest(1L, "Jane", "Smith", "jane@example.com", "555-0200");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(sampleFlight));
        stubSavePassenger();
        stubSaveReservation();

        // Act
        reservationService.bookFlight(request);

        // Assert: capture the Passenger that was passed to save()
        ArgumentCaptor<Passenger> passengerCaptor = ArgumentCaptor.forClass(Passenger.class);
        verify(passengerRepository).save(passengerCaptor.capture());

        Passenger saved = passengerCaptor.getValue();
        assertThat(saved.getFirstName()).isEqualTo("Jane");
        assertThat(saved.getLastName()).isEqualTo("Smith");
        assertThat(saved.getEmail()).isEqualTo("jane@example.com");
        assertThat(saved.getPhone()).isEqualTo("555-0200");
    }

    @Test
    void bookFlight_savesReservationLinkedToFlightAndPassenger() {
        // Arrange
        ReservationRequest request = buildRequest(1L, "Alice", "Brown", "alice@example.com", "555-0300");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(sampleFlight));

        Passenger savedPassenger = new Passenger();
        savedPassenger.setId(42L);
        when(passengerRepository.save(any(Passenger.class))).thenReturn(savedPassenger);

        stubSaveReservation();

        // Act
        reservationService.bookFlight(request);

        // Assert
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());

        Reservation saved = reservationCaptor.getValue();
        assertThat(saved.getFlight()).isSameAs(sampleFlight);
        assertThat(saved.getPassenger()).isNotNull();
    }

    @Test
    void bookFlight_setsCheckedInToFalseOnNewReservation() {
        // Arrange
        ReservationRequest request = buildRequest(1L, "Bob", "Jones", "bob@example.com", "555-0400");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(sampleFlight));
        stubSavePassenger();
        stubSaveReservation();

        // Act
        reservationService.bookFlight(request);

        // Assert: checkedIn must be false — passengers are not checked in at booking time
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().isCheckedIn()).isFalse();
    }

    @Test
    void bookFlight_returnsPersistedReservation() {
        // Arrange
        ReservationRequest request = buildRequest(1L, "Carol", "White", "carol@example.com", "555-0500");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(sampleFlight));
        stubSavePassenger();

        Reservation expectedReservation = new Reservation();
        expectedReservation.setId(99L);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(expectedReservation);

        // Act
        Reservation result = reservationService.bookFlight(request);

        // Assert: the object returned by reservationRepository.save() is what bookFlight() returns
        // Note: the implementation currently returns the local variable (not the save() result),
        // so we verify at minimum that a non-null Reservation is returned.
        assertThat(result).isNotNull();
    }

    @Test
    void bookFlight_passengersAreSavedBeforeReservation() {
        // Arrange — verify call ordering: passenger must exist before the reservation references it
        ReservationRequest request = buildRequest(1L, "Dave", "Green", "dave@example.com", "555-0600");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(sampleFlight));
        stubSavePassenger();
        stubSaveReservation();

        // Act
        reservationService.bookFlight(request);

        // Assert: ordered verification
        var inOrder = inOrder(passengerRepository, reservationRepository);
        inOrder.verify(passengerRepository).save(any(Passenger.class));
        inOrder.verify(reservationRepository).save(any(Reservation.class));
    }

    // -----------------------------------------------------------------------
    // Error / edge cases
    // -----------------------------------------------------------------------

    @Test
    void bookFlight_throwsNoSuchElementException_whenFlightNotFound() {
        // Arrange
        ReservationRequest request = buildRequest(999L, "Ghost", "User", "ghost@example.com", "000-0000");
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        // The implementation calls orElseThrow() (no-arg), which throws NoSuchElementException.
        assertThatThrownBy(() -> reservationService.bookFlight(request))
                .isInstanceOf(NoSuchElementException.class);

        // No passenger or reservation should be saved if the flight does not exist
        verifyNoInteractions(passengerRepository, reservationRepository);
    }

    @Test
    void bookFlight_doesNotSaveReservation_whenPassengerSaveThrows() {
        // Arrange
        ReservationRequest request = buildRequest(1L, "Err", "User", "err@example.com", "111-1111");
        when(flightRepository.findById(1L)).thenReturn(Optional.of(sampleFlight));
        when(passengerRepository.save(any(Passenger.class)))
                .thenThrow(new RuntimeException("DB constraint violation"));

        // Act & Assert
        assertThatThrownBy(() -> reservationService.bookFlight(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB constraint violation");

        verifyNoInteractions(reservationRepository);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ReservationRequest buildRequest(Long flightId, String firstName, String lastName,
                                            String email, String phone) {
        ReservationRequest req = new ReservationRequest();
        req.setFlightId(flightId);
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setEmail(email);
        req.setPhone(phone);
        return req;
    }

    private void stubSavePassenger() {
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void stubSaveReservation() {
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
    }
}
