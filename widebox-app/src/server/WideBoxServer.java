package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import common.Seat;
import exceptions.FullTheaterException;

public interface WideBoxServer extends Remote{

	Map<String, Integer> getTheaters() throws RemoteException;

	Seat[][] getTheaterInfo(int theaterId, int clientId) throws RemoteException, FullTheaterException;

	boolean reserveSeat(int theaterId, int clientId, int row, int column) throws RemoteException;

	boolean acceptReservedSeat(int clientId) throws RemoteException;

	boolean cancelReservation(int clientId) throws RemoteException;
	
}
