package server;

import java.rmi.Remote;
import java.util.Map;

import common.Theater;

public interface WideBoxServer extends Remote{

	Map<Integer, String> getTheaters();

	Theater getTheaterInfo(int theaterId);

	boolean reserveSeat(int clientId, int row, int column);

	boolean acceptReservedSeat(int clientId);

	boolean cancelReservation(int clientId);

	boolean stopServer();

	boolean startServer();

}