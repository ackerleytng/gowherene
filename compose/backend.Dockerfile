FROM openjdk:8-jre-slim

COPY ./target/gowherene-0.1.0-SNAPSHOT-standalone.jar /srv/gowherene.jar

ENTRYPOINT ["java", "-jar", "/srv/gowherene.jar"]