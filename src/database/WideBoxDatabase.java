package database;

import java.util.Map;

import common.Theater;


public class WideBoxDatabase {
	
	//opcodes to use when writing to the log
	private final static int ACCEPT_ACTION = 10;
	private final static int CANCEL_ACTION = 20;
	private final static int RESERVE_ACTION = 50;
	
	private Map<Integer,Boolean[][]> database;
	
	
	public WideBoxDatabase(){
		
		//TODO if there isn't a Database.dat file
		// create one based on the details on the Server.properties file with all seats at 0.
		
		//TODO If there is a Database.dat file and a Database.log file
		// then update the Database with the log file and clear the log.

		//TODO Restore Database.dat to the database object.
	}
	
	
	private Map<Integer,String> getTeaters(){
		
	}
	
	
	private Theater getTheaterInfo(int theaterId){
		
	}
	
	
	private synchronized boolean reserveSeat(int clientId, int row, int column){
		
	}
	
	
	private synchronized boolean acceptReservedSeat(int clientId){
		
	}
	
	
	private synchronized boolean cancelReservation(int clientId){
		
	}
	
	
}
