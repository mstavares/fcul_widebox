package client;

import java.rmi.RemoteException;
import java.util.HashMap;

import common.InstanceManager;

public class ClientStore {
	
	private static ClientStore instance;
	
	private static HashMap<Integer, WideBoxClient> clients = new HashMap<Integer, WideBoxClient>();
	private static String serverIp;
	private static int serverPort;
	
	
	private ClientStore() throws Exception{
		//TODO multi server support
		InstanceManager serverManager = InstanceManager.getInstance();
		serverIp = serverManager.getAppServers().get(0).getIp();
		serverPort = serverManager.getAppServers().get(0).getPort();
	}
	
	
	public static ClientStore getInstance() throws Exception{
		//TODO possiveis problemas de concurrencia?
		if (instance == null)
			instance = new ClientStore();
		return instance;
	}
	
	
	public WideBoxClient getClient(int id) throws RemoteException{
		if (clients.containsKey(id) )
			return clients.get(id);
		else{
			WideBoxClient client = new WideBoxClient(id, serverIp, serverPort);
			clients.put(id, client);
			return client;
		}
	}
	
	
	public void removeClient(int id){
		clients.remove(id);
	}
	
}
