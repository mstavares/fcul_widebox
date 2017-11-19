package client.text;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import client.WideBoxClient;

public class TrafficGenerator {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int numClients;		
		
		System.out.println("Insert the number of clients to use:");
		numClients = sc.nextInt();
		
		for (int clientId = 1; clientId <= numClients; clientId++){
			new Thread(new ClientRunnable(clientId)).start();
		}
		
		sc.close();
	}
	
	
	private static class ClientRunnable implements Runnable {
		
		private int clientId;

		public ClientRunnable(int clientId) {
			this.clientId = clientId;
		}

		@Override
		public void run() {
			
			try {
				WideBoxClient client = new WideBoxClient(clientId);
				
				Map<String, Integer>  theaterList = client.getTheaters();
				
				client.getTheaterInfo( new Random().nextInt(theaterList.size()) );
				
				if (client.acceptReservedSeat() )
					System.out.println("Client " + clientId + ": Accept the reserved seat.");
				else
					System.out.println("Client " + clientId + ": Error accepting reserved seat.");
			} catch (RemoteException e) {
				System.out.println("Client " + clientId + ": Error connecting to the server.");
			}
			
		}
		
	}

}
