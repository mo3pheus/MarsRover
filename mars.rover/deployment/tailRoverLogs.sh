#!/bin/bash
figlet roverLogs

unset -v latest
for file in "roverStatusReports"/*; do
  [[ $file -nt $latest ]] && latest=$file
done

echo Tailing $latest
tail -f "$latest"
