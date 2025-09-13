# Use OpenJDK 21 base image
FROM openjdk:21-jdk-slim

# Install ffmpeg
RUN apt-get update && apt-get install -y ffmpeg && apt-get clean

# Set working directory
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the project
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

# Expose port (Render uses PORT env)
ENV PORT 8080
EXPOSE 8080

# Set the startup command
CMD ["java", "-jar", "target/VideoCompreesed-2-0.0.1-SNAPSHOT.jar"]
