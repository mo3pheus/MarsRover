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

echo "Download marsRover project!"
wget https://storage.googleapis.com/rover_artifacts/mars.rover-1.5-SOLSHOT-shaded.jar
wget https://storage.googleapis.com/rover_artifacts/marsConfig.properties
wget https://storage.googleapis.com/rover_artifacts/roverDB.properties
wget https://storage.googleapis.com/rover_artifacts/launch.sh

echo "Download the Mission.Control application"
wget https://storage.googleapis.com/rover_artifacts/mission.control-1.2-shaded.jar

echo "Downloading the missionCommand software"
wget https://storage.googleapis.com/rover_artifacts/mission.control.manual.command-1.2-shaded.jar

echo "Downloading the missionCommand software"
wget https://storage.googleapis.com/rover_artifacts/mission.control-producer-1.2-shaded.jar

echo "Get the launch script"
wget https://storage.googleapis.com/rover_artifacts/linuxLaunch.sh
chmod +x linuxLaunch.sh

echo "Execute the launch script"
./linuxLaunch.sh $1
