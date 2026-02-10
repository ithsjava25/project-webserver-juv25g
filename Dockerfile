FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /build
COPY src/ src/
COPY pom.xml pom.xml
RUN mvn compile

FROM eclipse-temurin:25-jre-alpine
COPY --from=build /build/target/classes/org/example/CLASSNAME.class /app/org/example/CLASSNAME.class
ENTRYPOINT ["java", "-classpath", "/app", "org.example.CLASSNAME"]
