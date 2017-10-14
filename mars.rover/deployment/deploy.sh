#!/bin/bash
echo "Deploying Mars Rover"
java -jar target/mars.rover-0.0.1-SOLSHOT-shaded.jar src/main/resources/marsConfig.properties src/main/resources/kafka.properties src/main/resources/roverDB.properties src/main/resources/ dataArchives

