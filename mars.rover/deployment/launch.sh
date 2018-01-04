#!/bin/bash

echo "Deploying Mars Rover"
pwd=$(echo pwd)
dataArchiveLocation=$($pwd)/dataArchives
nohup java -jar mars.rover-mac-distro-1.5-SOLSHOT-shaded.jar marsConfig.properties roverDB.properties camImages/ $dataArchiveLocation false > roverConsoleLog.log &

echo "Deploying MissionControl - this is where you will see the received messages"
nohup java -jar mission.control-1.2-shaded.jar statusLogs/ false > missionControl.log &

echo "Deploying MissionCommand - this process sends commands to the rover and exercises various sensors."
java -jar mission.control-producer-1.2-shaded.jar -o missionCommand.log > missionCommand.log &

echo "Start tailing nohup"
figlet Mission Control - H O U S T O N
tail -f missionControl.log

