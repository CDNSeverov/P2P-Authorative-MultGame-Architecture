FROM openjdk:21

WORKDIR /app

# Copy only source files
COPY src/ ./src/

# Compile all Java files in the org.example package
RUN javac -d bin src/main/java/org/example/*.java

CMD ["java", "-cp", "bin", "org.example.Main"]