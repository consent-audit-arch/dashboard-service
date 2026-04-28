FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY dashboard_query_service/pom.xml .
RUN mvn dependency:go-offline -B

COPY dashboard_query_service/src ./src

RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy

RUN groupadd -r appuser && useradd -r -g appuser -m appuser

WORKDIR /app

COPY --from=build /app/target/dashboard_query_service-0.0.1-SNAPSHOT.jar app.jar

RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS=

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
