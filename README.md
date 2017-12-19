##   m a r s R o v e r

The MarsRover project is essentially an educational tool to help software developers try out new algorithms and technologies in a fun and challenging environment. The project has 2 essential parts viz. 1) The Rover, 2) Mission Control and Command Center

1) The Rover is a softbot that you launch to the Mars surface and that you hope to control.
2) Mission Control displays the messages received from the rover, such as any findings or status reports or diagnostic heartbeats. Command Center composes and forms properly formatted messages for the rover to act upon.
3) The Rover and Mission Control communicate with each other through a common communcations-protocol<1.0>.

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

### Rover Architecture Diagram:
![Rover Architecture](MarsRover/mars.rover/src/main/resources/roverArchitecture.jpg)

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

### Installation Instructions:

MarsRover installation steps:
      1) create a folder called SpaceExploration somewhere on your harddrive => here by referred to as <HOME>
        1.1) create a folder called Protobuf
        1.2) download the protoc into this folder. (linux binary) https://github.com/google/protobuf/releases/download/v3.4.0/protoc-3.4.0-linux-x86_32.zip
      2) clone https://github.com/mo3pheus/CommunicationsProtocol.git under <HOME> 
        2.1) open the pom.xml - and modify the protoc path to point to the file downloaded in 1.2 - run mvn clean install
      3) mvn install:install-file -Dfile=<HOME>/CommunicationsProtocol/target/communications.protocol-1.0.jar -Dpackaging=jar -DgroupId=space.exploration -DartifactId=communications.protocol -Dversion=1.0
      4) clone https://github.com/mo3pheus/MarsRover.git under <HOME>
        4.1 edit <HOME>.mars.rover/src/main/resources/marsConfig.properties and change mars.rover.weather.station.image to <HOME>/mars.rover/src/main/resources/sunIcon.jpg
      5) mvn clean install
      6) clone https://github.com/mo3pheus/Mission.Control.git under <HOME>
      7) mvn clean install
      8) Download kafka-jar -> http://apache.mesi.com.ar/kafka/0.10.2.1/kafka_2.10-0.10.2.1.tgz
      9) Extract kafka-jar into <HOME>/kafka
      10) open 5 terminal windows - here by referred to as t-i(0-4)
        10.1 - in t0 cd into <HOME>/kafka - run ./bin/zookeeper-server-start.sh config/zookeeper.properties - if you have trouble with this run the same with sudo.
        10.2 - in t1 cd into <HOME>/kafka - run ./bin/kafka-server-start.sh config/server.properties - if you run into trouble with this run the same with sudo.
        10.3 - in t2 cd into <HOME>mars.rover open deployment/deploy.sh - modify any hardcoded paths to <HOME>mars.rover/src/main/resources/
        10.3.1 - in t4 cd into <HOME>mars.rover/roverStatusReports - run ll and tail the last file in the list - this will give you a view into the inner workings of the rover OS.
        10.4 - in t2 modify src/main/resources/roverDB.properties - mars.rover.database.logging.enable=false - make sure this property is set to false
        10.5 - in t2 run ./deployment/deploy.sh false
        10.6 - in t3 cd into <HOME>/mission.control - run java -jar target/mission.control-0.0.1-SNAPSHOT-shaded.jar src/main/resources/kafka.properties dataArchives/ false
        10.7 - open <HOME>/mission.control/pom.xml as a PROJECT in intelliJ and run Producer.java as java Application
      11) At this point, your java application in 10.7 should send commands to the marsRover, the marsRover should act upon it - and send feedback (data+heartBeat) to 10.6
### Installation Instructions:
If the any application crashes please raise an issue at https://github.com/mo3pheus/MarsRover/projects/1 along with a copy of the roverStatusReports/roverStatus_<timestamp>.log file.

