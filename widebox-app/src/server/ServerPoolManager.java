package server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import common.Debugger;
import common.InstanceSelector;
import common.InstanceType;
import common.Server;
import common.Utilities;
import database.WideBoxDatabase;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

public class ServerPoolManager{
	private static final String ROOT_ZNODE = "/widebox";
    private static final String SERVER_ZNODE = ROOT_ZNODE + "/appserver";
    private static final String SERVER_ZNODE_DIR = ROOT_ZNODE + "/appserver/";
    private static final String DATABASE_ZNODE = ROOT_ZNODE + "/database";
    private static final String DATABASE_ZNODE_DIR = ROOT_ZNODE + "/database/";
    private InstanceSelector instanceSelector;
    private ZooKeeperManager zkmanager;
    private int myZnode;
    private Server myServer;
    private Map<String,Server> servers;
    
	/** HashMap with all remote database objects **/
	private HashMap<String, WideBoxDatabase> database;
	
    ServerPoolManager(Map<String,Server> servers) {
    	this.servers = servers;
    	instanceSelector = InstanceSelector.getInstance();
    	zkmanager = ZooKeeperManagerImpl.getInstace();
    	try {
            initialize();
            initializeDBSearch();
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
    
    private void initialize() throws IOException, KeeperException, InterruptedException {
        createRoot();
    	myServer = fetchMyServerData();
    	List<String> serversInZK = zkmanager.getChildren(SERVER_ZNODE, new DirectoryWatcher() );
    	
    	myZnode = getNextId(serversInZK);
    	
        String fullZnodeToCreate = SERVER_ZNODE_DIR + myZnode;
        if (!zkmanager.exists(fullZnodeToCreate, null)) {
            zkmanager.createEphemeral(fullZnodeToCreate, myServer.getBytes());
            Debugger.log("Criei o meu znode " + fullZnodeToCreate);
        }
        
    	if (myZnode != 0) {
    		//já há mais servers, fazer watch ao que está antes de mim:
    		zkmanager.getData(SERVER_ZNODE_DIR + (myZnode-1), new PreviousWatcher() );
    	}
    }
    
    
    private int getNextId(List<String> serversInZK) {
    	if (serversInZK.size() == 0)
    		return 0;
    	
    	int max = 0;
    	for (String s: serversInZK)
    		if (Integer.parseInt(s) > max)
    			max = Integer.parseInt(s);
    	
    	return max;
	}
    
    
    private Server fetchMyServerData() {
        String myIpAddress = Utilities.getOwnIp();
        return new Server(myIpAddress, Utilities.getPort() );
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
    
    
    private void removeEverythingFromServerList() {
    	servers.clear();
    	instanceSelector.updateInstances(InstanceType.APP, servers);
    }
    
    
    private void addToServers(String key, Server server) {
    	servers.put(key, server);
    	instanceSelector.updateInstances(InstanceType.APP, servers);
    }
    
    
    private class DirectoryWatcher implements Watcher{
		@Override
		public void process(WatchedEvent event) {
			Debugger.log("Directory Watcher event!");
			try {
				//TODO tornar isto mais eficiente
				removeEverythingFromServerList();
				List<String> serversInZK = zkmanager.getChildren(SERVER_ZNODE, new DirectoryWatcher());
				for (String s: serversInZK)
					addToServers(s, Server.buildObject(zkmanager.getData(SERVER_ZNODE_DIR + s, null)));
			} catch (KeeperException | InterruptedException e) {
				Debugger.log("Error updating list");
				e.printStackTrace();
			}
		}
    }
    
    
    
    private class PreviousWatcher implements Watcher{
		@Override
		public void process(WatchedEvent event) {
			Debugger.log("Previous Watcher event!");
			if (event.getType() == EventType.NodeDeleted ) {
				if (myZnode > 0) {
					try {
						myZnode--;
						zkmanager.createEphemeral(SERVER_ZNODE_DIR + myZnode, myServer.getBytes() );
						zkmanager.delete(SERVER_ZNODE_DIR + (myZnode + 1));
						
						sleep(2000);
						if (myZnode > 0)
							zkmanager.getData(SERVER_ZNODE_DIR + (myZnode + 1), new PreviousWatcher() );
					} catch (KeeperException | InterruptedException e) {
						Debugger.log("Error changing node name");
						e.printStackTrace();
					}
					
				}
			}
		}
    }
    
    
    private void sleep(int ms) {
    	try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    
    
    private void initializeDBSearch() throws IOException, KeeperException, InterruptedException {
    	createRootDB();
    	List<String> dbsInZK = zkmanager.getChildren(DATABASE_ZNODE, new DatabaseWatcher() );
    	updateDatabase(dbsInZK);
    }
    

	/**
     * Inicializa a árvore DATABASE_ZNODE -> /widebox/database
     */
    private void createRootDB() throws KeeperException, InterruptedException {
        if (!zkmanager.exists(ROOT_ZNODE, null)) {
            zkmanager.createPersistent(ROOT_ZNODE, null);
            zkmanager.createPersistent(DATABASE_ZNODE, null);
            Debugger.log("Znode created " + DATABASE_ZNODE);
        } else if (!zkmanager.exists(DATABASE_ZNODE, null)) {
            zkmanager.createPersistent(DATABASE_ZNODE, null);
            Debugger.log("Znode created " + DATABASE_ZNODE);
        }
    }
    
    
    
    private class DatabaseWatcher implements Watcher{
		@Override
		public void process(WatchedEvent event) {
			Debugger.log("Database Watcher event!");
			try {
				List<String> dbsInZK = zkmanager.getChildren(DATABASE_ZNODE, new DatabaseWatcher() );
				updateDatabase(dbsInZK);
			} catch (KeeperException | InterruptedException e) {
				Debugger.log("Error getting databases");
				e.printStackTrace();
			}
	    	
		}
    }
    
    
    private void updateDatabase(List<String> dbsInZK) {
		//TODO a lista e as ligações à db estão a ser criadas a cada atualização
    	//precisa ser otimizada
    	Map<String, Server> dbServers = new HashMap<String, Server>();
    	
    	try {
        	for(String s: dbsInZK)
        		dbServers.put(s, Server.buildObject(zkmanager.getData(DATABASE_ZNODE_DIR + s, null)));
        	
			database = getRemoteDatabaseObjects(dbServers);
		} catch (RemoteException | NotBoundException | KeeperException | InterruptedException e) {
			Debugger.log("Error creating remote connections to databases");
			e.printStackTrace();
		}
	}
    
    
    public WideBoxDatabase getDatabaseServing(int theaterId) {
    	Set<String> keys = database.keySet();
    	for (String s: keys) {
    		String[] range = s.split(";");
    		if (Integer.parseInt(range[0]) <= theaterId && Integer.parseInt(range[1]) >= theaterId)
    			return database.get(s);
    	}
    	return null;
    }
    
    
	private HashMap<String, WideBoxDatabase> getRemoteDatabaseObjects(Map<String, Server> servers) throws RemoteException, NotBoundException {
		HashMap<String, WideBoxDatabase> res = new HashMap<>();
		Registry registry;
		Set<String> keys = servers.keySet();
		//TODO otmizar isto
		for (String s : keys) {
			Server server = servers.get(s);
			registry = LocateRegistry.getRegistry(server.getIp(), server.getPort());
			res.put(s, (WideBoxDatabase) registry.lookup("WideBoxDatabase"));
			Debugger.log("Added Database Server " + server.getIp() + " to Remote Objects Map");
		}
		return res;		
	}
	
	
    
}
