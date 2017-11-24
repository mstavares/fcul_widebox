package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import common.InstanceSelector;
import common.InstanceType;
import common.Seat;
import common.Server;
import exceptions.FullTheaterException;
import server.WideBoxServer;

public class WideBoxClient {


	private int id;
	private static final InstanceType INSTANCE_TYPE = InstanceType.APP;
	private InstanceSelector instanceSelector;
	private HashMap<Server, WideBoxServer> servers;
	private WideBoxServer currentReservation;

	public WideBoxClient(int clientId) throws RemoteException{
		id = clientId;
		servers = new HashMap<Server, WideBoxServer>();

		try {
			instanceSelector = InstanceSelector.getInstance();
			Server initialServer = instanceSelector.getRandomInstance(INSTANCE_TYPE);
			
			Registry registry = LocateRegistry.getRegistry(initialServer.getIp(), initialServer.getPort() );
			WideBoxServer wideBoxServer = (WideBoxServer) registry.lookup("WideBoxServer");
			
			servers.put(initialServer, wideBoxServer);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("Error connecting to the server.");
		}

	}

	public Map<String, Integer> getTheaters() throws RemoteException{
		return servers.get(servers.keySet().iterator().next() ).getTheaters();
	}


	public Seat[][] getTheaterInfo(int theaterId) throws RemoteException, FullTheaterException{
		currentReservation = getServerServing(theaterId);
		return currentReservation.getTheaterInfo(theaterId, id);
	}


	public boolean reserveSeat(int theaterId, int row, int column) throws RemoteException{
		currentReservation = getServerServing(theaterId);
		return currentReservation.reserveSeat(theaterId, id, row, column);
	}


	public boolean acceptReservedSeat() throws RemoteException{
		if (currentReservation != null)
			return currentReservation.acceptReservedSeat(id);
		else
			throw new RemoteException("There's no current reservation.");
	}


	public boolean cancelReservation() throws RemoteException{
		if (currentReservation != null)
			return currentReservation.cancelReservation(id);
		else
			throw new RemoteException("There's no current reservation.");
	}
	
	
	private WideBoxServer getServerServing(int theaterId) throws RemoteException{
		Server s = instanceSelector.getInstanceServingTheater(theaterId, INSTANCE_TYPE);
		WideBoxServer wideBoxServer;
		
		if (servers.containsKey(s))
			wideBoxServer = servers.get(s);
		else{
			try{
				Registry registry = LocateRegistry.getRegistry(s.getIp(), s.getPort() );
				wideBoxServer = (WideBoxServer) registry.lookup("WideBoxServer");
				servers.put(s, wideBoxServer);
			}catch(RemoteException | NotBoundException e){
				e.printStackTrace();
				throw new RemoteException("Error connecting to the server.");
			}

		}
		
		return wideBoxServer;
	}

}
