package client.text;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import client.WideBoxClient;

public class TrafficGenerator {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String serverIp;
		int serverPort;
		int numClients;
		
		if (args.length >= 2){
			serverIp = args[0];
			serverPort = Integer.parseInt(args[1]);
		}else{
			System.out.println("Couldn't get ip and port arguments.");
			System.out.println("Insert the server IP:");
			serverIp = sc.nextLine();

			System.out.println("Insert the server port:");
			serverPort = sc.nextInt();
		}
		
		
		System.out.println("Insert the number of clients to use:");
		numClients = sc.nextInt();
		
		for (int clientId = 1; clientId <= numClients; clientId++){
			new Thread(new ClientRunnable(clientId, serverIp, serverPort)).start();
		}
		
		sc.close();
	}
	
	
	private static class ClientRunnable implements Runnable {
		
		private int clientId;
		private String serverIp;
		private int serverPort;

		public ClientRunnable(int clientId, String serverIp, int serverPort) {
			this.clientId = clientId;
			this.serverIp = serverIp;
			this.serverPort = serverPort;
		}

		@Override
		public void run() {
			
			try {
				WideBoxClient client = new WideBoxClient(clientId, serverIp, serverPort);
				
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
