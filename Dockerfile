# ---------- Стадия сборки ----------
FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# ---------- Стадия запуска ----------
FROM eclipse-temurin:21.0.12.1_1-jre-alpine
WORKDIR /app

RUN adduser -S -H -u 1001 appuser
COPY --from=build /app/target/task-tracker-1.0.0.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
