package database;

import common.*;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

class DatabasePoolManager implements Watcher {

    private static final String ROOT_ZNODE = "/widebox";
    private static final String DATABASE_ZNODE = ROOT_ZNODE + "/database";
    private static final String DATABASE_ZNODE_DIR = ROOT_ZNODE + "/database/";
    private ZooKeeperManager zkmanager = ZooKeeperManagerImpl.getInstace();
    private DatabasePoolManagerListener listener;

    private Server backupServer;
    private String backupZnode;
    private String myZnode;


    DatabasePoolManager(DatabasePoolManagerListener listener) {
        try {
            this.listener = listener;
            initialize();
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }


    /**
     * Inicializa a árvore de znodes relativa as bases de dados e cria o seu proprio znode
     */
    private void initialize() throws IOException, KeeperException, InterruptedException {
        DatabaseProperties dbp = new DatabaseProperties();
        InstanceManager instanceManager = InstanceManager.getInstance();
        Server server = fetchMyServerData(instanceManager);
        int numberOfDatabases = instanceManager.getServers(InstanceType.DATABASE).size();
        int numberOfTheaters = (dbp.getNumberOfTheaters() / numberOfDatabases);
        createRoot();
        for(int i = 1; i <= numberOfDatabases; i++) {
            myZnode = String.valueOf(computeMyLastTheater(numberOfTheaters, i));
            String fullZnodeToCreate = DATABASE_ZNODE_DIR + myZnode;
            if (!zkmanager.exists(fullZnodeToCreate, null)) {
                zkmanager.createEphemeral(fullZnodeToCreate, server.getBytes());
                Debugger.log("Criei o meu znode " + fullZnodeToCreate);
                List<String> znodes = fetchDatabaseZnodes();
                computeZnodeBackup(numberOfTheaters, numberOfDatabases, i);
                checkZnodeBackupIpAddress();
                printZnodes(znodes);
                break;
            }
        }
    }

    private int computeMyLastTheater(int numberOfTheaters, int i) {
        return (numberOfTheaters * i) - 1;
    }

    private int computeMyBackupLastTheater(int numberOfTheaters, int i) {
        return (numberOfTheaters * (i + 1)) - 1;
    }

    private Server fetchMyServerData(InstanceManager instanceManager) {
        String myIpAddress = Utilities.getOwnIp();
        List<Server> databaseServers = instanceManager.getServers(InstanceType.DATABASE);
        for(Server server : databaseServers) {
            if(server.getIp().equals(myIpAddress)) {
                Debugger.log(server.toString());
                return server;
            }
        }
        /* TODO lancar excecao em vez de retornar null? */
        return null;
    }


    /**
     * Inicializa a árvore DATABASE_ZNODE -> /widebox/database
     */
    private void createRoot() throws KeeperException, InterruptedException {
        if (!zkmanager.exists(ROOT_ZNODE, null)) {
            zkmanager.createPersistent(ROOT_ZNODE, null);
            zkmanager.createPersistent(DATABASE_ZNODE, null);
            Debugger.log("Znode created " + DATABASE_ZNODE);
        } else if (!zkmanager.exists(DATABASE_ZNODE, null)) {
            zkmanager.createPersistent(DATABASE_ZNODE, null);
            Debugger.log("Znode created " + DATABASE_ZNODE);
        }
    }

    /**
     * Devolve os znodes existentes em /widebox/database
     */
    private List<String> fetchDatabaseZnodes() throws KeeperException, InterruptedException {
        return zkmanager.getChildren(DATABASE_ZNODE, null);
    }

    /**
     * Vai calcular o znode que vai servir de backup / secundario
     */
    private void computeZnodeBackup(int numberOfTheaters, int numberOfDatabases, int i) throws KeeperException, InterruptedException {
        int lastBackupTheater;
        if(i == numberOfDatabases) {
            lastBackupTheater = numberOfTheaters - 1;
            backupZnode = String.valueOf(lastBackupTheater);
        } else {
            lastBackupTheater = computeMyBackupLastTheater(numberOfTheaters, i);
            backupZnode = String.valueOf(lastBackupTheater);
        }
        listener.onReceiveMyTheaterRange(lastBackupTheater);
        Debugger.log("My backup server is " + backupZnode);
    }

    private void checkZnodeBackupIpAddress() throws KeeperException, InterruptedException {
        try {
            if (zkmanager.exists(DATABASE_ZNODE_DIR + backupZnode, this)) {
                backupServer = Server.buildObject(zkmanager.getData(DATABASE_ZNODE_DIR + backupZnode, null));
                Debugger.log("Backup server data : " + backupServer.toString());
                listener.backupServerIsAvailable(backupServer);
            } else {
                backupServer = null;
                Debugger.log("Backup server is not available");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * >>> Para efeitos de debugging <<<
     * Imprime os znodes existentes na arvore
     *      Marca com PP XX PP o proprio / primario znode.
     *      Marca com BB XX BB o backup / secundario znode.
     */
    private void printZnodes(List<String> databaseZnodes) {
        Debugger.log("++++++++++++++++++++++++++++++++++");
        for(String znode : databaseZnodes) {
            if(znode.equals(myZnode))
                Debugger.log("PP " + znode + " PP");
            else if(znode.equals(backupZnode))
                Debugger.log("BB " + znode + " BB");
            else
                Debugger.log("-- " + znode + " --");
        }

        Debugger.log("++++++++++++++++++++++++++++++++++");
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        Debugger.log("Watch event!");
        try {
            List<String> znodes = fetchDatabaseZnodes();
            checkZnodeBackupIpAddress();
            printZnodes(znodes);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
