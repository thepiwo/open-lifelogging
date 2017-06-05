FROM openjdk:8

ENV SCALA_VERSION 2.12.2
ENV SBT_VERSION 0.13.15

# FROM https://github.com/hseeberger/scala-sbt
# Install Scala
RUN \
  curl -fsL http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> /root/.bashrc

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

EXPOSE 9000
CMD ["java", "-jar", "target/scala-2.12/open-lifelogging-assembly-0.0.2.jar"]
