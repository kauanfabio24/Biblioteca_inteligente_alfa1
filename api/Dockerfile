# Etapa 1: compilar
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Etapa 2: rodar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV TZ=America/Sao_Paulo
ENTRYPOINT ["java", "-Xmx300m", "-XX:+UseSerialGC", "-jar", "app.jar"]
