FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/sanly-pension-1.0.0.jar app.jar
EXPOSE 8099
ENTRYPOINT ["java", "-jar", "app.jar"]
