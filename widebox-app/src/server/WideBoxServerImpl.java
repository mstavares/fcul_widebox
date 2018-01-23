package server;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.Debugger;
import common.InstanceSelector;
import common.InstanceType;
import common.Seat;
import common.Server;
import common.TimeoutManager;
import common.Utilities;
import exceptions.FullTheaterException;
import exceptions.NotOwnerException;


public class WideBoxServerImpl extends UnicastRemoteObject implements WideBoxServer, SeatTimeoutListener {

	private static final long serialVersionUID = 6332295204270798892L;

	private InstanceSelector instanceSelector;

	/** Map to keep track of Open Seat Reservations **/
	private Map<Integer, Reservation> reservationMap;
	
	/** """Cache""" **/
	private Map<String, Integer> theatherMap;

	/** Server server properties object */
	private ServerProperties properties;
	
	private ServerPoolManager lifeguard;
	private Map<String, Server> servers;
	
	// private int[] lastFreeSeat;

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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + column;
			result = prime * result + row;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Place other = (Place) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (column != other.column)
				return false;
			if (row != other.row)
				return false;
			return true;
		}

		private WideBoxServerImpl getOuterType() {
			return WideBoxServerImpl.this;
		}
	}

	private class Reservation {
		private Place place;
		private int theaterId;
		private TimeoutManager timeoutManager;

		public Reservation(int theaterId, Place place, TimeoutManager timeoutManager) {
			this.theaterId = theaterId;
			this.place = place;
			this.timeoutManager = timeoutManager;
		}

		public int getTheaterId() {
			return this.theaterId;
		}

		public Place getPlace() {
			return this.place;
		}
		
		public TimeoutManager getTimeoutManager() {
			return this.timeoutManager;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((place == null) ? 0 : place.hashCode());
			result = prime * result + theaterId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Reservation other = (Reservation) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (place == null) {
				if (other.place != null)
					return false;
			} else if (!place.equals(other.place))
				return false;
			if (theaterId != other.theaterId)
				return false;
			return true;
		}

		private WideBoxServerImpl getOuterType() {
			return WideBoxServerImpl.this;
		}
	}


	public WideBoxServerImpl() throws IOException, RemoteException, NotBoundException {
		super();
		instanceSelector = InstanceSelector.getInstance();
		servers = new HashMap<>();
		lifeguard = new ServerPoolManager(servers);
		registerService();
		properties = new ServerProperties();
		reservationMap = new HashMap<>();
		
		try {
			/** """Cache""" the theather map for faster responde for getTheaters requests **/
			theatherMap = lifeguard.getDatabaseServing(0).getTheaters(); //TODO make it search for a random instance
			// lastFreeSeat = new int[theatherMap.size()];
		}catch(NullPointerException e) {
			Debugger.log("Error communicating with the DB, exiting.");
			System.exit(-1);
		}

		
		Debugger.log("Application server is ready");
	}
	
	
	private void registerService() throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry( Utilities.getPort() );
			registry.bind("WideBoxServer", this);
			Debugger.log("Successfully binded Application Server to Starter Registry");
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
			throw new RemoteException("Error creating registry");
		}
	}


	@Override
	public Map<String, Integer> getTheaters() throws RemoteException{
		Debugger.log("Got Request for Theaters");
		return theatherMap;
	}


	@Override
	public Seat[][] getTheaterInfo(int theaterId, int clientId) throws RemoteException, FullTheaterException, NotOwnerException{
		Debugger.log("Got info request for theather " + theaterId + " from clientID " + clientId);
		printServers();
		if(!instanceSelector.getInstanceServingTheater(theaterId, InstanceType.APP).getIp().equals(Utilities.getOwnIp())) {
			Debugger.log("Not responsible for this server");
			throw new NotOwnerException("This server is not responsible for that theater");
		}
		Seat[][] seats = lifeguard.getDatabaseServing(theaterId).getTheatersInfo(theaterId);
		if(!clientHasReservation(clientId)) {
			synchronized(reservationMap) {
				Debugger.log("In sync");
				Place seat = pickFreeSeat(seats, theaterId);
				if(!reserveSeat(theaterId, clientId, seat.getRow(), seat.getColumn())) {
					Debugger.log("Seat was not reserved");
					throw new RemoteException("Error automatically reserving seat");
				}
				seats[seat.getRow()][seat.getColumn()].setSelf();
			}
		} else {
			Reservation reservation = reservationMap.get(clientId);
			seats[reservation.getPlace().getRow()][reservation.getPlace().getColumn()].setSelf();
		}
		return seats;
	}

	
	@Override
	public synchronized boolean reserveSeat(int theaterId, int clientId, int row, int column) throws RemoteException, NotOwnerException{
		if(!instanceSelector.getInstanceServingTheater(theaterId, InstanceType.APP).getIp().equals(Utilities.getOwnIp())) {
			Debugger.log("Not responsible for this server");
			throw new NotOwnerException("This server is not responsible for that theater");
		}
		if (clientHasReservation(clientId)) {
			cancelReservation(clientId);
		}	
		Debugger.log("Reserving seat for clientID " + clientId);
		Reservation reservation = new Reservation(theaterId, new Place(row, column), new TimeoutManager(this, properties.getTimeoutValue(), clientId));
		reservationMap.put(clientId, reservation);
		reservation.getTimeoutManager().runOnlyOnce();
		return true;
	}
	
	@Override
	public synchronized boolean acceptReservedSeat(int clientId) throws RemoteException{
		Reservation reservation = reservationMap.get(clientId);
		if(reservation == null) {
			return false;
		}
		// I'm sorry. I'm so sorry.
		if (lifeguard.getDatabaseServing(reservation.getTheaterId()).acceptReservedSeat(reservation.getTheaterId(), clientId, reservation.getPlace().getRow(), reservation.getPlace().getColumn())) {
			reservation.getTimeoutManager().stop();
			reservationMap.remove(clientId);
			Debugger.log("Confirmed reservation for clientId " + clientId);
			return true;
		}
		return false;
	}
	
	@Override
	public synchronized boolean cancelReservation(int clientId) throws RemoteException{
		Reservation reservation = reservationMap.get(clientId);
		if(reservation == null) {
			return false;
		}
		reservation.getTimeoutManager().stop();
		reservationMap.remove(clientId);
		Debugger.log("Canceled reservation for clientId " + clientId);
		return true;
	}

	private Place pickFreeSeat(Seat[][] seats, int theaterId) throws FullTheaterException {
		for (int i = 0; i < seats.length; i++) {
			for (int k = 0; k < seats[i].length; k++) {
				if(seats[i][k].isFree() && !reservationMap.containsValue(new Reservation(theaterId, new Place(i, k), null)))
					return new Place(i, k);
			}
		}
		throw new FullTheaterException("Theater " + theaterId +" is full");
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

	public void unbind() {
		try {
			lifeguard.terminate();
			Registry registry = LocateRegistry.getRegistry(1090);
			registry.unbind("WideBoxServer");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, Server> getServerList() throws RemoteException {
		return servers;
	}
	
	private void printServers() {
		List<Map.Entry<String, Server>> entries = new LinkedList<>(servers.entrySet());
		for(Map.Entry<String, Server> entry: entries) {
			System.out.println(entry.getKey() + " " + entry.getValue().getIp());
		}
	}

}
