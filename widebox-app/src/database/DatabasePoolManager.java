package database;

import common.Debugger;
import common.InstanceManager;
import common.InstanceType;
import common.Utilities;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

import java.io.IOException;

public class DatabasePoolManager implements Watcher {

    private static final String ROOT_ZNODE = "/widebox";
    private static final String DATABASE_ZNODE = ROOT_ZNODE + "/database";
    private static final String DATABASE_ZNODE_DIR = ROOT_ZNODE + "/database/";
    private ZooKeeperManager zkmanager = ZooKeeperManagerImpl.getInstace();
    private String znodeToWatch;

    DatabasePoolManager() {
        try {
            initialize();
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    private void initialize() throws IOException, KeeperException, InterruptedException {
        DatabaseProperties dbp = new DatabaseProperties();
        InstanceManager instanceManager = InstanceManager.getInstance();
        int numberOfDatabases = instanceManager.getServers(InstanceType.DATABASE).size();
        int numberOfTheaters = (dbp.getNumberOfTheaters() / numberOfDatabases);
        createRoot();
        for(int i = 1; i <= numberOfDatabases; i++) {
            int lastTheater = (numberOfTheaters * i) - 1;
            String znodeToCreate = DATABASE_ZNODE_DIR + lastTheater;
            if (!zkmanager.exists(znodeToCreate, null)) {
                zkmanager.createEphemeral(znodeToCreate, Utilities.getOwnIp().getBytes());
                Debugger.log("Criei o meu znode " + znodeToCreate);
                createWatch(numberOfTheaters, i);
                break;
            }
        }
    }

    private void createRoot() throws KeeperException, InterruptedException {
        if (!zkmanager.exists(ROOT_ZNODE, null)) {
            zkmanager.createPersistent(ROOT_ZNODE, null);
            zkmanager.createPersistent(DATABASE_ZNODE, null);
            Debugger.log("Criei o znode " + DATABASE_ZNODE);
        } else if (!zkmanager.exists(DATABASE_ZNODE, null)) {
            zkmanager.createPersistent(DATABASE_ZNODE, null);
            Debugger.log("Criei o znode " + DATABASE_ZNODE);
        }
    }

    private void createWatch(int numberOfTheaters, int i) throws KeeperException, InterruptedException {
        if(i != 1) {
            int prevTheater = (numberOfTheaters * (i - 1)) - 1;
            znodeToWatch = DATABASE_ZNODE_DIR + prevTheater;
            zkmanager.exists(znodeToWatch, this);
            Debugger.log("Criei um watch para " + znodeToWatch);
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        Debugger.log("ENTREI NO WATCH");
        try {
            if(!zkmanager.exists(znodeToWatch, this)) {
                Debugger.log("O MEU ZNODE FOI ELIMINADO");
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
