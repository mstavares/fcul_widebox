#! /bin/bash
echo "Current ip is $(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p')"
echo "Starting WideBox Application Server..."
java -Djava.rmi.server.hostname=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p') -jar widebox-appserver.jar
