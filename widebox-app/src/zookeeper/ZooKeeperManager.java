package zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;

public interface ZooKeeperManager {

    void createPersistent(String path, byte[] data) throws KeeperException, InterruptedException;
    void createEphemeral(String path, byte[] data) throws KeeperException, InterruptedException;
    void setData(String path, byte[] data) throws KeeperException, InterruptedException;
    byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException;
    boolean exists(String path, Watcher watcher) throws KeeperException, InterruptedException;
    List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException;
    void registerWatcher(Watcher watcher) throws KeeperException, InterruptedException;
    void delete(String path) throws KeeperException, InterruptedException;
    void closeConnection();

}
