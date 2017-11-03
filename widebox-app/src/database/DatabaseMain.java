package database;

import java.io.IOException;

public class DatabaseMain {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// WideBoxDatabaseImpl wbi = new WideBoxDatabaseImpl();
		new DatabaseStarter();

		/*
		Seat[][] seats = wbi.getTheatersInfo(1);
		wbi.reserveSeat(1, 1, 0,0);
		wbi.acceptReservedSeat(1, 1, 0,0);
		wbi.reserveSeat(1, 1, 0,1);
		wbi.cancelReservation(1, 1, 0,1);
		*/

	}

}
