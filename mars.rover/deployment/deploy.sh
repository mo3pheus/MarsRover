#!/bin/bash
echo "Deploying Mars Rover"
java -jar target/mars.rover-1.2-SOLSHOT-shaded.jar src/main/resources/marsConfig.properties src/main/resources/roverDB.properties src/main/resources/ /home/sanket/Documents/workspace/MarsRover/mars.rover/dataArchives $1

