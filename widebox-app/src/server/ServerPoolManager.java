package server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import common.Debugger;
import common.InstanceManager;
import common.InstanceSelector;
import common.InstanceType;
import common.Server;
import common.Utilities;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

public class ServerPoolManager implements Watcher {
	private static final String ROOT_ZNODE = "/widebox";
    private static final String SERVER_ZNODE = ROOT_ZNODE + "/appserver";
    private static final String SERVER_ZNODE_DIR = ROOT_ZNODE + "/appserver/";
    private InstanceSelector instanceSelector = InstanceSelector.getInstance();
    private ZooKeeperManager zkmanager = ZooKeeperManagerImpl.getInstace();
    private String myZnode;
    private int watching;
    private Map<String,Server> servers;
    
    ServerPoolManager(Map<String,Server> servers) {
    	this.servers = servers;
    	try {
            initialize();
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
    
    private void initialize() throws IOException, KeeperException, InterruptedException {
        createRoot();
        InstanceManager instanceManager = InstanceManager.getInstance();
    	Server server = fetchMyServerData(instanceManager);
    	int numberOfServers = instanceManager.getServers(InstanceType.APP).size();
    	for(int i = 0; i < numberOfServers; i++) {
        	myZnode = String.valueOf(i);
            String znodeToCreate = SERVER_ZNODE_DIR + myZnode;
            if (!zkmanager.exists(znodeToCreate, null)) {
                zkmanager.createEphemeral(znodeToCreate, server.getBytes());
                Debugger.log("Criei o meu znode " + znodeToCreate);
                if(i != 0)
                	watching = i - 1;
                servers.put(myZnode, server);
                instanceSelector.updateInstances(InstanceType.APP, servers);
                createWatch();
                break;
            } else {
            	Server value = Server.buildObject(zkmanager.getData(SERVER_ZNODE_DIR + myZnode, null));
            	servers.put(myZnode, value);
            	instanceSelector.updateInstances(InstanceType.APP, servers);
            }
    	}
    }
    
    private void createNode() throws KeeperException, InterruptedException {
    	InstanceManager instanceManager = InstanceManager.getInstance();
    	Server server = fetchMyServerData(instanceManager);
    	int numberOfServers = instanceManager.getServers(InstanceType.APP).size();
    	for(int i = 0; i < numberOfServers; i++) {
        	myZnode = String.valueOf(i);
            String znodeToCreate = SERVER_ZNODE_DIR + myZnode;
            if (!zkmanager.exists(znodeToCreate, null)) {
                zkmanager.createEphemeral(znodeToCreate, server.getBytes());
                Debugger.log("Criei o meu znode " + znodeToCreate);
                if(i != 1)
                	watching = i - 1;
                addToServers(myZnode,server);
                break;
            }
        }
    }
    
    private Server fetchMyServerData(InstanceManager instanceManager) {
        String myIpAddress = Utilities.getOwnIp();
        List<Server> applicationServers = instanceManager.getServers(InstanceType.APP);
        for(Server server : applicationServers) {
            if(server.getIp().equals(myIpAddress)) {
                Debugger.log(server.toString());
                return server;
            }
        }
        /* TODO lancar excecao em vez de retornar null? */
        return null;
    }
    
    private void createRoot() throws KeeperException, InterruptedException {
        if (!zkmanager.exists(ROOT_ZNODE, null)) {
            zkmanager.createPersistent(ROOT_ZNODE, null);
            zkmanager.createPersistent(SERVER_ZNODE, null);
            Debugger.log("Criei o znode " + SERVER_ZNODE);
        } else if (!zkmanager.exists(SERVER_ZNODE, null)) {
            zkmanager.createPersistent(SERVER_ZNODE, null);
            Debugger.log("Criei o znode " + SERVER_ZNODE);
        }
    }
    
    private void createWatch() throws KeeperException, InterruptedException {
    	zkmanager.setData(SERVER_ZNODE, null);
        zkmanager.exists(SERVER_ZNODE, this);
    }
    
    private void removeFromServers(String key) {
    	servers.remove(key);
    	instanceSelector.updateInstances(InstanceType.APP, servers);
    }
    
    private void addToServers(String key, byte[] bytes) {
    	servers.put(key, Server.buildObject(bytes));
    	instanceSelector.updateInstances(InstanceType.APP, servers);
    }
    
    private void addToServers(String key, Server server) {
    	servers.put(key, server);
    	instanceSelector.updateInstances(InstanceType.APP, servers);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        Debugger.log("Watch event!");
        // ALL this is wrong since the event will not have these types
        /**
        try {
        	if(watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted) {
        		if(!zkmanager.exists(SERVER_ZNODE_DIR + String.valueOf(watching), null)) {
        			removeFromServers(String.valueOf(watching));
        			zkmanager.delete(SERVER_ZNODE_DIR + myZnode);
        			removeFromServers(myZnode);
        			createNode();
        		} else {
        			String[] temp = watchedEvent.getPath().split("/");
        			removeFromServers(temp[3]);
        		}
        	} else if(watchedEvent.getType() == Watcher.Event.EventType.NodeCreated) {
        		String[] temp = watchedEvent.getPath().split("/");
    			removeFromServers(temp[3]);
        		addToServers(temp[3], zkmanager.getData(watchedEvent.getPath(), null));
        	}
        	createWatch();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        
        **/
    }
}
