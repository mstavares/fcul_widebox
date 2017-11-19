package common;

import java.util.List;

public class InstanceSelector {
	private static InstanceSelector instance;
	
	private InstanceManager instanceManager;
	
	private InstanceSelector() throws Exception{
		instanceManager = InstanceManager.getInstance();
	}
	
	
	public static InstanceSelector getInstance() throws Exception{
		//TODO possiveis problemas de concurrencia?
		if (instance == null)
			instance = new InstanceSelector();
		return instance;
	}
	
	
	/**
	 * Returns a Server object with the details of the instance serving the given theater id
	 */
	public Server getInstanceServingTheater(int theaterId, InstanceType instanceType){
		List<Server> servers = instanceManager.getServers(instanceType);
		
		//TODO tornar isto sequencial?
		return servers.get( theaterId % servers.size() );
	}
	
}
