# use a full image with mvn building
FROM maven:eclipse-temurin AS build

# install dependencies
RUN apt-get update && apt-get install -y cmake zlib1g-dev libssl-dev patch perl ninja-build build-essential

# copy the source code
COPY . /src
# change to dir
WORKDIR /src/tlstesttool/build
# prepare build and run it parallel with ninja
RUN cmake -G Ninja -DCMAKE_BUILD_TYPE=Release .. && ninja

# switch back to task tool
WORKDIR /src/task
# build the task tool
RUN mvn clean install

# only keep a jdk image and copy the build artifacts
FROM eclipse-temurin
WORKDIR /task/
COPY --from=build /src/task/com.achelos.task.commandlineinterface/target/com.achelos.task.commandlineinterface*-jar-with-dependencies.jar .
CMD ["bash"]

# Usage
# build via:
# docker build -t task .
# run via:
# docker run --rm -ti task
# root@40aac5e86de3:/task# java -jar com.achelos.task.commandlineinterface-0.4.2-jar-with-dependencies.jar
