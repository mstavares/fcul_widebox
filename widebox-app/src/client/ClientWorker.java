package client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import common.InstanceManager;

public class ClientWorker {
	
	private static ClientWorker instance;
	
	private final int NRTH;
	private String serverIp;
	private int serverPort;
	private AtomicInteger currentClientId;
	private AtomicInteger failedClients;
	private List<Long> finishedClients;
	private List<Long> finishedLateClients;
	//ConcurrentLinkedQueue invés de List, maybe?
	
	private ClientWorker() throws Exception{
		//TODO multi server support
		InstanceManager serverManager = InstanceManager.getInstance();
		serverIp = serverManager.getAppServers().get(0).getIp();
		serverPort = serverManager.getAppServers().get(0).getPort();
		currentClientId = new AtomicInteger(0);
		failedClients = new AtomicInteger(0);
		finishedClients = Collections.synchronizedList(new ArrayList<Long>() );
		finishedLateClients = Collections.synchronizedList(new ArrayList<Long>() );
		NRTH = 1500; //TODO get this from the file
	}
	
	
	public static ClientWorker getInstance() throws Exception{
		//TODO possiveis problemas de concurrencia?
		if (instance == null)
			instance = new ClientWorker();
		return instance;
	}
	
	
	public Result sendRequests(int numClients, int numTeathers, boolean confirm) throws RemoteException{
		Long[] requestsCompleted;
		Long[] previousRequests;
		int[] theaters = new int[numTeathers];
		Random rd = new Random();
		
		for (int i = 0; i < numTeathers; i++){
			theaters[i] = rd.nextInt(NRTH) + 1;
		}
		
		for (int i = 0; i < numClients; i++){
			new Thread(new ClientRunnable(theaters, confirm) ).start();
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		synchronized(finishedClients){
			requestsCompleted = finishedClients.toArray(new Long[finishedClients.size()] );
			finishedClients.clear();
		}
		
		synchronized(finishedLateClients){
			previousRequests = finishedLateClients.toArray(new Long[finishedLateClients.size()] );
			finishedLateClients.clear();
		}
		
		return new Result(requestsCompleted, previousRequests, failedClients.getAndSet(0) );
	}
	
	
	public class Result{
		private Long[] requestsCompleted;
		private Long[] previousRequests;
		private int failedRequests;
		
		public Result(Long[] requestsCompleted, Long[] previousRequests, int failedRequests) {
			this.requestsCompleted = requestsCompleted;
			this.previousRequests = previousRequests;
			this.failedRequests = failedRequests;
		}

		public Long[] getRequestsCompleted() {
			return requestsCompleted;
		}

		public Long[] getPreviousRequests() {
			return previousRequests;
		}
		
		public int getFailedRequests(){
			return failedRequests;
		}	
	}
	
	
	private class ClientRunnable implements Runnable{

		private int[] theaters;
		private boolean confirm;

		public ClientRunnable(int[] theaters, boolean confirm) {
			this.theaters = theaters;
			this.confirm = confirm;
		}

		@Override
		public void run() {
			long initialTime = System.currentTimeMillis();
			try {
				int clientId = currentClientId.getAndIncrement();
				WideBoxClient client = new WideBoxClient(clientId, serverIp, serverPort);
				client.getTheaterInfo( theaters[ ThreadLocalRandom.current().nextInt(theaters.length) ] );
				//TODO what if it's full?
				
				if (confirm){
					if ( !client.acceptReservedSeat() ){
						failedClients.incrementAndGet();
						return;
					}	
				}
				
				long totalTime = System.currentTimeMillis() - initialTime;
				
				if (totalTime < 1000)
					finishedClients.add(totalTime);
				else
					finishedLateClients.add(totalTime);
				
			} catch (RemoteException e) {
				failedClients.incrementAndGet();
			}
			
		}
		
	}
	
}
