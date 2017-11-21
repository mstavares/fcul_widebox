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
import common.InstanceManager;
import common.InstanceSelector;
import common.InstanceType;
import common.Seat;
import common.Server;
import common.TimeoutManager;
import database.WideBoxDatabase;


public class WideBoxServerImpl extends UnicastRemoteObject implements WideBoxServer, SeatTimeoutListener {

	private static final long serialVersionUID = 6332295204270798892L;
	private HashMap<Server, WideBoxDatabase> database;
	private InstanceSelector instanceSelector;

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
	 * @throws NotBoundException 
	 */
	
	public WideBoxServerImpl() throws IOException, RemoteException, NotBoundException {
		super();
		InstanceManager instanceManager = InstanceManager.getInstance();
		instanceSelector = InstanceSelector.getInstance();
		database = getRemoteDatabaseObjects(instanceManager.getServers(InstanceType.DATABASE));
		registerService();
		randomGenerator = new Random();
		Debugger.log("Application server is ready");
		properties = new ServerProperties();
		timeoutMap = new HashMap<>();
		reservationMap = new HashMap<>();

	}
	
	private HashMap<Server, WideBoxDatabase> getRemoteDatabaseObjects(List<Server> servers) throws RemoteException, NotBoundException {
		HashMap<Server, WideBoxDatabase> res = new HashMap<>();
		Registry registry;
		for (Server s : servers) {
			registry = LocateRegistry.getRegistry(s.getIp(), s.getPort());
			res.put(s, (WideBoxDatabase) registry.lookup("WideBoxDatabase"));
		}
		return res;		
	}
	
	private void registerService() throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry(1090);
			registry.bind("WideBoxServer", this);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
			throw new RemoteException("Error creating registry");
		}
	}


	@Override
	public Map<String, Integer> getTheaters() throws RemoteException{
		Debugger.log("Got Request for Theaters");
		return database.get(instanceSelector.getInstanceServingTheater(0, InstanceType.DATABASE)).getTheaters();
	}


	@Override
	public Seat[][] getTheaterInfo(int theaterId, int clientId) throws RemoteException{
		Debugger.log("Got info request for theather " + theaterId + " from clientID " + clientId);
		Seat[][] seats = database.get(instanceSelector.getInstanceServingTheater(theaterId, InstanceType.DATABASE)).getTheatersInfo(theaterId);
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

	
	// TODO Deixa de se ligar à DB
	
	@Override
	public synchronized boolean reserveSeat(int theaterId, int clientId, int row, int column) throws RemoteException{
		if (clientHasReservation(clientId)) {
			cancelReservation(clientId);
		}
	
		Debugger.log("Reserving seat for clientID " + clientId);
		reservationMap.put(clientId, new Reservation(theaterId, new Place(row, column)));
		// Este construtor tambem está horrivel, mas por agora serve
		TimeoutManager timeout = new TimeoutManager(this, properties.getTimeoutValue(), clientId);
		timeout.runOnlyOnce();
		timeoutMap.put(clientId, timeout);
		return true;
	}

	// TODO necessário alterar a DB para não verificar se o lugar está reservado
	@Override
	public synchronized boolean acceptReservedSeat(int clientId) throws RemoteException{
		Reservation reservation = reservationMap.get(clientId);
		if(reservation == null) {
			return false;
		}
		// I'm sorry. I'm so sorry.
		if (database.get(instanceSelector.getInstanceServingTheater(reservation.getTheaterId(), InstanceType.DATABASE)).acceptReservedSeat(reservation.getTheaterId(), clientId, reservation.getPlace().getRow(), reservation.getPlace().getColumn())) {
			TimeoutManager timeout = timeoutMap.get(clientId);
			timeout.stop();
			timeoutMap.remove(clientId);
			reservationMap.remove(clientId);
			Debugger.log("Confirmed reservation for clientId " + clientId);
			return true;
		}
		return false;
	}

	// TODO Deixa de se ligar à DB
	@Override
	public synchronized boolean cancelReservation(int clientId) throws RemoteException{
		Reservation reservation = reservationMap.get(clientId);
		if(reservation == null) {
			return false;
		}
		TimeoutManager timeout = timeoutMap.get(clientId);
		timeout.stop();
		timeoutMap.remove(clientId);
		reservationMap.remove(clientId);
		Debugger.log("Canceled reservation for clientId " + clientId);
		return true;
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

	// TODO Analisar isto como deve ser
	public void unbind() {
		try {
			Registry registry = LocateRegistry.getRegistry(1090);
			registry.unbind("WideBoxServer");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

}
