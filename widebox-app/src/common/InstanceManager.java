package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This singleton reads the information about all the server instances from the
 * servers.config file, and saves it to provite it to whoever needs it.
 */
public class InstanceManager {
	
	private Map<InstanceType, List<Server>> servers;
	
	private InstanceManager(){
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
			System.out.println("servers.config file not found.\n Looking for it in this path: " + new File("config/servers.config").getAbsolutePath() );
			System.exit(-1);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing servers.config file.");
			System.exit(-1);
		}
	}
	
	
	private static class StaticHolder {
		static final InstanceManager INSTANCE = new InstanceManager();
	}
    
	
	public static InstanceManager getInstance(){
		return StaticHolder.INSTANCE;
	}
	
	
	public Map<InstanceType, List<Server>> getAllServers(){
		return servers;
	}
	
	
	public List<Server> getServers(InstanceType type){
		return servers.get(type);
	}
	
}
