package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import common.Debugger;
import common.InstanceSelector;
import common.InstanceType;
import common.Seat;
import common.Server;
import exceptions.FullTheaterException;
import exceptions.NotOwnerException;
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
		
		for (int i = 1; i < 10; i++) {
			try {
				instanceSelector = InstanceSelector.getInstance();
				Server initialServer = instanceSelector.getRandomInstance(INSTANCE_TYPE);
				
				Registry registry = LocateRegistry.getRegistry(initialServer.getIp(), initialServer.getPort() );
				WideBoxServer wideBoxServer = (WideBoxServer) registry.lookup("WideBoxServer");
				
				servers.put(initialServer, wideBoxServer);
				break;
			} catch (Exception e) {
				Debugger.log("Error connecting to the server. Trying another one. (" + i + ")");
			}
		}
		
		if (servers.size() < 1)
			throw new RemoteException("Error connecting to the server.");
	}

	public Map<String, Integer> getTheaters() throws RemoteException{
		return servers.get(servers.keySet().iterator().next() ).getTheaters();
	}


	public Seat[][] getTheaterInfo(int theaterId) throws RemoteException, FullTheaterException{
		currentReservation = getServerServing(theaterId);
		try {
			return currentReservation.getTheaterInfo(theaterId, id);
		} catch (NotOwnerException e) {
			Debugger.log("Contacted wrong server, fetching list");
			instanceSelector.updateInstances(INSTANCE_TYPE, currentReservation.getServerList() );
			return getTheaterInfo(theaterId);
		} catch (RemoteException e) {
			servers.remove(instanceSelector.getInstanceServingTheater(theaterId, INSTANCE_TYPE));

			for (int i = 1; i < 10; i++) {
				try {
					Debugger.log("Error connecting to the server, contacting another one. (" + i + ")");
					WideBoxServer server = getRemote(instanceSelector.getRandomInstance(INSTANCE_TYPE));
					instanceSelector.updateInstances(INSTANCE_TYPE, server.getServerList() );
					return getTheaterInfo(theaterId);
				}catch(RemoteException e2) {}

			}
			
			throw new RemoteException("Error contacting the servers.");
		}
	}


	public boolean reserveSeat(int theaterId, int row, int column) throws RemoteException{
		currentReservation = getServerServing(theaterId);
		try {
			return currentReservation.reserveSeat(theaterId, id, row, column);
		} catch (NotOwnerException e) {
			Debugger.log("Contacted wrong server, fetching list");
			instanceSelector.updateInstances(INSTANCE_TYPE, currentReservation.getServerList() );
			return reserveSeat(theaterId, row, column);
		} catch (RemoteException e) {
			servers.remove(instanceSelector.getInstanceServingTheater(theaterId, INSTANCE_TYPE));

			for (int i = 1; i < 10; i++) {
				try {
					Debugger.log("Error connecting to the server, contacting another one. (" + i + ")");
					WideBoxServer server = getRemote(instanceSelector.getRandomInstance(INSTANCE_TYPE));
					instanceSelector.updateInstances(INSTANCE_TYPE, server.getServerList() );
					return reserveSeat(theaterId, row, column);
				}catch(RemoteException e2) {}

			}
			
			throw new RemoteException("Error contacting the servers.");
		}
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
				//e.printStackTrace();
				throw new RemoteException("Error connecting to the server.");
			}

		}
		
		return wideBoxServer;
	}

	
	
	private WideBoxServer getRemote(Server server) throws RemoteException{;
		WideBoxServer wideBoxServer;
		
		if (servers.containsKey(server))
			wideBoxServer = servers.get(server);
		else{
			try{
				Registry registry = LocateRegistry.getRegistry(server.getIp(), server.getPort() );
				wideBoxServer = (WideBoxServer) registry.lookup("WideBoxServer");
				servers.put(server, wideBoxServer);
			}catch(RemoteException | NotBoundException e){
				//e.printStackTrace();
				throw new RemoteException("Error connecting to the server.");
			}

		}
		
		return wideBoxServer;
	}
}
