FROM openjdk:15-jdk-buster as build

ENV SCALA_VERSION 2.13.2
ENV SBT_VERSION 1.3.10

# FROM https://github.com/hseeberger/scala-sbt
# Install Scala
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

WORKDIR /app
COPY . /app

RUN sbt assembly


FROM openjdk:15-alpine as server

ENV APP_VERSION 0.0.3

WORKDIR /app
COPY --from=build /app/target/scala-2.13/open-lifelogging-assembly-$APP_VERSION.jar /app/open-lifelogging.jar

EXPOSE 9001
CMD ["java", "-jar", "/app/open-lifelogging.jar"]
