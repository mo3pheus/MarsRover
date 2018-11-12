#!/bin/bash
# import time
echo "Deploying Mars Rover"
pwd=$(echo pwd)
dataArchiveLocation=$($pwd)/dataArchives
nohup java -jar target/mars.rover-2.1-SOLSHOT-shaded.jar src/main/resources/marsConfig.properties src/main/resources/roverDB.properties src/main/resources/ $dataArchiveLocation $1 zion-portable &
# gnome-terminal --command="./deployment/tailRoverLogs.sh"
sleep 5
./deployment/tailRoverLogs.sh
