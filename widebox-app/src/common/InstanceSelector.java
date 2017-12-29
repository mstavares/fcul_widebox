package common;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Singleton used to get the details of the instance of a server serving a theater.
 */
public class InstanceSelector {
	
	private InstanceManager instanceManager;
	
	private InstanceSelector(){
		instanceManager = InstanceManager.getInstance();
	}
	
	
	private static class StaticHolder {
		static final InstanceSelector INSTANCE = new InstanceSelector();
	}
    
	
	public static InstanceSelector getInstance(){
		return StaticHolder.INSTANCE;
	}
	
	
	/**
	 * Returns a Server object with the details of the instance serving the given theater id
	 */
	public Server getInstanceServingTheater(int theaterId, InstanceType instanceType){
		List<Server> servers = instanceManager.getServers(instanceType);
		
		return servers.get( theaterId % servers.size() );
	}
	
	
	/**
	 * Returns a random Server object of the instanceType given.
	 */
	public Server getRandomInstance(InstanceType instanceType){
		List<Server> servers = instanceManager.getServers(instanceType);
		Random rd = new Random();
		
		return servers.get( rd.nextInt(servers.size()) );
	}
	
	
	public void updateInstances(InstanceType instanceType, Map<String, String> serverList) {
		instanceManager.updateInstances(instanceType, serverList);
	}
	
}
