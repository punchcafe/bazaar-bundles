FROM openjdk:26-ea-17-slim-trixie AS jar
WORKDIR /src
COPY . .
RUN ["./mvnw", "-Dmaven.test.skip=true", "package"]

FROM openjdk:26-ea-17-slim-trixie
WORKDIR app
COPY --from=jar /src/target/bazaar-0.0.1-SNAPSHOT.jar /app/app.jar
COPY config/application.yml /app/config/application.yml
CMD ["java", "-jar", "app.jar"]