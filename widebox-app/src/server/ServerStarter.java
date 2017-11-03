package server;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.InstanceControl;

public class ServerStarter extends UnicastRemoteObject implements InstanceControl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -23490925764346709L;
	private boolean online;
	private WideBoxServerImpl serverInstance;
	private String serverIp;
	private int serverPort;
	
	public ServerStarter(String host, int port) throws RemoteException {
		super();
		this.serverIp = host;
		this.serverPort = port;
		online = false;
		startRegistry();
		startServer();
	}

	private void startRegistry() throws RemoteException {
		try {
			Registry registry = LocateRegistry.createRegistry(1090);
			registry.bind("ServerStarter", this);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
			throw new RemoteException("Error creating registry");
		}
		
	}

	@Override
	public boolean startServer() throws RemoteException {
		if (this.online)
			return false;
		try {
			this.serverInstance = new WideBoxServerImpl(serverIp, serverPort);
			this.online = true;
		} catch (IOException e) {
			throw new RemoteException("Error starting server instance");
			// Return false em vez de lançar uma excepção?
		}
		return true;
	}

	@Override
	public boolean stopServer() throws RemoteException {
		if (!this.online)
			return false;
		this.serverInstance.unbind();
		this.serverInstance = null;
		this.online = false;
		return true;
	}

	@Override
	public boolean isOnline() throws RemoteException {
		return this.online;
	}

}
