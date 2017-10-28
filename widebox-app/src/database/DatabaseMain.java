package database;

import common.Seat;

import java.io.*;

public class DatabaseMain {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		WideBoxDatabaseImpl wbi = new WideBoxDatabaseImpl();


		Seat[][] seats = wbi.getTheatersInfo(1);
		wbi.reserveSeat(1, 1, 0,0);
		wbi.acceptReservedSeat(1, 1, 0,0);
		wbi.reserveSeat(1, 1, 0,1);
		wbi.cancelReservation(1, 1, 0,1);


	}

}
