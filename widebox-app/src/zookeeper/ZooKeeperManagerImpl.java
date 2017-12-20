package zookeeper;

import common.Debugger;
import common.InstanceManager;
import common.InstanceType;
import org.apache.zookeeper.*;

public class ZooKeeperManagerImpl implements ZooKeeperManager {

    private static final ZooKeeperManagerImpl INSTANCE = new ZooKeeperManagerImpl();
    private static ZooKeeperConnection zkConnection;
    private static ZooKeeper zkeeper;

    private ZooKeeperManagerImpl() {
        initialize();
    }

    public static ZooKeeperManager getInstace() {
        return INSTANCE;
    }

    /**
     * Initialize connection
     */
    private void initialize() {
        try {
            InstanceManager instanceManager = InstanceManager.getInstance();
            String zookeeperIpAddress = instanceManager.getServers(InstanceType.ZOOKEEPER).get(0).getIp();
            zkConnection = new ZooKeeperConnection();
            zkeeper = zkConnection.connect(zookeeperIpAddress);
            Debugger.log("ZooKeeper connection initialized");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void create(String path, byte[] data) throws KeeperException, InterruptedException {
        zkeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        Debugger.log("Node " + path + " created successfully");
    }

    @Override
    public void setData(String path, byte[] data) throws KeeperException, InterruptedException {
        zkeeper.setData(path, data, zkeeper.exists(path, true).getVersion());
        Debugger.log("Node " + path + " updated successfully");
    }

    @Override
    public byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zkeeper.getData(path, watcher, null);
    }

    @Override
    public void delete(String path) throws KeeperException, InterruptedException {
        zkeeper.delete(path, zkeeper.exists(path, true).getVersion());
        Debugger.log("Node " + path + " was deleted successfully");
    }

    /**
     * Close the zookeeper connection
     */
    @Override
    public void closeConnection() {
        try {
            zkConnection.close();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
