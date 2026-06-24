# Flight Reservation App

Spring Boot + MySQL + Thymeleaf web app for booking flights.

---

## Run Locally

### Option A — Docker Compose (MySQL + App together)

> Requires: Docker Desktop running

```bash
mvn clean package -DskipTests
docker-compose up --build
```

Open: http://localhost:8081/flightreservation/findFlights

To stop: `Ctrl+C` then `docker-compose down`

---

### Option B — Maven directly (MySQL must already be running)

> Requires: MySQL running on port 3306 with database `reservation`, user `root`, password `Dileep@143`

```bash
mvn spring-boot:run
```

Open: http://localhost:8080/flightreservation/findFlights

---

## URL Reference

| Page                  | URL                                                       |
|-----------------------|-----------------------------------------------------------|
| Find Flights          | http://localhost:8080/flightreservation/findFlights       |
| Complete Reservation  | http://localhost:8080/flightreservation/showCompleteReservation |
| REST - Get Reservation | http://localhost:8080/flightreservation/reservations/{id} |

> If using Docker Compose, replace `8080` with `8081` in all URLs above.

---

## Run Tests

```bash
mvn test
```

Tests use an in-memory H2 database — no MySQL needed.

---

## Deploy to Render (Cloud)

1. Update `application.properties` to use environment variables:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/reservation}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:Dileep@143}
server.port=${PORT:8080}
server.servlet.context-path=/flightreservation
```

2. Update `Dockerfile` to a multi-stage build (compiles + runs):

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/flightreservation-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

3. Push to GitHub, connect to Render as a **Docker** web service.

4. Set these environment variables in the Render dashboard:

```
SPRING_DATASOURCE_URL      = jdbc:mysql://<your-db-host>/reservation
SPRING_DATASOURCE_USERNAME = <your-db-user>
SPRING_DATASOURCE_PASSWORD = <your-db-password>
```

5. For the database, use a free external MySQL service like **PlanetScale** (planetscale.com) — Render's hobby tier has no free MySQL.

Live URL will be: `https://your-app-name.onrender.com/flightreservation/findFlights`

---

## Project Structure

```
src/main/java/com/bharath/flightreservation/
├── FlightreservationApplication.java   # Entry point
├── controllers/
│   ├── Flightcontroller.java
│   ├── Reservationcontroller.java
│   └── ReservationRestcontroller.java  # REST API
├── services/
│   └── ReservationServicempl.java
├── entities/
│   ├── Flight.java
│   ├── Passenger.java
│   └── Reservation.java
└── repos/                              # JPA repositories

src/main/resources/
├── application.properties              # DB config, port, context path
└── templates/                          # Thymeleaf HTML pages

sql-scripts/init.sql                    # Creates tables + seeds 10 flights
```

---

## Database Config (Local)

| Setting  | Value                |
|----------|----------------------|
| Host     | localhost:3306       |
| Database | reservation          |
| Username | root                 |
| Password | Dileep@143           |
