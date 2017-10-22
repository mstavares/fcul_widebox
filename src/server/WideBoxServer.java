package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import common.Theater;

public interface WideBoxServer extends Remote{

	Map<Integer, String> getTheaters() throws RemoteException;

	Theater getTheaterInfo(int theaterId) throws RemoteException;

	boolean reserveSeat(int clientId, int row, int column) throws RemoteException;

	boolean acceptReservedSeat(int clientId) throws RemoteException;

	boolean cancelReservation(int clientId) throws RemoteException;

	boolean stopServer() throws RemoteException;

	boolean startServer() throws RemoteException;

}