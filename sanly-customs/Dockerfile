FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/sanly-customs-*.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
