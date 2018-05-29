#!/bin/bash

JAR_FILE=mars.rover-1.8-SOLSHOT-shaded.jar

figlet "marsRover"
echo "Launching marsRover " ${JAR_FILE}

sleep 10

MARS_CONFIG=marsConfig.properties
DB_CONFIG=roverDB.properties
PWD=$(echo pwd)
ARCHIVE_LOCATION=$(pwd)/dataArchives
RESOURCES_DIR=$(pwd)

JAVA_CMD=nohup java -Xms256m -Xmx1g -jar ${JAR_FILE} ${MARS_CONFIG} ${DB_CONFIG} ${RESOURCES_DIR} ${ARCHIVE_LOCATION} false &
$(JAVA_CMD)

sleep 5
./tailRoverLogs.sh

