package database;

import common.*;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

import java.io.IOException;

public class DatabaseMain {

	public static void main(String[] args) throws IOException, ClassNotFoundException, KeeperException, InterruptedException {
		
		try {
			if (args.length > 1)
				Utilities.setPort( Integer.parseInt(args[0]) );
			else
				Utilities.setPort(1098);
		}catch (Exception e) {
			System.out.println("Error with the port given.");
			System.exit(-1);
		}

		//WideBoxDatabaseImpl wbi = new WideBoxDatabaseImpl();
		/** Retirar public ->> public  WideBoxDatabaseImpl serverInstance; */
		DatabaseStarter dbs = new DatabaseStarter();

		Thread.sleep(20 * 1000);

		Seat[][] seats = dbs.serverInstance.getTheatersInfo(1);
		dbs.serverInstance.acceptReservedSeat(1, 1, 0,0);
		seats = dbs.serverInstance.getTheatersInfo(1);

		//new DatabasePoolManager();

		//Thread.sleep(60 * 1000);

		/*
		ZooKeeperManager zkmanager = ZooKeeperManagerImpl.getInstace();
		String bola = "bola";

		if(zkmanager.exists("/teste2", null))
			System.out.println("ENCONTREI O TESTE2");

		zkmanager.createPersistent("/teste", bola.getBytes());
		System.out.println("CRIEI O TESTE");
		zkmanager.createEphemeral("/teste2", bola.getBytes());
		System.out.println("CRIEI O TESTE2");

		byte[] r = zkmanager.getData("/teste", new DatabaseMain());
		System.out.println(new String (r, "UTF-8"));

		if(zkmanager.exists("/teste", null))
			System.out.println("ENCONTREI O TESTE");

		zkmanager.delete("/teste");

		if(!zkmanager.exists("/teste", null))
			System.out.println("ELIMINEI O TESTE");

		if(zkmanager.exists("/teste2", null))
			System.out.println("ENCONTREI O TESTE2");
			*/

/*
		DatabaseProperties dbp = new DatabaseProperties();
		InstanceManager instanceManager = InstanceManager.getInstance();
		int numberOfDatabases = instanceManager.getServers(InstanceType.DATABASE).size();
		int numberOfTheaters = (dbp.getNumberOfTheaters() / numberOfDatabases);

		for(int lastTheater, i = 1; ; i++) {
			lastTheater = (numberOfTheaters * i) - 1;
			if(!zkmanager.exists("/database", null))
				zkmanager.create("/database", null);
			if (!zkmanager.exists("/database/" + lastTheater, null)) {
				Debugger.log("CRIOU!");
				zkmanager.create("/database/" + lastTheater, Utilities.getOwnIp().getBytes());
				break;
			}
		}
*/

		/*
		Seat[][] seats = dbs.serverInstance.getTheatersInfo(1);
		dbs.serverInstance.acceptReservedSeat(1, 1, 0,0);
		seats = dbs.serverInstance.getTheatersInfo(1);
		*/

	}

}
