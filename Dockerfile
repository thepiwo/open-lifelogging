FROM openjdk:16-jdk-buster as build

ENV SCALA_VERSION 2.13.5
ENV SBT_VERSION 1.5.0

# FROM https://github.com/hseeberger/scala-sbt
# Install Scala
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# Install sbt
RUN \
  curl -fsL "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar xfz - -C /usr/share && \
  chown -R root:root /usr/share/sbt && \
  chmod -R 755 /usr/share/sbt && \
  ln -s /usr/share/sbt/bin/sbt /usr/local/bin/sbt

WORKDIR /app
COPY . /app

RUN sbt assembly


FROM openjdk:16-alpine as server

ENV APP_VERSION 0.0.3

WORKDIR /app
COPY --from=build /app/target/scala-2.13/open-lifelogging-assembly-$APP_VERSION.jar /app/open-lifelogging.jar

EXPOSE 9001
CMD ["java", "-jar", "/app/open-lifelogging.jar"]
