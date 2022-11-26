FROM openjdk:11-jdk

ARG JAR_FILE=./build/libs/coinkaraoke-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080
EXPOSE 7054

ENTRYPOINT ["java","-jar","/app.jar"]
