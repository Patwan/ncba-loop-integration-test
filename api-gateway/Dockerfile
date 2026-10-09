# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-26 AS build

WORKDIR /app

COPY pom.xml ./

# Cache mount keeps downloaded Maven dependencies between builds (even with --no-cache)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B


# ---- Runtime stage ----
FROM eclipse-temurin:26-jre

# Pull the latest Ubuntu security patches (fixes the expat/libexpat1 CVEs from AWS Inspector and future OS CVEs)
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]