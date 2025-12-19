# Bazaar Bundles

A little micro-service to help you manage your marketing at those competitive medieval Bazaars.

## Getting Started
The easiest way to run this application for development is through either [devcontainers](https://containers.dev/) for development, or by using the docker compose stack for a release-like environment.

Alternatively for running manually you will need Java 17+, and a running instance of MySQL.

This app requires access to the **products API** service. You will need to get Basic Auth credentials, which is beyond the scope of this README.

### Docker Compose
The docker compose file in this directory contains a full instance of the stack. This can be done with the `docker compose up` command, however it's worth noting that you'll need to define your Products API credentials as env vas `PRODUCTS_API_USERNAME` and `PRODUCTS_API_PASSWORD` for username / password respectively.

You can either provide them as command line args while running docker compose, or add them as a `.env` file to this directory for automatic detection. The .env file is in the .gitignore so you shouldn't need to worry, but please take care not to commit any credentials to code. A sample .env looks like:
```.env
PRODUCTS_API_USERNAME=your_user_name
PRODUCTS_API_PASSWORD=your_password
```

### Dev Containers
For more information on dev container support, check with your IDE documentation to see how your IDE specifically launches dev containers.

### Starting the App

To launch the app from the command line using maven:
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