package database;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import common.Debugger;
import common.Seat;


public class WideBoxDatabaseImpl extends UnicastRemoteObject implements WideBoxDatabase {
	
	private static final long serialVersionUID = -3423585153833379299L;

	private DatabaseManager databaseManager;


	WideBoxDatabaseImpl() throws IOException, ClassNotFoundException {
		databaseManager = new DatabaseManager();
		registerService();
	}

	private void registerService() {
		try {
			Registry registry = LocateRegistry.createRegistry(1098);
			registry.bind("WideBoxDatabase", this);
			Debugger.log("Database server is ready");
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, Integer> getTheaters() throws RemoteException{
		return databaseManager.getTheaters();
	}
	
	
	@Override
	public Seat[][] getTheatersInfo(int theaterId) throws RemoteException{
		return databaseManager.getTheaterInfo(theaterId);
	}
	
	
	@Override
	public synchronized boolean reserveSeat(int theaterId, int clientId, int row, int column) throws RemoteException{
		return databaseManager.reserveSeat(theaterId, clientId, row, column);
	}
	
	
	@Override
	public synchronized boolean acceptReservedSeat(int theaterId, int clientId, int row, int column) throws RemoteException{
		return databaseManager.acceptReservedSeat(theaterId, clientId, row, column);
	}
	
	
	@Override
	public synchronized boolean cancelReservation(int theaterId, int clientId, int row, int column) throws RemoteException{
		return databaseManager.cancelReservation(theaterId, clientId, row, column);
	}

}
