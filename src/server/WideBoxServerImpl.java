package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import common.Theater;


public class WideBoxServerImpl extends UnicastRemoteObject implements WideBoxServer {
	
	private static final long serialVersionUID = 6332295204270798892L;
	private boolean online;
	
	
	public WideBoxServerImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public Map<Integer,String> getTheaters() throws RemoteException{
		//TODO
		return null;
	}
	
	
	@Override
	public Theater getTheaterInfo(int theaterId) throws RemoteException{
		//TODO
		return null;
	}
	
	
	@Override
	public boolean reserveSeat(int clientId, int row, int column) throws RemoteException{
		//TODO
		return false;
	}
	
	
	@Override
	public boolean acceptReservedSeat(int clientId) throws RemoteException{
		//TODO
		return false;
	}
	
	
	@Override
	public boolean cancelReservation(int clientId) throws RemoteException{
		//TODO
		return false;
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
	
}
