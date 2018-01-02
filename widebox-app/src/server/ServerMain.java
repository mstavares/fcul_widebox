package server;

import java.io.IOException;
import java.rmi.RemoteException;

import common.Utilities;

public class ServerMain {

	public static void main(String[] args) throws IOException, RemoteException {
		
		try {
			if (args.length > 1)
				Utilities.setPort( Integer.parseInt(args[0]) );
			else
				Utilities.setPort(1090);
		}catch (Exception e) {
			System.out.println("Error with the port given.");
			System.exit(-1);
		}
		
		// ServerStarter provides the interface for the failure generator.
		new ServerStarter();
	}

}
