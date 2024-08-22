FROM openjdk:17
WORKDIR /app
COPY build/libs/asset-management-service-1.0.0.jar app.jar
CMD ["java", "-jar", "app.jar", "--spring.config.location=application.properties/application.properties"]

