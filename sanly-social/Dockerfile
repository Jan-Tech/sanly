FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/sanly-social-*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
