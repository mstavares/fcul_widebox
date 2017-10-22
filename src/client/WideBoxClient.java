package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import common.Theater;
import server.WideBoxServer;

public class WideBoxClient {
	
	
	private int id;
	private WideBoxServer wideBoxServer;

	public WideBoxClient(int clientId, String serverHost, int serverPort) throws RemoteException{
		id = clientId;
		
		try {
			Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);
			wideBoxServer = (WideBoxServer) registry.lookup("WideBoxServer");
			System.out.println("book: " + wideBoxServer.acceptReservedSeat(clientId));
		} catch (RemoteException | NotBoundException e) {
			throw new RemoteException("Error connecting to the server.");
		}

	}
	
	public Map<Integer,String> getTheaters() throws RemoteException{
		return wideBoxServer.getTheaters();
	}
	
	
	public Theater getTheaterInfo(int theaterId) throws RemoteException{
		return wideBoxServer.getTheaterInfo(theaterId);
	}
	
	
	public boolean reserveSeat(int row, int column) throws RemoteException{
		return wideBoxServer.reserveSeat(id, row, column);
	}
	
	
	public boolean acceptReservedSeat() throws RemoteException{
		return wideBoxServer.acceptReservedSeat(id);
	}
	
	
	public boolean cancelReservation() throws RemoteException{
		return wideBoxServer.cancelReservation(id);
	}
	
}
