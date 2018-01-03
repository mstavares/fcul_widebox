package database;

import common.Debugger;
import common.Server;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.zookeeper.KeeperException;

class DatabaseSynchronizer implements DatabasePoolManagerListener {

	private DatabasePoolManager databasePoolManager;
    private WideBoxDatabase wideBoxDatabase;
    private int firstTheaterNumber;
    private int lastTheaterNumber;


    DatabaseSynchronizer() {
    	wideBoxDatabase = null;
    	databasePoolManager = new DatabasePoolManager(this);
    }

    @Override
    public void onReceiveMyTheaterRange(int firstTheaterNumber, int lastTheaterNumber) {
    	this.firstTheaterNumber = firstTheaterNumber;
    	this.lastTheaterNumber = lastTheaterNumber;
    }

    @Override
    public void backupServerIsAvailable(Server server) {
        try {
            Debugger.log("Binding backup server " + server.toString());
            Registry registry = LocateRegistry.getRegistry(server.getIp(), server.getPort());
            wideBoxDatabase = (WideBoxDatabase) registry.lookup("WideBoxDatabase");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void backupServerIsUnavailable() {
        wideBoxDatabase = null;
    }

    boolean sendToBackupServer(int theaterId, int clientId, int row, int column) {
        if(wideBoxDatabase != null) {
            if (theaterId >= firstTheaterNumber && theaterId <= lastTheaterNumber) {
                try {
                    boolean isReplicated = wideBoxDatabase.acceptReservedSeat(theaterId, clientId, row, column);
                    if(isReplicated)
                        Debugger.log("Entry replicated successfully.");
                    else
                    	Debugger.log("Failed to replicate entry.");
                    return isReplicated;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            Debugger.log("Out of my range, so I will not replicate this entry");
            //TODO throw not owner exception
        } else {
            Debugger.log("Backup server is not available");
        }
        return true;
    }
    
    
	public void updateRange(int newEnd) {
		lastTheaterNumber = newEnd;
		try {
			databasePoolManager.setNewName(firstTheaterNumber + ";" + lastTheaterNumber);
		} catch (KeeperException | InterruptedException e) {
			Debugger.log("Error updating znode.");
			e.printStackTrace();
		}
	}
	
	
	public void setNewSecondary(String newSecondary) {
		ZooKeeperManager zkmanager = ZooKeeperManagerImpl.getInstace();
		
		try {
			Server secondary = Server.buildObject( zkmanager.getData(newSecondary, null) );
			backupServerIsAvailable(secondary);
		} catch (KeeperException | InterruptedException e) {
			Debugger.log("Error setting new secundary.");
			e.printStackTrace();
		}
		
	}
}
