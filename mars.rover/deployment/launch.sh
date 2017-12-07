#!/bin/bash

nohup ./deployment/deploy.sh > /dev/null 2>&1 & echo $!
nohup ./deployment/takeThreadDumps.sh $! sanket 40 900

