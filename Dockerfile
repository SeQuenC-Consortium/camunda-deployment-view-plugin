FROM maven:3.9.4-eclipse-temurin-11-focal as builder
COPY ./ /app
WORKDIR /app
RUN mvn install -DskipTests -f pom.xml
RUN ls -la /app/target

FROM camunda/camunda-bpm-platform:run-7.20.0-SNAPSHOT
COPY --chown=camunda:camunda --from=builder /app/target/camunda-deployment-view-plugin-1.0-SNAPSHOT.jar /camunda/configuration/userlib/camunda-deployment-view-plugin-1.0-SNAPSHOT.jar