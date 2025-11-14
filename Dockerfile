# Stage 1: build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew bootJar

# Stage 2: runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy the JAR built in stage 1
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]


