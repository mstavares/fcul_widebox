package zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZooKeeperConnection implements Watcher {

    private final CountDownLatch connectionLatch = new CountDownLatch(1);
    private ZooKeeper zooKeeper;


    // Method to connect zookeeper
    public ZooKeeper connect(String host) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(host, 2000, this);
        connectionLatch.await();
        return zooKeeper;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            connectionLatch.countDown();
        }
    }

    // Method to disconnect from zookeeper server
    public void close() throws InterruptedException {
        zooKeeper.close();
    }

}
