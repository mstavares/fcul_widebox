package server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import common.Seat;
import common.Theater;
import database.WideBoxDatabase;


public class WideBoxServerImpl extends UnicastRemoteObject implements WideBoxServer {
	
	private static final long serialVersionUID = 6332295204270798892L;
	private boolean online;
	private WideBoxDatabase wideBoxDatabase;
	private Random randomGenerator;
	
	private class Place {
		private int row;
		private int column;
		
		public Place(int row, int column) {
			this.row = row;
			this.column = column;
		}
		
		public int getRow() {
			return this.row;
		}
		
		public int getColumn() {
			return this.column;
		}
	}
	
	
	public WideBoxServerImpl(String host, int port) throws RemoteException {
		super();
		
		try {
			Registry registry = LocateRegistry.getRegistry(host, port);
			wideBoxDatabase = (WideBoxDatabase) registry.lookup("WideBoxDatabase");
		} catch (RemoteException | NotBoundException e) {
			throw new RemoteException("Error connecting to the Database Server.");
		}
		
		
		
	}
	
	
	@Override
	public Map<String, Integer> getTheaters() throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		return wideBoxDatabase.getTheaters();
	}
	
	
	@Override
	public Seat[][] getTheaterInfo(int theaterId, int clientId) throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		
		Seat[][] seats = wideBoxDatabase.getTheatersInfo(theaterId);
		Place seat = pickFreeSeat(seats);
		if(!reserveSeat(theaterId, clientId, seat.getRow(), seat.getColumn())) {
			throw new RemoteException("Error automatically reserving seat");
		}
		seats[seat.getRow()][seat.getColumn()].setSelf();
		
		//Criar o timeout da reserva
		return seats;
	}
	
	
	@Override
	public boolean reserveSeat(int theaterId, int clientId, int row, int column) throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		//TODO iniciar timer
		return wideBoxDatabase.reserveSeat(theaterId, clientId, row, column);
	}
	
	@Override
	public boolean acceptReservedSeat(int theaterId, int clientId, int row, int column) throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		//TODO parar timer
		return wideBoxDatabase.acceptReservedSeat(theaterId, clientId, row, column);
	}
	
	
	@Override
	public boolean cancelReservation(int theaterId, int clientId, int row, int column) throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		//TODO parar timer
		return wideBoxDatabase.cancelReservation(theaterId, clientId, row, column);
	}
	
	
	@Override
	public boolean stopServer() throws RemoteException{
		if (!online)
			return false;
		
		online = false;
		return true;
	}
	
	
	@Override
	public boolean startServer() throws RemoteException{
		if (online)
			return false;
		
		online = true;
		return true;
	}
	
	private boolean isOnline() {
		return this.online;
	}
	
	//TODO arranjar uma forma mais eficiente de fazer isto
	private Place pickFreeSeat(Seat[][] seats) {
		List<Place> freeSeats = new ArrayList<>();
		for(int i = 0; i < seats.length; i++) {
			for(int k = 0; k < seats[i].length; k++) {
				if(seats[i][k].isFree()) {
					freeSeats.add(new Place(i,k));
				}
			}
		}
		return freeSeats.get(randomGenerator.nextInt(freeSeats.size()));
	}
	
}
