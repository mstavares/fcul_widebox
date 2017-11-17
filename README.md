# fcul_widebox
WideBox: Design and implementation of a distributed and resilient very-large scale interactive system

# RMI Instructions for the labs
Application servers MUST have the "-Djava.rmi.server.hostname=<public-ip-of-the-server>" property set AND the rmiregistry should be started previously (and thus, port 1099 should not be used, or an "AlreadyBoundException" will be thrown. Failure to follow these steps will result in a "Connection Refused" error upon any RMI Invocation

PSA: Currently the config/server.properties file needs to be placed on the bin directory of the web server for it to work. It's advisable to create a symlink there to the actual file.
