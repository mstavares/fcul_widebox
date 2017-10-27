package client;

import java.rmi.RemoteException;
import java.util.HashMap;

public class ClientStore {
	
	private static HashMap<Integer, WideBoxClient> clients = new HashMap<Integer, WideBoxClient>();
	private static String serverIp = "localhost";
	private static int serverPort = 1090;
	
	public static WideBoxClient getClient(int id) throws RemoteException{
		if (clients.containsKey(id) )
			return clients.get(id);
		else{
			//TODO get ip and port from file
			WideBoxClient client = new WideBoxClient(id, serverIp, serverPort);
			clients.put(id, client);
			return client;
		}
	}
	
	public static void removeClient(int id){
		clients.remove(id);
	}
	
}
