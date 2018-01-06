#!/bin/bash

echo "Start zookeeper"
nohup ./kafka_2.10-0.10.0.1/bin/zookeeper-server-start.sh kafka_2.10-0.10.0.1/config/zookeeper.properties > zookeeperLogs.log &
sleep 10

echo "Start kafka"
nohup ./kafka_2.10-0.10.0.1/bin/kafka-server-start.sh kafka_2.10-0.10.0.1/config/server.properties > kafkaLogs.log &
sleep 5

echo "Deploying Mars Rover"
pwd=$(echo pwd)
dataArchiveLocation=$($pwd)/dataArchives
nohup java -jar mars.rover-1.5-SOLSHOT-shaded.jar marsConfig.properties roverDB.properties camImages/ $dataArchiveLocation false > roverConsoleLog.log &
sleep 15

echo "Grab the script for tailing roverLogs"
wget https://storage.googleapis.com/rover_artifacts/tailRoverLogs.sh
chmod +x tailRoverLogs.sh

echo "Launching a window to tail roverSystemLogs - these contain useful information about the roverState"
gnome-terminal --command="./tailRoverLogs.sh"

echo "Deploying MissionControl - this is where you will see the received messages"
nohup java -jar mission.control-1.2-shaded.jar statusLogs/ false > missionControl.log &
sleep 10

echo "Grab the script to get the command control software"
wget https://storage.googleapis.com/rover_artifacts/launchMissionCommand.sh
chmod +x launchMissionCommand.sh

echo "Deploying MissionCommand - this process sends commands to the rover and exercises various sensors."
gnome-terminal --command="./launchMissionCommand.sh"

echo "Grab the coverageGaps.txt file for research"
wget https://storage.googleapis.com/rover_artifacts/coverageGaps.txt

echo "Start tailing nohup"
figlet NASA  HOUSTON
figlet missionControl
tail -f missionControl.log

