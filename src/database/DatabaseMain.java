package database;

import common.Debugger;
import common.Seat;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class DatabaseMain {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub

		WideBoxDatabaseImpl wbi = new WideBoxDatabaseImpl();
		wbi.getTheaters();
		Seat[][] seats = wbi.getTheatersInfo(1);

	}

}
