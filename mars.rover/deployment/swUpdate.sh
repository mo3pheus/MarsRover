#!/bin/bash

VERSION=$1

figlet "marsRover"
echo "MarsRover starting after a software update."
echo "Upgrading marsRover to version :: " + ${VERSION}
cp softwareUpdate/*.txt target/

pwd=$(echo pwd)
dataArchiveLocation=$($pwd)/dataArchives
nohup java -jar target/mars.rover-1.5-SOLSHOT-shaded.jar src/main/resources/marsConfig.properties src/main/resources/roverDB.properties src/main/resources/ $dataArchiveLocation false &

