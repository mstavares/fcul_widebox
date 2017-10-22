package database;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import common.Theater;

public interface WideBoxDatabase extends Remote{

	public Map<Integer, String> getTeaters() throws RemoteException;

	public Theater getTheaterInfo(int theaterId) throws RemoteException;

	public boolean reserveSeat(int clientId, int row, int column) throws RemoteException;

	public boolean acceptReservedSeat(int clientId) throws RemoteException;

	public boolean cancelReservation(int clientId) throws RemoteException;

}