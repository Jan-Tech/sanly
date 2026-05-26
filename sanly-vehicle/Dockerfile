FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/sanly-vehicle-1.0.0.jar app.jar
EXPOSE 8096
ENTRYPOINT ["java", "-jar", "app.jar"]
