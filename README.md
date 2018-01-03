# WideBox
Design and implementation of a distributed and resilient very-large scale interactive system

## RMI Instructions for the labs
The servers MUST have the `-Djava.rmi.server.hostname=<public-ip-of-the-server>` property set AND the rmiregistry should be started previously (and thus, port 1099 should not be used, or an `AlreadyBoundException` will be thrown). Failure to follow these steps will result in a "Connection Refused" error upon any RMI Invocation.

We include 2 .sh files to run the appservers and the dbservers which already sets the hostname argument automatically, so you can use these executables to run the servers, assuming you have the jar files.

To start the rmi registry, simply run `rmiregistry`. The terminal running this needs to remain open while the server is running.

By default, the appserver runs on port 1090 and the database server on port 1098, but if you want you can specify a new port as an optional argument when running the executables, just run `start.sh 8080`

## Running the Web Client
The web client was tested using two web servers, Wildfly and Tomcat, but it's currently suggested to use Wildfly.

There's also a way to create a standalone jar file of the web client that already contains a copy of the Wildfly Swarm web server. To create this standalone jar file, run `mv install` on the main project.
The `widebox-web-client-swarm.jar` file will be created at `WideBox/widebox-web-client/target`.

The client will be running at `http://localhost:8080/`.


## Provided Jars

We already provide pre-built executable jars of everything: the 3 text clients, the web client, the app server and the database server.
Since the web client jar file has 44 MB, if we can't upload it due to size limits, we'll place a text file with a link to download it instead.

To execute any of the jar files, you just need to use the standart java jar command, eg:
```shell
java -jar widebox-web-client-swarm.jar
```