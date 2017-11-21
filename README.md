# fcul_widebox
WideBox: Design and implementation of a distributed and resilient very-large scale interactive system

# RMI Instructions for the labs
Application servers MUST have the "-Djava.rmi.server.hostname=<public-ip-of-the-server>" property set AND the rmiregistry should be started previously (and thus, port 1099 should not be used, or an "AlreadyBoundException" will be thrown. Failure to follow these steps will result in a "Connection Refused" error upon any RMI Invocation

PSA: Currently the config/server.properties file needs to be placed on the bin directory of the web server for it to work. It's advisable to create a symlink there to the actual file.

# Running the Web Client
The web client was tested using two application servers, Wildfly and Tomcat, but it's currently suggested to use Tomcat.

There's a way to create a standalone jar file of the web client that already contains a copy of the Tomcat 7 web server. To create this standalone jar file, run `mv install` on the main project, and then `mv package` on the web-client subproject.
The widebox-web-client.jar file will be created in the target folder of the web-client subproject.

The client will be running at `http://localhost:8080/widebox-web-client/`.

It's possible to change the default port of the web server by adding the `-httpPort`argument when running the jar, for example:
```shell
java -jar widebox-web-client.jar -httpPort=7070
```