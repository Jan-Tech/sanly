FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/sanly-education-1.0.0.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
