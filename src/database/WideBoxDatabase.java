package database;

import java.rmi.Remote;
import java.util.Map;

import common.Theater;

public interface WideBoxDatabase extends Remote{

	public Map<Integer, String> getTeaters();

	public Theater getTheaterInfo(int theaterId);

	public boolean reserveSeat(int clientId, int row, int column);

	public boolean acceptReservedSeat(int clientId);

	public boolean cancelReservation(int clientId);

}