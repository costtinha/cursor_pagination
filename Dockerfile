# Etapa 1: construir a aplicação com Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: rodar o JAR empacotado
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /build/target/tcc-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
