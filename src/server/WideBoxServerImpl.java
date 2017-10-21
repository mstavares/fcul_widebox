package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import common.Theater;


public class WideBoxServerImpl extends UnicastRemoteObject implements WideBoxServer {
	
	private boolean online;
	
	
	public WideBoxServerImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public Map<Integer,String> getTheaters(){
		//TODO
		return null;
	}
	
	
	@Override
	public Theater getTheaterInfo(int theaterId){
		//TODO
		return null;
	}
	
	
	@Override
	public boolean reserveSeat(int clientId, int row, int column){
		//TODO
		return false;
	}
	
	
	@Override
	public boolean acceptReservedSeat(int clientId){
		//TODO
		return false;
	}
	
	
	@Override
	public boolean cancelReservation(int clientId){
		//TODO
		return false;
	}
	
	
	@Override
	public boolean stopServer(){
		if (!online)
			return false;
		
		online = false;
		return true;
	}
	
	
	@Override
	public boolean startServer(){
		if (online)
			return false;
		
		online = true;
		return true;
	}
	
}
