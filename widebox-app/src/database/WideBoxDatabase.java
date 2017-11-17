package database;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import common.Seat;

public interface WideBoxDatabase extends Remote {

	public Map<String, Integer> getTheaters() throws RemoteException;

	public Seat[][] getTheatersInfo(int theaterId) throws RemoteException;

	// Este método e o cancel deixam de ser necessários visto não guardarmos as reservas na DB
	// public boolean reserveSeat(int theaterId, int clientId, int row, int column) throws RemoteException;

	public boolean acceptReservedSeat(int theaterId, int clientId, int row, int column) throws RemoteException;

	// public boolean cancelReservation(int theaterId, int clientId, int row, int column) throws RemoteException;

}