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
FROM eclipse-temurin:17.0.8_7-jre

# expose server port
EXPOSE 8080

# download script for reading docker secrets
ADD https://raw.githubusercontent.com/HSLdevcom/jore4-tools/main/docker/read-secrets.sh /app/scripts/read-secrets.sh

# copy over compiled jar
COPY --from=builder /build/target/*.jar /usr/src/jore4-auth/auth-backend.jar

# read docker secrets into environment variables and run application
CMD /bin/bash -c "source /app/scripts/read-secrets.sh && java -jar /usr/src/jore4-auth/auth-backend.jar"

HEALTHCHECK --interval=1m --timeout=5s \
  CMD curl --fail http://localhost:8080/actuator/health
