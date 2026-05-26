FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/sanly-land-1.0.0.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
