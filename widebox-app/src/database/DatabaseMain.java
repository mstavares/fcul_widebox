package database;

import common.Seat;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

import java.io.IOException;

public class DatabaseMain implements Watcher {

	public static void main(String[] args) throws IOException, ClassNotFoundException, KeeperException, InterruptedException {
		//WideBoxDatabaseImpl wbi = new WideBoxDatabaseImpl();
		/** Retirar public ->> public  WideBoxDatabaseImpl serverInstance; */
		//DatabaseStarter dbs = new DatabaseStarter();


		ZooKeeperManager zkmanager = ZooKeeperManagerImpl.getInstace();
		String bola = "bola";
		zkmanager.create("/teste", bola.getBytes());
		System.out.println("OK");


		byte[] r = zkmanager.getData("/teste", new DatabaseMain());
		System.out.println(new String (r, "UTF-8"));
		zkmanager.delete("/teste");

		/*
		Seat[][] seats = dbs.serverInstance.getTheatersInfo(1);
		dbs.serverInstance.acceptReservedSeat(1, 1, 0,0);
		seats = dbs.serverInstance.getTheatersInfo(1);
		*/

	}

	@Override
	public void process(WatchedEvent watchedEvent) {
		System.out.println("ENTREI");
		//ZooKeeperManager zkmanager = ZooKeeperManagerImpl.getInstace();

	}
}
