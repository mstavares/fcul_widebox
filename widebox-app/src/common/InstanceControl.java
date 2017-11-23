package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InstanceControl extends Remote {
	
	/**
	 * Starts the server
	 * @return False if unsuccessful
	 * @throws RemoteException
	 */
	boolean startServer() throws RemoteException;
	
	/**
	 * Stops the server
	 * @return False if unsuccessful
	 * @throws RemoteException
	 */
	boolean stopServer() throws RemoteException;
	
	/**
	 * Indicates wether the server is Online of Offline
	 * @return True if Online, False otherwise
	 * @throws RemoteException
	 */
	boolean isOnline() throws RemoteException;
	
}
