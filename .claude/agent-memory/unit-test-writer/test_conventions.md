---
name: test-conventions
description: Existing test file location, naming, and framework conventions for the flightreservation project
metadata:
  type: project
---

Test source root: src/test/java/com/bharath/flightreservation/

Test files written (all passing as of 2026-05-27):
- services/ReservationServicemplTest.java — 8 tests, @ExtendWith(MockitoExtension), @InjectMocks
- controllers/ReservationRestcontrollerTest.java — 8 tests, @WebMvcTest + MockMvc
- controllers/ReservationcontrollerTest.java — 7 tests, @WebMvcTest + MockMvc
- repos/FlightRepositoryTest.java — 8 tests, @DataJpaTest (H2 in-memory)

src/test/resources/application.properties added to override MySQL with H2 for all test slices.
H2 dependency added to pom.xml (test scope) to enable @DataJpaTest.

Naming convention: *Test.java (single suffix). Maven Surefire picks this up automatically.

Test runner: Maven (`mvn test`). No separate test runner config file found.

Mocking pattern: @ExtendWith(MockitoExtension.class) + @InjectMocks for pure unit tests. @WebMvcTest for controller slices with @MockBean for dependencies. @DataJpaTest for repository slice tests.

Known tricky-to-mock note: @WebMvcTest slices with Thymeleaf controllers — templates are rendered by default. MockMvc in a @WebMvcTest slice re-throws unhandled servlet exceptions rather than converting them to HTTP responses, so use assertThrows(Exception.class, ...) to assert on known-buggy .get() calls instead of andExpect(status().is5xxServerError()).

**Why:** Tests were written in a dedicated session; these conventions emerged from the first run.

**How to apply:** Place new test classes under src/test/java/com/bharath/flightreservation/ mirroring the production package structure. Name files <ClassUnderTest>Test.java.
