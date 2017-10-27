package server;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import common.Debugger;
import common.Seat;
import common.TimeoutManager;
import database.WideBoxDatabase;


public class WideBoxServerImpl extends UnicastRemoteObject implements WideBoxServer, SeatTimeoutListener {

	private static final long serialVersionUID = 6332295204270798892L;
	private boolean online;
	private WideBoxDatabase wideBoxDatabase;

	/** Map to keep track of Seat Reservation Timeouts **/
	private Map<Integer, TimeoutManager> timeoutMap;
	/** Map to keep track of Open Seat Reservations **/
	private Map<Integer, Reservation> reservationMap;
	private Random randomGenerator;

	/** Server server properties object */
	private ServerProperties properties;

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

	private class Reservation {
		private Place place;
		private int theaterId;

		public Reservation(int theaterId, Place place) {
			this.theaterId = theaterId;
			this.place = place;
		}

		public int getTheaterId() {
			return this.theaterId;
		}

		public Place getPlace() {
			return this.place;
		}
	}


	/**
	 * Só consegui por a funcionar tendo o vm argument 
	 * -Djava.rmi.server.hostname=ip-dos-servidores
	 * e iniciando o rmiregistry antes dos servidores.
	 * Alterei tambem a porta 1099 do database server
	 */
	
	public WideBoxServerImpl(String host, int port) throws IOException, RemoteException {
		super();

		try {
			Registry registry = LocateRegistry.getRegistry(host, port);
			wideBoxDatabase = (WideBoxDatabase) registry.lookup("WideBoxDatabase");
			registerService();
			online = true;
			randomGenerator = new Random();
			Debugger.log("Application server is ready");
		} catch (RemoteException | NotBoundException e) {
			throw new RemoteException("Error connecting to the Database Server.");
		}
		properties = new ServerProperties();
		timeoutMap = new HashMap<>();
		reservationMap = new HashMap<>();

	}
	
	private void registerService() throws RemoteException {
		try {
			Registry registry = LocateRegistry.createRegistry(1090);
			registry.bind("WideBoxServer", this);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
			throw new RemoteException("Error creating registry");
		}
	}


	@Override
	public Map<String, Integer> getTheaters() throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		Debugger.log("Got Request for Theaters");
		return wideBoxDatabase.getTheaters();
	}


	@Override
	public Seat[][] getTheaterInfo(int theaterId, int clientId) throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		Debugger.log("Got info request for theather " + theaterId + " from clientID " + clientId);
		Seat[][] seats = wideBoxDatabase.getTheatersInfo(theaterId);
		if(!clientHasReservation(clientId)) {
			Place seat = pickFreeSeat(seats);
			if(!reserveSeat(theaterId, clientId, seat.getRow(), seat.getColumn())) {
				Debugger.log("Seat was not reserved");
				throw new RemoteException("Error automatically reserving seat");
			}
			seats[seat.getRow()][seat.getColumn()].setSelf();
		} else {
			Reservation reservation = reservationMap.get(clientId);
			seats[reservation.getPlace().getRow()][reservation.getPlace().getColumn()].setSelf();
		}
		return seats;
	}

	
	// TODO cancelar reserva quando cliente pede nova reserva
	
	@Override
	public boolean reserveSeat(int theaterId, int clientId, int row, int column) throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		if (clientHasReservation(clientId)) {
			cancelReservation(clientId);
		}
		if (wideBoxDatabase.reserveSeat(theaterId, clientId, row, column)){
			Debugger.log("Reserving seat for clientID " + clientId);
			reservationMap.put(clientId, new Reservation(theaterId, new Place(row, column)));
			// Este construtor tambem está horrivel, mas por agora serve
			TimeoutManager timeout = new TimeoutManager(this, properties.getTimeoutValue(), clientId);
			timeout.runOnlyOnce();
			timeoutMap.put(clientId, timeout);
			return true;
		}
		return false;
	}

	@Override
	public boolean acceptReservedSeat(int clientId) throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		Reservation reservation = reservationMap.get(clientId);
		if(reservation == null) {
			throw new RemoteException("No Reservation for this client present");
		}
		if (wideBoxDatabase.acceptReservedSeat(reservation.getTheaterId(), clientId, reservation.getPlace().getRow(), reservation.getPlace().getColumn())) {
			TimeoutManager timeout = timeoutMap.get(clientId);
			timeout.stop();
			timeoutMap.remove(clientId);
			reservationMap.remove(clientId);
			Debugger.log("Confirmed reservation for clientId " + clientId);
			return true;
		}
		return false;
	}


	@Override
	public boolean cancelReservation(int clientId) throws RemoteException{
		if (!isOnline())
			throw new RemoteException("Server is Offline");
		Reservation reservation = reservationMap.get(clientId);
		if(reservation == null) {
			throw new RemoteException("No Reservation for this client present");
		}
		if (wideBoxDatabase.cancelReservation(reservation.getTheaterId(), clientId, reservation.getPlace().getRow(), reservation.getPlace().getColumn())) {
			TimeoutManager timeout = timeoutMap.get(clientId);
			timeout.stop();
			timeoutMap.remove(clientId);
			reservationMap.remove(clientId);
			Debugger.log("Canceled reservation for clientId " + clientId);
			return true;
		}
		return false;
	}


	@Override
	public boolean stopServer() throws RemoteException{
		Debugger.log("Stopping server");
		if (!online)
			return false;
		
		online = false;
		return true;
	}


	@Override
	public boolean startServer() throws RemoteException{
		Debugger.log("Starting server");
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
	
	private boolean clientHasReservation(int clientId) {
		return reservationMap.containsKey(clientId);
	}


	@Override
	public void onSeatTimeout(int clientId) {
		try {
			Debugger.log("Reserved Seat timeout for clientId " + clientId);
			cancelReservation(clientId);
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Error handling seat reservation timeout");
		}
	}

}