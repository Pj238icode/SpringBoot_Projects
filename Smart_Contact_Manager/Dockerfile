# ---- Build Stage ----
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and src folder relative to Dockerfile
COPY Smart_Contact_Manager/pom.xml .
COPY Smart_Contact_Manager/src ./src

# Build the project and skip tests
RUN mvn clean package -DskipTests

# ---- Runtime Stage ----
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port for Render
EXPOSE 8080

# Start the application
CMD ["java", "-jar", "app.jar"]
