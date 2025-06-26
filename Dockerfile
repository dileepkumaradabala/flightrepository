# Use a lightweight Java image
FROM alpine/java:17-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file
COPY target/flightreservation-0.0.1-SNAPSHOT.jar /app/flightreservation-0.0.1-SNAPSHOT.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/flightreservation-0.0.1-SNAPSHOT.jar"]