FROM adoptopenjdk/openjdk11:jre-11.0.9_11.1-alpine
VOLUME /tmp
ADD data-service-broker-2.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=local","-jar","/app.jar"]
