#!/bin/bash

echo "Downloading the missionCommand software"
wget https://storage.googleapis.com/rover_artifacts/mission.control.manual.command-1.2-shaded.jar

figlet commandCenter
figlet NASA Houston

java -jar mission.control.manual.command-1.2-shaded.jar



