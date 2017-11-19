package client;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

public class ClientStore {
	
	private ConcurrentHashMap<Integer, WideBoxClient> clients;
	
	
	private ClientStore(){
		clients = new ConcurrentHashMap<Integer, WideBoxClient>();
	}
	
	
	private static class StaticHolder {
		static final ClientStore INSTANCE = new ClientStore();
	}
    
	
	public static ClientStore getInstance(){
		return StaticHolder.INSTANCE;
	}
	
	
	public WideBoxClient getClient(int id) throws RemoteException{
		if (clients.containsKey(id) )
			return clients.get(id);
		else{
			WideBoxClient client = new WideBoxClient(id);
			clients.put(id, client);
			return client;
		}
	}
	
	
	public void removeClient(int id){
		clients.remove(id);
	}
	
}
