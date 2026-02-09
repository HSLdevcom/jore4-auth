# builder docker image
FROM maven:3-eclipse-temurin-17 AS builder

# set up workdir
WORKDIR /build

# download dependencies
COPY ./pom.xml /build
RUN mvn de.qaware.maven:go-offline-maven-plugin:resolve-dependencies

# build
COPY ./src /build/src
COPY ./profiles/prod /build/profiles/prod
RUN mvn clean package spring-boot:repackage -Pprod

# distributed docker image
FROM eclipse-temurin:25.0.2_10-jre

# Application Insights version
ARG APPINSIGHTS_VERSION=3.7.7

# expose server port
EXPOSE 8080

# download script for reading docker secrets
ADD --chmod=755 https://raw.githubusercontent.com/HSLdevcom/jore4-tools/main/docker/read-secrets.sh /app/scripts/read-secrets.sh

# Connection string is provided as env in Kubernetes by secrets manager
# it should not be provided for other environments (local etc)
ADD --chmod=755 https://github.com/microsoft/ApplicationInsights-Java/releases/download/${APPINSIGHTS_VERSION}/applicationinsights-agent-${APPINSIGHTS_VERSION}.jar /usr/src/jore4-auth/applicationinsights-agent.jar
COPY --chmod=755 ./applicationinsights.json /usr/src/jore4-auth/applicationinsights.json

# copy over compiled jar
COPY --from=builder /build/target/*.jar /usr/src/jore4-auth/auth-backend.jar

# read docker secrets into environment variables and run application
CMD ["/bin/bash", "-c", "source /app/scripts/read-secrets.sh && java -javaagent:/usr/src/jore4-auth/applicationinsights-agent.jar -jar /usr/src/jore4-auth/auth-backend.jar"]

HEALTHCHECK --interval=1m --timeout=5s \
  CMD curl --fail http://localhost:8080/actuator/health
