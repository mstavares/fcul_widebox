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
    	zkmanager.newConnection();
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
        	int start = Integer.parseInt( nodeName.split(";")[1]) - (max / 2) ;
        	int end = Integer.parseInt( nodeName.split(";")[1]) ;
        	listener.onReceiveMyTheaterRange(start, end);
        	
        	myPrimary = Server.buildObject( zkmanager.getData(DATABASE_ZNODE_DIR + nodeName, new PrimaryWatcher() ) );
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
            String secondary = getServerByStart(end + 1);
        	listener.backupServerIsAvailable(Server.buildObject( zkmanager.getData(DATABASE_ZNODE_DIR + secondary, new SecondaryWatcher() ) ));
        	
        }else {
        	//I'm the first node, taking everything for me:
        	myZnode = 0 + ";" + ( dbp.getNumberOfTheaters() - 1 ) ;
        	
            //creating my znode:
            String fullZnodeToCreate = DATABASE_ZNODE_DIR + myZnode;
            if (!zkmanager.exists(fullZnodeToCreate, null)) {
                zkmanager.createEphemeral(fullZnodeToCreate, server.getBytes());
                Debugger.log("Criei o meu znode " + fullZnodeToCreate);
            }
            
            //wait for a second server:
        	zkmanager.getChildren(DATABASE_ZNODE, new GetSecondaryWatcher() );
        	databaseManager.setDatabase(null);
        }
        
    }

    private String getServerByStart(int start) {
		try {
			List<String> servers = zkmanager.getChildren(DATABASE_ZNODE, null);
			
	    	String first = null, server = null;
	    	
	    	for (String s: servers) {
	    		if (s.startsWith("0"))
	    			first = s;
	    		else if (s.startsWith(start + ""))
	    			server = s;
	    	}
	    	
			return server == null ? first : server;
		} catch (KeeperException | InterruptedException e) {
			Debugger.log("Error getting children");
			e.printStackTrace();
			return "";
		}
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
			Debugger.log("Get Secondary Watcher event!: " + event.getType());
			if (event.getType() == EventType.NodeChildrenChanged ) {
				if (event.getPath() != myZnode) {
					try {
						Thread.sleep(2000);
						Debugger.log("INTO Get Secondary Watcher event!");
						//set the new node as secondary:
						listener.backupServerIsAvailable(Server.buildObject( zkmanager.getData(DATABASE_ZNODE_DIR + getServerByStart(Integer.parseInt(myZnode.split(";")[1]) +1), new SecondaryWatcher() ) ));
						
						//set the new node as primary: //TODO dois watches?
						myPrimaryZnode = getServerByStart(Integer.parseInt(myZnode.split(";")[1]) +1);
						myPrimary = Server.buildObject(	zkmanager.getData(DATABASE_ZNODE_DIR + myPrimaryZnode, new PrimaryWatcher() ) );
					} catch (RemoteException | KeeperException | InterruptedException e) {
						Debugger.log("Error setting secondary");
						e.printStackTrace();
					}
				}else{
					Debugger.log("Recreating get secondary watcher. 1");
					try {
						zkmanager.getChildren(DATABASE_ZNODE, new GetSecondaryWatcher() );
					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}else {
				Debugger.log("Recreating get secondary watcher. 2");
				try {
					zkmanager.getChildren(DATABASE_ZNODE, new GetSecondaryWatcher() );
				} catch (KeeperException | InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
    }
    
    
    private class PrimaryWatcher implements Watcher{
		@Override
		public void process(WatchedEvent event) {
			Debugger.log("Primary Watcher event!");
			if (event.getType() == EventType.NodeDeleted ) {
				if (!isOnline(myPrimary)) {
					Debugger.log("Primary is not online, taking over.");
					try {
						int start = Integer.parseInt(myPrimaryZnode.split(";")[0]);
						int end = Integer.parseInt(myZnode.split(";")[1]);
						
						if (start > end) {
							start = Integer.parseInt(myZnode.split(";")[0]);
							end = Integer.parseInt(myPrimaryZnode.split(";")[1]);
						}
						
						String newName = start + ";" + end;
						setNewName(newName);
						listener.onReceiveMyTheaterRange(start, end);
					} catch (KeeperException | InterruptedException e) {
						Debugger.log("Error changing name");
						e.printStackTrace();
					}
					
					listener.updateSecondary();
				}
				
				myPrimaryZnode = getServerByEnd(Integer.parseInt(myZnode.split(";")[0]) - 1);
				if (!myPrimaryZnode.equals(myZnode)) {
					Debugger.log("My new primary: " + myPrimaryZnode);
				}else {
					Debugger.log("There are no more servers. Setting up Watcher for new nodes.");
					try {
						zkmanager.getChildren(DATABASE_ZNODE, new GetSecondaryWatcher() );
					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			try {
				myPrimary = Server.buildObject(	zkmanager.getData(DATABASE_ZNODE_DIR + myPrimaryZnode, new PrimaryWatcher()) );
			} catch (KeeperException | InterruptedException e) {
				e.printStackTrace();
			}
		}
    }

    
    private class SecondaryWatcher implements Watcher{
		@Override
		public void process(WatchedEvent event) {
			Debugger.log("Secondary Watcher event!");
			if (event.getType() == EventType.NodeDeleted ) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				int secondaryStart = Integer.parseInt( myZnode.split(";")[1] ) + 1;
				try {
					String newSecondary = getServerByStart(secondaryStart);
					
					if (!newSecondary.equals(myZnode)) {
						listener.backupServerIsAvailable(Server.buildObject( zkmanager.getData(DATABASE_ZNODE_DIR + newSecondary, new SecondaryWatcher()) ));
						
						listener.updateSecondary();
					}
				} catch (RemoteException | KeeperException | InterruptedException e) {
					Debugger.log("Error setting new backup server");
					//e.printStackTrace();
				}
			}else {
				Debugger.log("WARNING: THIS SHOULDN'T BE HAPPENING");
				//TODO é preciso recriar o watch se não for deleted event? shouldn't happen anyway
			}
		}
    }
    
    
	public void setNewName(String newName) throws KeeperException, InterruptedException {
		zkmanager.createEphemeral(DATABASE_ZNODE_DIR + newName, fetchMyServerData().getBytes() );
		zkmanager.delete(DATABASE_ZNODE_DIR + myZnode);
		myZnode = newName;
        Debugger.log("Criei o meu znode " + newName);
	}
	
	
	private boolean isOnline(Server server) {
		try {
			Registry registry = LocateRegistry.getRegistry(server.getIp(), server.getPort());
			WideBoxDatabase primary = (WideBoxDatabase) registry.lookup("WideBoxDatabase");
			if (primary.getTheaters() != null)
				return true;
		} catch (Exception e) {
			//e.printStackTrace();
		}
        return false;
	}


	public void terminate() {
		zkmanager.closeConnection();		
	}
	
}
