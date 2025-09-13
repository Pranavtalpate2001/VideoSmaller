# Use official OpenJDK 21
FROM eclipse-temurin:21-jdk-alpine

# Install ffmpeg
RUN apk add --no-cache ffmpeg

# Set working directory
WORKDIR /app

# Copy Maven project
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src

# Package application using Maven wrapper
RUN ./mvnw clean package -DskipTests

# Expose port (Render sets PORT environment variable)
EXPOSE 8080

# Run Spring Boot app
ENTRYPOINT ["java","-jar","target/VideoCompreesed-2-0.0.1-SNAPSHOT.jar"]
