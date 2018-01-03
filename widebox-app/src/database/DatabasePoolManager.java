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

    private Server backupServer;
    private String backupZnode;
    private String myZnode;


    DatabasePoolManager(DatabasePoolManagerListener listener) {
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
        InstanceManager instanceManager = InstanceManager.getInstance();
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
        	
        	//is it fine to watch my primary here?
        	Server primary = Server.buildObject( zkmanager.getData(nodeName, new PrimaryWatcher() ) );
        	
        	//contact primary and ask for stuff
            try {
                Debugger.log("Contacting primary: " + primary.toString());
                Registry registry = LocateRegistry.getRegistry(primary.getIp(), primary.getPort());
                WideBoxDatabase myPrimary = (WideBoxDatabase) registry.lookup("WideBoxDatabase");
                
                myZnode = start + ";" + end;
                Map<Integer, Seat[][]> entries = myPrimary.fetchEntries(start, myZnode);
                //TODO update this locally
            } catch (RemoteException | NotBoundException e) {
            	Debugger.log("Failed to contact primary");
                e.printStackTrace();
                //System.exit?
            }
        	
        	myZnode = start + ";" + end;
        	
        	//set the following node as my secondary
        	//TODO find a way to tell him the range is updated
            String secondary = getServerByRange(databases, end + 1);
        	listener.backupServerIsAvailable(Server.buildObject( zkmanager.getData(secondary, null) ));
        	
        }else {
        	//I'm the first node, taking everything for me and wait for a secondary
        	myZnode = 0 + ";" + ( dbp.getNumberOfTheaters() - 1 ) ;
        	zkmanager.getChildren(DATABASE_ZNODE, new GetSecondaryWatcher() );
        }
        
        //creating my nznode
        String fullZnodeToCreate = DATABASE_ZNODE_DIR + myZnode;
        if (!zkmanager.exists(fullZnodeToCreate, null)) {
            zkmanager.createEphemeral(fullZnodeToCreate, server.getBytes());
            Debugger.log("Criei o meu znode " + fullZnodeToCreate);
        }
        
    }

    private String getServerByRange(List<String> servers, int start) {
    	String first = null, server = null;
    	
    	for (String s: servers) {
    		if (s.startsWith("0"))
    			first = s;
    		else if (s.startsWith(start + ""))
    			server = s;
    	}
    	
		return server == null ? first : server;
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
     * Vai calcular o znode que vai servir de backup / secundario
     */
    /*private void computeZnodeBackup(int numberOfTheaters, int numberOfDatabases, int i) throws KeeperException, InterruptedException {
        int lastBackupTheater;
        if(i == numberOfDatabases) {
            lastBackupTheater = numberOfTheaters - 1;
            backupZnode = String.valueOf(lastBackupTheater);
        } else {
            lastBackupTheater = computeMyBackupLastTheater(numberOfTheaters, i);
            backupZnode = String.valueOf(lastBackupTheater);
        }
        listener.onReceiveMyTheaterRange(lastBackupTheater);
        Debugger.log("My backup server is " + backupZnode);
    }


    private void checkZnodeBackupIpAddress() throws KeeperException, InterruptedException {
        try {
            if (zkmanager.exists(DATABASE_ZNODE_DIR + backupZnode, this)) {
                backupServer = Server.buildObject(zkmanager.getData(DATABASE_ZNODE_DIR + backupZnode, null));
                Debugger.log("Backup server data : " + backupServer.toString());
                listener.backupServerIsAvailable(backupServer);
            } else {
                backupServer = null;
                Debugger.log("Backup server is not available");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/

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
						listener.backupServerIsAvailable(Server.buildObject( zkmanager.getData(event.getPath(), null) ));
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
				//ver se o primary ainda está vivo
				//TODO my primary died
				//redifinir o meu range
				//enviar as coisas do meu antigo primario ao meu secundario
				
			}
		}
    }


	public void setNewName(String newName) throws KeeperException, InterruptedException {
        zkmanager.createEphemeral(newName, fetchMyServerData().getBytes() );
		zkmanager.delete(myZnode);
        Debugger.log("Criei o meu znode " + newName);
	}
}
