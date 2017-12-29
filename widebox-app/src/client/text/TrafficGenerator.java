package client.text;

import java.rmi.RemoteException;
import java.util.Scanner;

import client.ClientWorker;
import client.ClientWorker.Result;

public class TrafficGenerator {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		boolean work = true;
		
		System.out.println("Insert the number of theaters to use:");
		int numTeathers = sc.nextInt();
		
		System.out.println("Insert the number of clients to create each second:");
		int numClients = sc.nextInt();
		
		System.out.println("Do you want to confirm reservations? (Y/N)");
		boolean confirm = (sc.next().equals("Y") ? true : false);
		
		
		ClientWorker clientWorker = ClientWorker.getInstance();
		boolean newTheaters = true;
		while(work) {
			System.out.println("----------");
			try {
				Result r = clientWorker.sendRequests(numClients, numTeathers, confirm, newTheaters);
				System.out.println("Requests completed this second: " + r.getRequestsCompleted().length);
				System.out.println("Previous requests completed now: " + r.getPreviousRequests().length);
				System.out.println("Number of failed requests: " + r.getFailedRequests());
			} catch (RemoteException e) {
				System.out.println("Error connecting to the server.");
			}
			newTheaters = false;
			System.out.println("----------");
		}
		
		sc.close();
	}
	
}
