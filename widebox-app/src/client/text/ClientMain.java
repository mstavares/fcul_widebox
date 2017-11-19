package client.text;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import client.WideBoxClient;
import common.Debugger;
import common.Seat;

public class ClientMain {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Random rd = new Random();
		int op;
		int clientId = rd.nextInt(10000); //TODO get this from the properties file

		try {
			WideBoxClient client = new WideBoxClient(clientId);
			Debugger.log("Got Remote Object");
			Map<String, Integer> theaterList = client.getTheaters();

			System.out.println("Choose a theater: ");
			for (Entry<String, Integer> t: theaterList.entrySet()){
				System.out.println(t.getKey() + "- " + t.getValue());
			}

			int theaterId = sc.nextInt();


			boolean finished = false;
			
			while (!finished){
				
				Seat[][] seats = client.getTheaterInfo(theaterId);
				
				for (int i = 0; i < seats.length; i++){
					for (int j = 0; j < seats [i].length; j++){
						if ( seats[i][j].isSelf() )
							System.out.print(ANSI_YELLOW + i + "." + j + " " + ANSI_RESET);
						else if ( seats[i][j].isReserved() )
							System.out.print(ANSI_RED + i + "." + j + " " + ANSI_RESET);
						else if ( !seats[i][j].isFree() )
							System.out.print(ANSI_BLUE + i + "." + j + " " + ANSI_RESET);
						else
							System.out.print(ANSI_GREEN + i + "." + j + " " + ANSI_RESET);
					}
					System.out.print("\n");
				}
				System.out.println("The yellow seat is your current reservation, the red seats are reserved, and the blue seats are occupied.");
				System.out.println("Choose an option:");
				System.out.println("1- Reserve a new seat.");
				System.out.println("2- Accept reserved seat.");
				System.out.println("3- Cancel reservation.");
				System.out.println("0- Exit.");

				op = sc.nextInt();

				switch (op){
					case 1:
						System.out.println("Choose the row of the new seat:");
						int row = sc.nextInt();
						System.out.println("Choose the column of the new seat:");
						int column = sc.nextInt();

						if ( client.reserveSeat(theaterId, row, column) )
							System.out.println("New seat reserved.");
						else
							System.out.println("Error reserving seat.");
						break;

					case 2:
						if ( client.acceptReservedSeat() ){
							System.out.println("Seat accepted.");
							finished = true;
						}else
							System.out.println("Error accepting seat.");
						break;

					case 3:
						if ( client.cancelReservation() ){
							System.out.println("Reservation canceled.");
							finished = true;
						}else
							System.out.println("Error canceling reservation.");
						break;

					case 0:
						finished = true;
						break;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Error connecting to the server.");
		}

		sc.close();
	}

}
