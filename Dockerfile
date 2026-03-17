FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /build
COPY src/ src/
COPY pom.xml pom.xml
RUN mvn compile
RUN mvn dependency:copy-dependencies -DincludeScope=compile

FROM eclipse-temurin:25-jre-alpine
ARG PORT=8080
EXPOSE $PORT
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app/
COPY --from=build /build/target/classes/ /app/
COPY --from=build /build/target/dependency/ /app/dependencies/
COPY www/ ./www/
ENV HEALTHCHECK_PORT=$PORT
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD sh -c 'curl -fsS "http://localhost:${HEALTHCHECK_PORT}/" || exit 1'
RUN apk --no-cache add curl
USER appuser
ENTRYPOINT ["java", "-classpath", "/app:/app/dependencies/*", "org.example.App"]