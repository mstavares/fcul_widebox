package database;

import common.*;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

class DatabasePoolManager{

    private static final String ROOT_ZNODE = "/widebox";
    private static final String DATABASE_ZNODE = ROOT_ZNODE + "/database";
    private static final String DATABASE_ZNODE_DIR = ROOT_ZNODE + "/database/";
    private ZooKeeperManager zkmanager;
    private DatabasePoolManagerListener listener;

    //private Server backupServer;
    private String backupZnode;
    private String myZnode;
	private DatabaseManager databaseManager;
	private Server myPrimary;
	private String myPrimaryZnode;
	

    DatabasePoolManager(DatabasePoolManagerListener listener, DatabaseManager databaseManager) {
    	this.databaseManager = databaseManager;
    	zkmanager = ZooKeeperManagerImpl.getInstace();
    	
        try {
            this.listener = listener;
            initialize();
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }


    /**
     * Inicializa a árvore de znodes relativa as bases de dados e cria o seu proprio znode
     */
    private void initialize() throws IOException, KeeperException, InterruptedException {
        DatabaseProperties dbp = new DatabaseProperties();
        Server server = fetchMyServerData();
        createRoot();
        
        List<String> databases = fetchDatabaseZnodes();
        printZnodes(databases);
        
        if (databases.size() > 0) {
        	//there are already znodes, trying to find someone to share with
        	int max = 0;
        	String nodeName = "";
        	for (String db: databases) {
        		String[] range = db.split(";");
        		int theaterAmount = Integer.parseInt(range[1]) - Integer.parseInt(range[0]);
        		//TODO hm, maybe problemas por o limite ser inclusive? possivelmente should be exclusive
        		if (theaterAmount >= max) {
        			nodeName = db;
        			max = theaterAmount;
        		}
        	}
        	int start = Integer.getInteger( nodeName.split(";")[1]) - (max / 2) ;
        	int end = Integer.getInteger( nodeName.split(";")[1]) ;
        	listener.onReceiveMyTheaterRange(start, end);
        	
        	myPrimary = Server.buildObject( zkmanager.getData(nodeName, new PrimaryWatcher() ) );
        	myPrimaryZnode = nodeName;
        	//contact primary and ask for stuff:
            try {
                Debugger.log("Contacting primary: " + myPrimary.toString());
                Registry registry = LocateRegistry.getRegistry(myPrimary.getIp(), myPrimary.getPort());
                WideBoxDatabase primary = (WideBoxDatabase) registry.lookup("WideBoxDatabase");
                
                myZnode = start + ";" + end;
                
                //creating my nznode:
                String fullZnodeToCreate = DATABASE_ZNODE_DIR + myZnode;
                if (!zkmanager.exists(fullZnodeToCreate, null)) {
                    zkmanager.createEphemeral(fullZnodeToCreate, server.getBytes());
                    Debugger.log("Criei o meu znode " + fullZnodeToCreate);
                }
                
                Map<Integer, Seat[][]> entries = primary.fetchEntries(start, myZnode);
                databaseManager.setDatabase(entries);
            } catch (RemoteException | NotBoundException e) {
            	Debugger.log("Failed to contact primary");
                e.printStackTrace();
                //System.exit?
            }
        	
        	//set the following node as my secondary:
            String secondary = getServerByStart(databases, end + 1);
        	listener.backupServerIsAvailable(Server.buildObject( zkmanager.getData(secondary, new SecondaryWatcher() ) ));
        	
        }else {
        	//I'm the first node, taking everything for me and wait for a secondary:
        	myZnode = 0 + ";" + ( dbp.getNumberOfTheaters() - 1 ) ;
        	zkmanager.getChildren(DATABASE_ZNODE, new GetSecondaryWatcher() );
        	databaseManager.setDatabase(null);
        	
            //creating my nznode:
            String fullZnodeToCreate = DATABASE_ZNODE_DIR + myZnode;
            if (!zkmanager.exists(fullZnodeToCreate, null)) {
                zkmanager.createEphemeral(fullZnodeToCreate, server.getBytes());
                Debugger.log("Criei o meu znode " + fullZnodeToCreate);
            }
        }
        
    }

    private String getServerByStart(List<String> servers, int start) {
    	String first = null, server = null;
    	
    	for (String s: servers) {
    		if (s.startsWith("0"))
    			first = s;
    		else if (s.startsWith(start + ""))
    			server = s;
    	}
    	
		return server == null ? first : server;
	}
    
    
    private String getServerByEnd(int end) {
		try {
			List<String> servers = zkmanager.getChildren(DATABASE_ZNODE, null);
			
	    	String last = null, server = null;
	    	int max = 0;
	    	for (String s: servers) {
	    		int n = Integer.parseInt(s.split(";")[0]);
	    		if (n >= max) {
	    			max = n;
	    			last = s;
	    		}
	    		
	    		if (s.endsWith(end + ""))
	    			server = s;
	    	}
	    	
			return server == null ? last : server;
		} catch (KeeperException | InterruptedException e) {
			Debugger.log("Error getting children");
			e.printStackTrace();
			return "";
		}
	}
    

    private Server fetchMyServerData() {
        String myIpAddress = Utilities.getOwnIp();
        return new Server(myIpAddress, Utilities.getPort() );
    }


    /**
     * Inicializa a árvore DATABASE_ZNODE -> /widebox/database
     */
    private void createRoot() throws KeeperException, InterruptedException {
        if (!zkmanager.exists(ROOT_ZNODE, null)) {
            zkmanager.createPersistent(ROOT_ZNODE, null);
            zkmanager.createPersistent(DATABASE_ZNODE, null);
            Debugger.log("Znode created " + DATABASE_ZNODE);
        } else if (!zkmanager.exists(DATABASE_ZNODE, null)) {
            zkmanager.createPersistent(DATABASE_ZNODE, null);
            Debugger.log("Znode created " + DATABASE_ZNODE);
        }
    }

    
    /**
     * Devolve os znodes existentes em /widebox/database
     */
    private List<String> fetchDatabaseZnodes() throws KeeperException, InterruptedException {
        return zkmanager.getChildren(DATABASE_ZNODE, null);
    }


    /**
     * >>> Para efeitos de debugging <<<
     * Imprime os znodes existentes na arvore
     *      Marca com PP XX PP o proprio / primario znode.
     *      Marca com BB XX BB o backup / secundario znode.
     */
    private void printZnodes(List<String> databaseZnodes) {
        Debugger.log("++++++++++++++++++++++++++++++++++");
        for(String znode : databaseZnodes) {
            if(znode.equals(myZnode))
                Debugger.log("PP " + znode + " PP");
            else if(znode.equals(backupZnode))
                Debugger.log("BB " + znode + " BB");
            else
                Debugger.log("-- " + znode + " --");
        }

        Debugger.log("++++++++++++++++++++++++++++++++++");
    }

    
    private class GetSecondaryWatcher implements Watcher{
		@Override
		public void process(WatchedEvent event) {
			if (event.getType() == EventType.NodeCreated ) {
				if (event.getPath() != myZnode) {
					try {
						listener.backupServerIsAvailable(Server.buildObject( zkmanager.getData(event.getPath(), new SecondaryWatcher() ) ));
					} catch (RemoteException | KeeperException | InterruptedException e) {
						Debugger.log("Error setting secondary");
						e.printStackTrace();
					}
				}
			}
		}
    }
    
    
    private class PrimaryWatcher implements Watcher{
		@Override
		public void process(WatchedEvent event) {
			Debugger.log("Watch event!");
			if (event.getType() == EventType.NodeDeleted ) {
				if (!isOnline(myPrimary)) {
					try {
						int start = Integer.parseInt(myPrimaryZnode.split(";")[0]);
						int end = Integer.parseInt(myZnode.split(";")[1]);
						String newName = start + ";" + end;
						setNewName(newName);
						listener.onReceiveMyTheaterRange(start, end);
					} catch (KeeperException | InterruptedException e) {
						Debugger.log("Error changing name");
						e.printStackTrace();
					}
					
					listener.updateSecondary();
				}
				
				myPrimaryZnode = getServerByEnd(Integer.parseInt(myZnode.split(";")[1]) + 1);
				myPrimary = new Server(myPrimaryZnode.split(";")[0], Integer.parseInt(myPrimaryZnode.split(";")[1]));
			}
		}
    }

    
    private class SecondaryWatcher implements Watcher{
		@Override
		public void process(WatchedEvent event) {
			Debugger.log("Watch event!");
			if (event.getType() == EventType.NodeDeleted ) {
				//TODO maybe sleep enquanto o secundario recria o nó para evitar race conditions?
				int secondaryStart = Integer.parseInt( myZnode.split(";")[1] ) + 1;
				try {
					String newSecondary = getServerByStart(zkmanager.getChildren(DATABASE_ZNODE, null), secondaryStart);
					listener.backupServerIsAvailable(Server.buildObject( zkmanager.getData(newSecondary, new SecondaryWatcher()) ));
					
					listener.updateSecondary();
				} catch (RemoteException | KeeperException | InterruptedException e) {
					Debugger.log("Error setting new follower");
					e.printStackTrace();
				}
			}
			//TODO é preciso recriar o watch se não for deleted event? shouldn't happen anyway
		}
    }
    
    
	public void setNewName(String newName) throws KeeperException, InterruptedException {
        zkmanager.createEphemeral(newName, fetchMyServerData().getBytes() );
		zkmanager.delete(myZnode);
        Debugger.log("Criei o meu znode " + newName);
	}
	
	
	private boolean isOnline(Server server) {
		try {
			Registry registry = LocateRegistry.getRegistry(server.getIp(), server.getPort());
			WideBoxDatabase primary = (WideBoxDatabase) registry.lookup("WideBoxDatabase");
			if (primary.getTheaters() != null)
				return true;
		} catch (Exception e) {
		}
        return false;
	}
	
}
