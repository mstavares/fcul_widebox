package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import common.Seat;
import common.Server;
import exceptions.FullTheaterException;
import exceptions.NotOwnerException;

public interface WideBoxServer extends Remote{

	Map<String, Integer> getTheaters() throws RemoteException;

	Seat[][] getTheaterInfo(int theaterId, int clientId) throws RemoteException, FullTheaterException, NotOwnerException;

	boolean reserveSeat(int theaterId, int clientId, int row, int column) throws RemoteException, NotOwnerException;

	boolean acceptReservedSeat(int clientId) throws RemoteException;

	boolean cancelReservation(int clientId) throws RemoteException;
	
	Map<String, Server> getServerList() throws RemoteException;
	
}
