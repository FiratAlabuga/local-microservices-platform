# Multi-stage Dockerfile for Service A (Order Service)
# Stage 1: Runtime base
FROM amazoncorretto:25-alpine

# Security: non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy built bootJar from host or build context
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

RUN chown -R appuser:appgroup /app

USER appuser:appgroup

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
