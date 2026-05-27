---
name: project-stack
description: Language, build tool, framework, and testing setup for the flightreservation project
metadata:
  type: project
---

Java 17 Spring Boot 3.2.5 project built with Maven. Artifact: com.bharath:flightreservation:0.0.1-SNAPSHOT.

Key production dependencies: spring-boot-starter-data-jpa, spring-boot-starter-web, spring-boot-starter-thymeleaf, mysql-connector-j, lombok.

Testing dependencies (pom.xml):
- spring-boot-starter-test (scope=test) — JUnit 5, Mockito, Spring Test
- h2 (scope=test) — added to enable @DataJpaTest without a running MySQL instance

Runtime datasource: MySQL at localhost:3306/reservation (application.properties).
Test datasource: H2 in-memory, configured in src/test/resources/application.properties.

**Why:** pom.xml was the sole build file found; no Gradle, package.json, etc. H2 was added because @DataJpaTest requires an in-memory DB when MySQL is the runtime driver.

**How to apply:** When writing tests, target JUnit 5 (@Test from org.junit.jupiter.api.Test), Mockito for mocking, and @SpringBootTest / @WebMvcTest / @DataJpaTest slices as appropriate. Do not write tests that rely on a live MySQL connection.
