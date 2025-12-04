FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY . .
RUN chmod +x gradlew
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
