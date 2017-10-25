package server;

import java.rmi.RemoteException;

public class ServerMain {
	
	public static void main(String[] args) throws RemoteException {
		new WideBoxServerImpl(args[0], Integer.parseInt(args[1]));
	}
	
}
