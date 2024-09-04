FROM openjdk:11-slim-buster as builder
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B package

FROM bellsoft/liberica-openjdk-alpine-musl:latest
COPY --from=builder target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]