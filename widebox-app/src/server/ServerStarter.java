package server;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.Debugger;
import common.InstanceControl;
import common.Utilities;

public class ServerStarter extends UnicastRemoteObject implements InstanceControl {

	private static final long serialVersionUID = -23490925764346709L;
	private boolean online;
	/* The serverInstance of this node */
	private WideBoxServerImpl serverInstance;
	
	public ServerStarter() throws RemoteException {
		super();
		online = false;
		startRegistry();
		startServer();
	}

	/* Creates the registry and binds this object to it.
	 * Ideally, the actual server implementation also uses this registry
	 */
	private void startRegistry() throws RemoteException {
		try {
			Registry registry = LocateRegistry.createRegistry( Utilities.getPort() );
			registry.bind("InstanceControl", this);
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
			this.serverInstance = new WideBoxServerImpl();
			this.online = true;
		} catch (IOException e) {
			throw new RemoteException("Error starting server instance");
			// Return false em vez de lançar uma excepção?
		} catch (NotBoundException e) {
			throw new RemoteException("Error starting server instance: Already Bound");
		}
		Debugger.log("Server successfuly started");
		return true;
	}

	@Override
	public boolean stopServer() throws RemoteException {
		if (!this.online)
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
