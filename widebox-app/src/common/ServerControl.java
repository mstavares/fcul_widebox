package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerControl extends Remote {
	
	boolean startServer() throws RemoteException;
	
	boolean stopServer() throws RemoteException;
	
	boolean isOnline() throws RemoteException;
	
}
