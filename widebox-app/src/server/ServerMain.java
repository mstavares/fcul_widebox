package server;

import java.io.IOException;
import java.rmi.RemoteException;

public class ServerMain {

	public static void main(String[] args) throws IOException, RemoteException {
		// ServerStarter provides the interface for the failure generator.
		new ServerStarter();
	}

}
