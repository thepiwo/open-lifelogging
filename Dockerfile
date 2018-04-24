FROM openjdk:8 as build

ENV SCALA_VERSION 2.12.5
ENV SBT_VERSION 1.1.2

# FROM https://github.com/hseeberger/scala-sbt
# Scala expects this file
RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

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


FROM openjdk:8-jre-alpine as server

ENV APP_VERSION 0.0.2

WORKDIR /app
COPY --from=build /app/target/scala-2.12/open-lifelogging-assembly-$APP_VERSION.jar /app/open-lifelogging.jar

EXPOSE 9001
CMD ["java", "-jar", "/app/open-lifelogging.jar"]
