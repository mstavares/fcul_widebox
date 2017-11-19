package client;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

import common.InstanceManager;
import common.InstanceType;

public class ClientStore {
	
	private static ClientStore instance;
	
	private ConcurrentHashMap<Integer, WideBoxClient> clients;
	private String serverIp;
	private int serverPort;
	
	
	private ClientStore() throws Exception{
		//TODO multi server support
		clients = new ConcurrentHashMap<Integer, WideBoxClient>();
		InstanceManager serverManager = InstanceManager.getInstance();
		serverIp = serverManager.getServers(InstanceType.APP).get(0).getIp();
		serverPort = serverManager.getServers(InstanceType.APP).get(0).getPort();
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
