package database;

import common.Server;

import java.rmi.RemoteException;

public interface DatabasePoolManagerListener {

	void onReceiveMyTheaterRange(int firstTheaterNumber, int lastTheaterNumber);
    void backupServerIsAvailable(Server server) throws RemoteException;
    void backupServerIsUnavailable();

}
