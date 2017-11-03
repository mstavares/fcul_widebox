package common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ServerManager {
	
	private static ServerManager instance;
	
	private Map<String, List<Server>> servers;
	
	
	private ServerManager() throws Exception{
		servers = new HashMap<String, List<Server>>();
		//TODO fiz this messy code and the file
		try {
			Scanner sc = new Scanner(new File("config/servers.config") );
			String s;
			
			while (sc.hasNext()){
				String serverType = sc.next();
				ArrayList<Server> serverList = new ArrayList<Server>();
				
				while ( sc.hasNext() && !(s = sc.next()).equals("-") ){
					Server server = new Server(s, sc.nextInt() );
					serverList.add(server);
				}
				
				servers.put(serverType, serverList);
			}
			
			sc.close();
		} catch (Exception e) {
			throw new Exception("Error parsing servers.config file.");
		}
	}
	
	
	public static ServerManager getInstance() throws Exception{
		if (instance == null)
			instance = new ServerManager();
		return instance;
	}
	
	
	public Map<String, List<Server>> getAllServers() throws Exception{
		return servers;
	}
	
	
	public List<Server> getAppServers() throws Exception{
		return servers.get("AppServer");
	}
	
	
	public List<Server> getDatabaseServers() throws Exception{
		return servers.get("DatabaseServer");
	}
	
}