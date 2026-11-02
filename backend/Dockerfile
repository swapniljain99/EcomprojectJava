FROM maven:3.9-eclipse-temurin-17 AS build
ARG SERVICE
WORKDIR /build
COPY pom.xml ./
COPY product-service ./product-service
COPY cart-service ./cart-service
COPY credit-service ./credit-service
COPY payment-service ./payment-service
COPY order-service ./order-service
RUN mvn -q -pl ${SERVICE} -am package -DskipTests

FROM eclipse-temurin:17-jre
ARG SERVICE
COPY --from=build /build/${SERVICE}/target/${SERVICE}-1.0.0.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
