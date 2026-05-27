package com.bharath.flightreservation.repos;

import com.bharath.flightreservation.entities.Flight;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice tests for FlightRepository using @DataJpaTest (H2 in-memory).
 *
 * Focuses on the custom JPQL query method findFlights(), which is the only
 * query that cannot be verified by simply trusting Spring Data's generated methods.
 */
@DataJpaTest
class FlightRepositoryTest {

    @Autowired
    private FlightRepository flightRepository;

    private static final Date DEPARTURE_DATE = Date.valueOf("2024-12-01");
    private static final Date OTHER_DATE     = Date.valueOf("2024-12-02");

    @BeforeEach
    void seedData() {
        // Flight 1: matches NYC -> LAX on DEPARTURE_DATE
        Flight f1 = new Flight();
        f1.setFlightNumber("AA101");
        f1.setOperatingAirlines("American Airlines");
        f1.setDepartureCity("New York");
        f1.setArrivalCity("Los Angeles");
        f1.setDateOfDeparture(DEPARTURE_DATE);
        f1.setEstimatedDepartureTime(Time.valueOf("08:00:00"));
        flightRepository.save(f1);

        // Flight 2: same route, different date
        Flight f2 = new Flight();
        f2.setFlightNumber("AA102");
        f2.setOperatingAirlines("American Airlines");
        f2.setDepartureCity("New York");
        f2.setArrivalCity("Los Angeles");
        f2.setDateOfDeparture(OTHER_DATE);
        f2.setEstimatedDepartureTime(Time.valueOf("12:00:00"));
        flightRepository.save(f2);

        // Flight 3: different route, same date
        Flight f3 = new Flight();
        f3.setFlightNumber("DL300");
        f3.setOperatingAirlines("Delta");
        f3.setDepartureCity("Chicago");
        f3.setArrivalCity("Miami");
        f3.setDateOfDeparture(DEPARTURE_DATE);
        f3.setEstimatedDepartureTime(Time.valueOf("10:00:00"));
        flightRepository.save(f3);
    }

    // -----------------------------------------------------------------------
    // findFlights(from, to, departureDate)
    // -----------------------------------------------------------------------

    @Test
    void findFlights_returnsMatchingFlight_whenAllCriteriaMatch() {
        // Act
        List<Flight> results = flightRepository.findFlights("New York", "Los Angeles", DEPARTURE_DATE);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFlightNumber()).isEqualTo("AA101");
    }

    @Test
    void findFlights_returnsEmptyList_whenNoCityMatch() {
        // Act
        List<Flight> results = flightRepository.findFlights("Houston", "Denver", DEPARTURE_DATE);

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    void findFlights_returnsEmptyList_whenDateDoesNotMatch() {
        // Act — correct cities but a date with no flights
        List<Flight> results = flightRepository.findFlights("New York", "Los Angeles",
                Date.valueOf("2099-01-01"));

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    void findFlights_doesNotReturnFlightsFromDifferentRoute_onSameDate() {
        // Act
        List<Flight> results = flightRepository.findFlights("New York", "Los Angeles", DEPARTURE_DATE);

        // Assert: the Chicago -> Miami flight on the same date must not appear
        boolean containsDeltaFlight = results.stream()
                .anyMatch(f -> "DL300".equals(f.getFlightNumber()));
        assertThat(containsDeltaFlight).isFalse();
    }

    @Test
    void findFlights_doesNotReturnFlightsOnDifferentDate_forSameRoute() {
        // Act
        List<Flight> results = flightRepository.findFlights("New York", "Los Angeles", DEPARTURE_DATE);

        // Assert: AA102 (same route, OTHER_DATE) must not be returned
        boolean containsSecondFlight = results.stream()
                .anyMatch(f -> "AA102".equals(f.getFlightNumber()));
        assertThat(containsSecondFlight).isFalse();
    }

    @Test
    void findFlights_returnsMultipleFlights_whenMoreThanOneMatchExists() {
        // Arrange: add a second flight on the same route and same date
        Flight extra = new Flight();
        extra.setFlightNumber("UA500");
        extra.setOperatingAirlines("United");
        extra.setDepartureCity("New York");
        extra.setArrivalCity("Los Angeles");
        extra.setDateOfDeparture(DEPARTURE_DATE);
        extra.setEstimatedDepartureTime(Time.valueOf("15:00:00"));
        flightRepository.save(extra);

        // Act
        List<Flight> results = flightRepository.findFlights("New York", "Los Angeles", DEPARTURE_DATE);

        // Assert
        assertThat(results).hasSize(2);
    }

    // -----------------------------------------------------------------------
    // Standard CRUD (inherited from JpaRepository — sanity checks)
    // -----------------------------------------------------------------------

    @Test
    void save_persistsFlightAndAssignsId() {
        Flight flight = new Flight();
        flight.setFlightNumber("WN999");
        flight.setOperatingAirlines("Southwest");
        flight.setDepartureCity("Dallas");
        flight.setArrivalCity("Phoenix");
        flight.setDateOfDeparture(DEPARTURE_DATE);

        Flight saved = flightRepository.save(flight);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0L);
    }

    @Test
    void findById_returnsPersistedFlight() {
        Flight flight = new Flight();
        flight.setFlightNumber("B6100");
        flight.setOperatingAirlines("JetBlue");
        flight.setDepartureCity("Boston");
        flight.setArrivalCity("Fort Lauderdale");
        flight.setDateOfDeparture(DEPARTURE_DATE);
        Flight saved = flightRepository.save(flight);

        Optional<Flight> found = flightRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFlightNumber()).isEqualTo("B6100");
    }
}
