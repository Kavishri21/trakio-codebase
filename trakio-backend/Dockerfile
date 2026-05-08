# -------- Build Stage --------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and install dependencies first (caches them for faster builds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and package the application
COPY src ./src
RUN mvn clean package -DskipTests

# -------- Run Stage --------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the packaged JAR file from the build stage
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

# Explicitly expose port 8080 for Render
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
