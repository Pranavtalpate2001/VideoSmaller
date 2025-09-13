FROM eclipse-temurin:21-jdk-alpine

# Install ffmpeg
RUN apk add --no-cache ffmpeg bash

# Set working directory
WORKDIR /app

# Copy Maven wrapper & project files
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src

# Make mvnw executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port (Render sets PORT env variable)
EXPOSE 8080

# Run Spring Boot app
ENTRYPOINT ["java","-jar","target/VideoCompreesed-2-0.0.1-SNAPSHOT.jar"]
