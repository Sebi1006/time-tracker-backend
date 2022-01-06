FROM maven:3.6-alpine as DEPS

WORKDIR /opt/app

COPY web/pom.xml web/pom.xml
COPY common/pom.xml common/pom.xml
COPY tenant-manager/pom.xml tenant-manager/pom.xml
COPY user-manager/pom.xml user-manager/pom.xml

COPY pom.xml .
RUN mvn -B -e -C org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline

FROM maven:3.6-alpine as BUILDER

WORKDIR /opt/app

COPY --from=deps /root/.m2 /root/.m2
COPY --from=deps /opt/app/ /opt/app

COPY web/src /opt/app/web/src
COPY common/src /opt/app/common/src
COPY tenant-manager/src /opt/app/tenant-manager/src
COPY user-manager/src /opt/app/user-manager/src

RUN mvn -B -e clean install -DskipTests=true

FROM openjdk:8-alpine

WORKDIR /opt/app

COPY --from=builder /opt/app/web/target/web-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/opt/app/web-0.0.1-SNAPSHOT.jar" ]
