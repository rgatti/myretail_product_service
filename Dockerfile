# Use official maven/Java 11 image to create a build artifact
FROM maven:3.6-jdk-11 as builder

# Copy local code to the container image
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the release artifact
RUN mvn package -DskipTests

# Use OpenJDK for the base image
FROM openjdk:11-jre-slim

# Copy release artifacts to the production image from the builder stage
COPY --from=builder /app/target/product-service-1.0.jar /product-service-1.0.jar
COPY --from=builder /app/target/product-service-1.0.lib/* /product-service-1.0.lib/

# Run the service on container start
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar"]
CMD ["/product-service-1.0.jar"]
