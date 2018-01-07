#!/bin/bash

figlet commandCenter
figlet NASA Houston

commandChoice="$1"

if [ $commandChoice = "manual" ]; then
    java -jar mission.control.manual.command-1.2-shaded.jar
else
    java -jar mission.control-producer-1.2-shaded.jar
fi





