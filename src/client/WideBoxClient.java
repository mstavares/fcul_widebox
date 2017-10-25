package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import common.Seat;
import server.WideBoxServer;

public class WideBoxClient {
	
	
	private int id;
	private WideBoxServer wideBoxServer;

	public WideBoxClient(int clientId, String serverHost, int serverPort) throws RemoteException{
		id = clientId;
		
		try {
			Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);
			wideBoxServer = (WideBoxServer) registry.lookup("WideBoxServer");
		} catch (RemoteException | NotBoundException e) {
			throw new RemoteException("Error connecting to the server.");
		}

	}
	
	public Map<String, Integer> getTheaters() throws RemoteException{
		return wideBoxServer.getTheaters();
	}
	
	
	public Seat[][] getTheaterInfo(int theaterId) throws RemoteException{
		return wideBoxServer.getTheaterInfo(theaterId, id);
	}
	
	
	public boolean reserveSeat(int theaterId, int row, int column) throws RemoteException{
		return wideBoxServer.reserveSeat(theaterId, id, row, column);
	}
	
	
	public boolean acceptReservedSeat() throws RemoteException{
		return wideBoxServer.acceptReservedSeat(id);
	}
	
	
	public boolean cancelReservation() throws RemoteException{
		return wideBoxServer.cancelReservation(id);
	}
	
}
