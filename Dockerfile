# Stage 1: Build the native executable with Maven and GraalVM
# Using a base image with Java 21, which has good support for GraalVM native image compilation via Spring Boot 3
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the native executable
# This command uses the 'native' profile, which should be activated in Spring Boot projects.
# We skip tests as they are not needed for the final artifact.
RUN ./mvnw native:compile -Pnative -DskipTests

# Stage 2: Create the final, minimal image
# Using a distroless static image which is very small and secure
FROM gcr.io/distroless/cc-static

# Set the working directory
WORKDIR /app

# Copy the native executable from the builder stage
COPY --from=builder /app/target/spring-project-steganography-tool .

# Expose the port the application runs on
EXPOSE 8080

# Set the entrypoint to run the executable
ENTRYPOINT ["/app/spring-project-steganography-tool"]
