package server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class ServerMain {

	public static void main(String[] args) throws IOException, RemoteException {
		String serverIp;
		int serverPort;
		Scanner sc = new Scanner(System.in);
		
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
		
		sc.close();
		new WideBoxServerImpl(serverIp, serverPort);
	}

}
