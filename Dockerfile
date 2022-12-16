FROM openjdk:11-jdk-slim

WORKDIR /src
COPY . /src

RUN apt-get update
RUN apt-get install -y dos2unix
RUN dos2unix gradlew

RUN bash gradlew fatJar

WORKDIR /run
RUN cp /src/build/libs/*.jar /run/server.jar

EXPOSE 8001

CMD java -jar /run/server.jar