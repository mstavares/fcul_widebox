package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import common.InstanceControl;

/**
 * A singleton that creates and stores InstanceControls of the given servers.
 */
public class InstanceStore {
	
	private HashMap<String, InstanceControl> instances;
	
	
	private InstanceStore() {
		instances = new HashMap<String, InstanceControl>();
	}
	
	
	private static class StaticHolder {
		static final InstanceStore INSTANCE = new InstanceStore();
	}
    
	
	public static InstanceStore getInstance(){
		return StaticHolder.INSTANCE;
	}
	
	
	public InstanceControl getInstanceControl(String ip, int port) throws RemoteException{
		String address = ip + ":" + port;
		
		if ( instances.containsKey(address) )
			return instances.get(address);
		else{
			try {
				Registry registry = LocateRegistry.getRegistry(ip, port);
				InstanceControl instanceControl = (InstanceControl) registry.lookup("InstanceControl");
				
				instances.put(address, instanceControl);
				return instanceControl;
			} catch (RemoteException | NotBoundException e) {
				throw new RemoteException("Error connecting to the server.");
			}
		}
	}
	
}
