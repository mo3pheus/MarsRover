#!/usr/bin/env bash

# make all required directories
echo "Make the required directory structure"
mkdir statusLogs
mkdir analysisLogs
mkdir roverStatusReports
mkdir dataArchives
mkdir dataArchives/CHEMCAM
mkdir dataArchives/FHAZ
mkdir dataArchives/MAHLI
mkdir dataArchives/MAST
mkdir dataArchives/NAVCAM
mkdir dataArchives/RHAZ
mkdir src
mkdir src/main
mkdir src/main/resources
mkdir src/main/resources/WeatherDataArchive

echo "Using homebrew to install necessary programs"
brew install wget
brew install gzip
brew install figlet

echo "Download required image files"
wget https://storage.googleapis.com/rover_artifacts/camImages.tar.gzip
tar -xzf camImages.tar.gzip

echo "Download kafka"
wget https://storage.googleapis.com/rover_artifacts/kafka_2.10-0.10.0.1.tar.gz

echo "Unzipping kafka"
tar -xzf kafka_2.10-0.10.0.1.tar.gz

echo "Start zookeeper"
nohup ./kafka_2.10-0.10.0.1/bin/zookeeper-server-start.sh kafka_2.10-0.10.0.1/config/zookeeper.properties > zookeeperLogs.log &

echo "Start kafka"
nohup ./kafka_2.10-0.10.0.1/bin/kafka-server-start.sh kafka_2.10-0.10.0.1/config/server.properties > kafkaLogs.log &

echo "Download marsRover project!"
wget https://storage.googleapis.com/rover_artifacts/mars.rover-1.5-SOLSHOT-shaded.jar
wget https://storage.googleapis.com/rover_artifacts/marsConfig.properties
wget https://storage.googleapis.com/rover_artifacts/roverDB.properties

echo "Download the Mission.Control application"
wget https://storage.googleapis.com/rover_artifacts/mission.control-1.2-shaded.jar

echo "Launch the Mission.Command application"
wget https://storage.googleapis.com/rover_artifacts/mission.control-producer-1.2-shaded.jar

echo "Deploying Mars Rover"
pwd=$(echo pwd)
dataArchiveLocation=$($pwd)/dataArchives
nohup java -jar mars.rover-1.5-SOLSHOT-shaded.jar marsConfig.properties roverDB.properties camImages/ $dataArchiveLocation false > roverConsoleLog.log &

echo "Deploying MissionControl - this is where you will see the received messages"
nohup java -jar mission.control-1.2-shaded.jar statusLogs/ false > missionControl.log &

echo "Deploying MissionCommand - this process sends commands to the rover and exercises various sensors."
java -jar mission.control-producer-1.2-shaded.jar > missionCommand.log &

echo "Start tailing nohup"
figlet Mission Control - H O U S T O N
tail -f missionControl.log
