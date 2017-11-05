##   m a r s R o v e r

The MarsRover project is essentially an educational tool to help software developers try out new algorithms and technologies in a fun and challenging environment. The project has 2 essential parts viz. 1) The Rover, 2) Mission Control and Command Center

1) The Rover is a softbot that you launch to the Mars surface and that you hope to control.
2) Mission Control displays the messages received from the rover, such as any findings or status reports or diagnostic heartbeats. Command Center composes and forms properly formatted messages for the rover to act upon.

Part 1 is this repository, part 2 is in a separate repository to help facilitate independent development.

### Rover

The rover is a softbot or a software robot with predefined capabilities. It is operates on the Mars surface through a series of well defined states. These states are as follows:
1) Listening State
2) Hibernating State
3) Moving State
4) Recharging State
5) Photographing State
6) Sensing State
7) Transmitting State 
8) Sleeping State
9) WeatherSensingState
10) ExploringState

Following is the rover equipment list:
1) Battery/ PowerUnit.
2) Propulsion Unit + TelemetrySensor*
3) Radio with transmitter and receiver*
4) Camera*^
5) Lidar*
6) Spectrometer*
7) WeatherSensor*^

* indicates that the equipment is supported with its own animationEngine. 
^ indicates that the data returned is from curiosityActual - i.e. the actual curiosityRover on Mars.

### Mission Control:
The MissionControl center operates as CommandAndControl for the rover. It gives the rover commands and duties to carry out and listens to the rover, collects its data and telemetry.

### Communication System:
The two project modules, rover and missionControl communicate through apache-kafka - so setting up kafka is essential to getting the project to work locally.

### Logging:
The rover logs all instructions it receives and all actions its performing. If the rover software crashes on a command, the logs will show you the stackTrace. You can tune logging to INFO or DEBUG and this is done by passing in a debugModeFlag at runtime. If debugMode is false, the logs will be saved at <HOME_DIRECTORY>/roverStatusReports, if its set to true, they will be saved at <HOME_DIRECTORY>/analysisLogs. Bear in mind that the kafka & zookeeper infrastructure generates a lot of noise when log is set to DEBUG mode - so its best to use the debug mode when you are trying to collect the logs during bugReproduction.

### Project Guidelines:
1) Write Java code, learn design patterns and best practices for software development.
2) Stay true to the underlying science of space travel and remote vehicle operation. 
3) Have fun incorporating newer sensors and capabilities for the rover.
4) Learn everything you can about Mars.
5) Morpheus, Neo and everything Matrix is cool! If you are not a Matrix fan - we wish you good luck and good bye.

### Project Contacts:
Sanket Korgaonkar
Email : sanket.korgaonkar@gmail.com
GitHub: http://github.com/mo3pheus 
