FROM openjdk:21-jdk

WORKDIR /app

# Copy the built jar file (you'll need to build it first in IntelliJ)
COPY out/artifacts/projectSeverov_jar/projectSeverov.jar /app/connect4-p2p.jar

# Create a directory for peer-specific configurations
RUN mkdir -p /app/config

# Entry point will be overridden by docker-compose for each peer
CMD ["java", "-jar", "connect4-p2p.jar"]