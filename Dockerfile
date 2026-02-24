FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /build
COPY src/ src/
COPY pom.xml pom.xml
RUN mvn compile
RUN mvn dependency:copy-dependencies -DincludeScope=compile

FROM eclipse-temurin:25-jre-alpine
EXPOSE 8080
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app/
COPY --from=build /build/target/classes/ /app/
COPY --from=build /build/target/dependency/ /app/dependencies/
COPY www/ ./www/
USER appuser
ENTRYPOINT ["java", "-classpath", "/app:/app/dependencies/*", "org.example.App"]
