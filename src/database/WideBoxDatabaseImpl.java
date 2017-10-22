package database;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import common.Theater;


public class WideBoxDatabaseImpl extends UnicastRemoteObject implements WideBoxDatabase{
	
	private static final long serialVersionUID = -3423585153833379299L;
	
	//opcodes to use when writing to the log
	private final static int ACCEPT_ACTION = 10;
	private final static int CANCEL_ACTION = 20;
	private final static int RESERVE_ACTION = 50;
	
	private Map<Integer,Boolean[][]> database;
	
	
	public WideBoxDatabaseImpl() throws RemoteException{
		
		//TODO if there isn't a Database.dat file
		// create one based on the details on the Server.properties file with all seats at 0.
		
		//TODO If there is a Database.dat file and a Database.log file
		// then update the Database with the log file and clear the log.

		//TODO Restore Database.dat to the database object.
	}
	
	
	@Override
	public Map<Integer,String> getTeaters() throws RemoteException{
		//TODO
		return null;
	}
	
	
	@Override
	public Theater getTheaterInfo(int theaterId) throws RemoteException{
		//TODO
		return null;
	}
	
	
	@Override
	public synchronized boolean reserveSeat(int clientId, int row, int column) throws RemoteException{
		//TODO
		return false;
	}
	
	
	@Override
	public synchronized boolean acceptReservedSeat(int clientId) throws RemoteException{
		//TODO
		return false;
	}
	
	
	@Override
	public synchronized boolean cancelReservation(int clientId) throws RemoteException{
		//TODO
		return false;
	}
	
	
}
