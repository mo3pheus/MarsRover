#!/bin/bash
echo "Deploying Mars Rover"
pwd=$(echo pwd)
dataArchiveLocation=$($pwd)/dataArchives
java -jar target/mars.rover-1.4-SOLSHOT-shaded.jar src/main/resources/marsConfig.properties src/main/resources/roverDB.properties src/main/resources/ $dataArchiveLocation $1

