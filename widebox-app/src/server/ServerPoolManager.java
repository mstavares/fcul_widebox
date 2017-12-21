package server;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import common.Debugger;
import common.InstanceManager;
import common.InstanceType;
import common.Utilities;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

public class ServerPoolManager implements Watcher {
	private static final String ROOT_ZNODE = "/widebox";
    private static final String SERVER_ZNODE = ROOT_ZNODE + "/appserver";
    private static final String SERVER_ZNODE_DIR = ROOT_ZNODE + "/appserver/";
    private ZooKeeperManager zkmanager = ZooKeeperManagerImpl.getInstace();
    private String znodeToWatch;
    
    ServerPoolManager() {
    	try {
            initialize();
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
    
    private void initialize() throws IOException, KeeperException, InterruptedException {
    	ServerProperties sbp = new ServerProperties();
        InstanceManager instanceManager = InstanceManager.getInstance();
        int numberOfServers = instanceManager.getServers(InstanceType.APP).size();
        int numberOfTheaters = (sbp.getNumberOfTheaters() / numberOfServers);
        createRoot();
        for(int i = 1; i <= numberOfServers; i++) {
            int lastTheater = (numberOfTheaters * i) - 1;
            String znodeToCreate = SERVER_ZNODE_DIR + lastTheater;
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
            zkmanager.createPersistent(SERVER_ZNODE, null);
            Debugger.log("Criei o znode " + SERVER_ZNODE);
        } else if (!zkmanager.exists(SERVER_ZNODE, null)) {
            zkmanager.createPersistent(SERVER_ZNODE, null);
            Debugger.log("Criei o znode " + SERVER_ZNODE);
        }
    }
    
    private void createWatch(int numberOfTheaters, int i) throws KeeperException, InterruptedException {
        if(i != 1) {
            int prevTheater = (numberOfTheaters * (i - 1)) - 1;
            znodeToWatch = SERVER_ZNODE_DIR + prevTheater;
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
