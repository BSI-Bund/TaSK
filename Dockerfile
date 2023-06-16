# If docker was installed using snap on ubuntu please do the following:
# - edit /var/snap/docker/current/config/daemon.json and replace “overlay2” with “vfs” for the storage-driver
# - run sudo snap restart docker

FROM ubuntu:22.04

# noninteractive to avoid tzdata prompt
ENV DEBIAN_FRONTEND noninteractive

# install dependencies
# make the task directory /task
# make report directory in /task/reports

RUN apt-get update && \
    apt-get install -y openjdk-17-jdk build-essential cmake maven python3 unzip wget ninja-build && \
    mkdir -p /task/reports &&\
    mkdir -p /task/src/task &&\
    mkdir -p /task/src/tlstesttool/build


# versions of packages
# Apache Maven 3.6.3
# java 17
# OpenSSL 3.0.2 15 Mar 2022 (Library: OpenSSL 3.0.2 15 Mar 2022)
# python3.8

# get the sources expect files from .dockerignore
COPY . /task/src/
# change to dir
WORKDIR /task/src/tlstesttool/build
# prepare build and run it parallel with ninja
RUN cmake -G Ninja -DCMAKE_BUILD_TYPE=Release .. && ninja
# switch to task
WORKDIR /task/src/task
# build the task tool and clean up the apt cache
RUN mvn clean install && \
    rm -rf /var/lib/apt/lists/*

# start task as a REST service
ENTRYPOINT ["java","-jar","/task/src/task/com.achelos.task.commandlineinterface/target/com.achelos.task.commandlineinterface-1.0.1-jar-with-dependencies.jar",  "-c" , "/task/src/task/docker/GlobalConfig.xml",  "-s" ]
