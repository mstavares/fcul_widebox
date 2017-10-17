package server;

import java.util.Map;

import common.Theater;


public class WideBoxServer {
	
	private boolean online;
	
	public Map<Integer,String> getTheaters(){
		
	}
	
	
	public Theater getTheaterInfo(int theaterId){
		
	}
	
	public boolean reserveSeat(int clientId, int row, int column){
		
	}
	
	
	public boolean acceptReservedSeat(int clientId){
		
	}
	
	
	public cancelReservation(int clientId){
		
	}
	
	
	public boolean stopServer(){
		if (!online)
			return false;
		
		online = false;
		return true;
	}
	
	
	public boolean startServer(){
		if (online)
			return false;
		
		online = true;
		return true;
	}
	
}
