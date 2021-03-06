package client.text;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import common.InstanceControl;

public class FailureGenerator {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String serverIp;
		int serverPort;
		int op;
		
		System.out.println("****************************");
		System.out.println("* Simple failure Generator *");
		System.out.println("****************************");
		
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
		
		try {
			
			Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
			InstanceControl wideBoxServer = (InstanceControl) registry.lookup("InstanceControl");
			
			boolean finished = false;
			
			while (!finished){
				System.out.println("Choose an option:");
				System.out.println("1- Start server.");
				System.out.println("2- Stop server.");
				System.out.println("3- Is the server online?.");
				System.out.println("0- Exit.");
				
				op = sc.nextInt();
				
				switch(op){
				case 1:
					if( wideBoxServer.startServer() )
						System.out.println("Server started.");
					else
						System.out.println("Error starting the server.");
					break;
				case 2:
					if( wideBoxServer.stopServer() )
						System.out.println("Server stopped.");
					else
						System.out.println("Error stopping the server.");
					break;
				case 3:
					if( wideBoxServer.isOnline() )
						System.out.println("Server is online.");
					else
						System.out.println("Server is offline.");
					break;
				case 0:
					finished = true;
					break;
				}
			}
			
		} catch (RemoteException | NotBoundException e) {
			System.out.println("Error connecting to the server.");
		}
		
		sc.close();
	}

}
