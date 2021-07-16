FROM gradle:6.5-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootRepackage --no-daemon

FROM openjdk:8-jre-slim
EXPOSE 8080

COPY --from=build /home/gradle/src/budgeteer-web-interface/build/libs/*.war /budgeteer.war

ENTRYPOINT ["java","-jar","/budgeteer.war"]
