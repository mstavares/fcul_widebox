package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InstanceManager {
	
	private static InstanceManager instance;
	
	private Map<InstanceType, List<Server>> servers;
	
	
	private InstanceManager() throws Exception{
		servers = new HashMap<InstanceType, List<Server>>();
		//TODO fix this messy code and the file
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
				
				servers.put(InstanceType.valueOf(serverType), serverList);
			}
			
			sc.close();
		}catch (FileNotFoundException e){
			//TODO the folder/file needs to be on the bin folder of the web server for it to work, fix this somehow
			System.out.println("File not found. Full path: " + new File("config/servers.config").getAbsolutePath() );
			e.printStackTrace();
		} catch (Exception e) {
			throw new Exception("Error parsing servers.config file.");
		}
	}
	
	
	public static InstanceManager getInstance() throws Exception{
		if (instance == null)
			instance = new InstanceManager();
		return instance;
	}
	
	
	public Map<InstanceType, List<Server>> getAllServers(){
		return servers;
	}
	
	
	public List<Server> getServers(InstanceType type){
		return servers.get( type.getFileName() );
	}
	
}
