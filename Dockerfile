FROM openjdk:11-jre-slim
ADD build/libs/twitteo-0.0.1-SNAPSHOT.jar twitteo.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "twitteo.jar"]
