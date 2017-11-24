package client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import exceptions.FullTheaterException;

public class ClientWorker {
	
	private final int NRTH;
	private AtomicInteger currentClientId;
	private AtomicInteger failedClients;
	private List<Long> finishedClients;
	private List<Long> finishedLateClients;
	private int[] theaters;
	
	private ClientWorker(){
		currentClientId = new AtomicInteger(0);
		failedClients = new AtomicInteger(0);
		finishedClients = Collections.synchronizedList(new ArrayList<Long>() );
		finishedLateClients = Collections.synchronizedList(new ArrayList<Long>() );
		NRTH = 1500; //TODO get this from the file
	}
	
	
	private static class StaticHolder {
		static final ClientWorker INSTANCE = new ClientWorker();
	}
    
	
	public static ClientWorker getInstance(){
		return StaticHolder.INSTANCE;
	}
	
	
	public Result sendRequests(int numClients, int numTeathers, boolean confirm, boolean newTheaters) throws RemoteException{
		Long[] requestsCompleted;
		Long[] previousRequests;
		Random rd = new Random();
		
		if (newTheaters || theaters == null){
			theaters = new int[numTeathers];
			for (int i = 0; i < numTeathers; i++){
				theaters[i] = rd.nextInt(NRTH) + 1;
			}
		}

		
		for (int i = 0; i < numClients; i++){
			new Thread(new ClientRunnable(confirm) ).start();
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

		private boolean confirm;

		public ClientRunnable(boolean confirm) {
			this.confirm = confirm;
		}

		@Override
		public void run() {
			long initialTime = System.currentTimeMillis();
			try {
				int clientId = currentClientId.getAndIncrement();
				WideBoxClient client = new WideBoxClient(clientId);
				client.getTheaterInfo( theaters[ ThreadLocalRandom.current().nextInt(theaters.length) ] );
				
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
				
			} catch (RemoteException | FullTheaterException e) {
				failedClients.incrementAndGet();
			}
			
		}
		
	}
	
}
