package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import common.InstanceControl;

public class InstanceStore {
	
	private static InstanceStore instance;
	
	private static HashMap<String, InstanceControl> instances;
	
	
	private InstanceStore() {
		instances = new HashMap<String, InstanceControl>();
	}
	
	
	public static InstanceStore getInstance(){
		//TODO possiveis problemas de concurrencia?
		if (instance == null)
			instance = new InstanceStore();
		return instance;
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
