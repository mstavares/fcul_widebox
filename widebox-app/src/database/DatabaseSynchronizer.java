package database;

import common.Debugger;
import common.Server;

import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class DatabaseSynchronizer implements DatabasePoolManagerListener {

    private DatabasePoolManager databasePoolManager;
    private WideBoxDatabase wideBoxDatabase;
    private int lastTheaterNumber;


    DatabaseSynchronizer() {
        databasePoolManager = new DatabasePoolManager(this);
    }

    @Override
    public void onReceiveMyTheaterRange(int lastTheaterNumber) {
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
            if (theaterId <= lastTheaterNumber) {
                try {
                    boolean isReplicated = wideBoxDatabase.acceptReservedSeat(theaterId, clientId, row, column);
                    Debugger.log("Resultado " + isReplicated);
                    if(isReplicated)
                        Debugger.log("Entry replicated successfully");
                    return isReplicated;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            Debugger.log("Out of my range, so I will not replicate this entry");
        } else {
            Debugger.log("Backup server is not available");
        }
        return true;
    }

}
