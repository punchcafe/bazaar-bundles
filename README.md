# Bazaar Bundles

A little micro-service to help you manage your marketing at those competitive medieval Bazaars.

## Getting Started
The easiest way to run this application is through [devcontainers](https://containers.dev/). This window can be opened in a ready to go environment.

Alternatively for running manually you will need Java 17+, and a running instance of MySQL.

This app requires access to the **products API** service. You will need to get Basic Auth credentials, which is beyond the scope of this README. 

To launch the app:
```bash
$ > PRODUCTS_API_USERNAME="<...credentials...>" PRODUCTS_API_PASSWORD="<...credentials...>" ./mvnw spring-boot:run
```

The app will, by default, use the credentials configured for the devcontainer when connecting to MySQL. If you need to override this, use environment variables when starting the app:
```bash
$ > SPRING_DATASOURCE_URL="jdbc:<...your connection url>" \
    SPRING_DATASOURCE_URL="<...your username...>" \
    SPRING_DATASOURCE_PASSWORD="<...your password...>" \
    PRODUCTS_API_USERNAME="<...credentials...>" \
    PRODUCTS_API_PASSWORD="<...credentials...>" \
    ./mvnw spring-boot:run
```

Once running, the app will be available on `http://localhost:8080`.

## Postman
This app has a postman collection to help get familiar with the API.