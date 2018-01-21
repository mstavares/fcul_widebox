package database;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.Debugger;
import common.InstanceControl;
import common.Utilities;

public class DatabaseStarter extends UnicastRemoteObject implements InstanceControl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1344731779778569015L;
	private boolean online;
	public  WideBoxDatabaseImpl serverInstance;
	
	protected DatabaseStarter() throws RemoteException {
		super();
		
		online = false;
		startRegistry();
		startServer();
	}

	private void startRegistry() {
		try {
			Registry registry = LocateRegistry.createRegistry(Utilities.getPort());
			registry.bind("InstanceControl", this);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean startServer() throws RemoteException {
		if (this.online)
			return false;
		try {
			this.serverInstance = new WideBoxDatabaseImpl();
			this.online = true;
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			//Retornar false em vez de lançar a excepção?
			throw new RemoteException("Error starting Database Server Instance");
		}
		Debugger.log("Server successfuly started");
		return true;
	}

	@Override
	public boolean stopServer() throws RemoteException {
		if(!this.online)
			return false;
		this.serverInstance.unbind();
		this.serverInstance = null;
		this.online = false;
		Debugger.log("Server successfuly stopped");
		return true;
	}

	@Override
	public boolean isOnline() throws RemoteException {
		return this.online;
	}

}
