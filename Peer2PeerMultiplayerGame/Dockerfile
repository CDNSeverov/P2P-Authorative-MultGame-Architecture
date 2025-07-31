# Use a base image with Java
FROM openjdk:21-jdk

# Create app directory
WORKDIR /app

# Copy the built JAR file
COPY out/artifacts/projectSeverov_jar/projectSeverov.jar app.jar

# Expose the default port (can be overridden)
EXPOSE 7777

# Set the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]