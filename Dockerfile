FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /build
COPY src/ src/
COPY pom.xml pom.xml
RUN mvn compile

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /build/target/classes/ /app/
ENTRYPOINT ["java", "-classpath", "/app", "org.example.App"]
USER appuser
