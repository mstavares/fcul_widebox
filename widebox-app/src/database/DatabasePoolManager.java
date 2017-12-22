package database;

import common.Debugger;
import common.InstanceManager;
import common.InstanceType;
import common.Utilities;
import exceptions.NoBackupZnodeException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import zookeeper.ZooKeeperManager;
import zookeeper.ZooKeeperManagerImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabasePoolManager implements Watcher {

    private static final String ROOT_ZNODE = "/widebox";
    private static final String DATABASE_ZNODE = ROOT_ZNODE + "/database";
    private static final String DATABASE_ZNODE_DIR = ROOT_ZNODE + "/database/";
    private ZooKeeperManager zkmanager = ZooKeeperManagerImpl.getInstace();
    private String myZnode;
    private String backupZnode;

    DatabasePoolManager() {
        try {
            initialize();
        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    public String getBackupZnode() {
        return backupZnode;
    }

    public String getMyZnode() {
        return myZnode;
    }

    public String getBackupZnodePath() {
        return DATABASE_ZNODE_DIR + backupZnode;
    }

    public String getMyZnodePath() {
        return DATABASE_ZNODE_DIR + myZnode;
    }

    /**
     * Inicializa a árvore de znodes relativa as bases de dados e cria o seu proprio znode
     */
    private void initialize() throws IOException, KeeperException, InterruptedException {
        DatabaseProperties dbp = new DatabaseProperties();
        InstanceManager instanceManager = InstanceManager.getInstance();
        int numberOfDatabases = instanceManager.getServers(InstanceType.DATABASE).size();
        int numberOfTheaters = (dbp.getNumberOfTheaters() / numberOfDatabases);
        createRoot();
        for(int i = 1; i <= numberOfDatabases; i++) {
            myZnode = String.valueOf((numberOfTheaters * i) - 1);
            String fullZnodeToCreate = DATABASE_ZNODE_DIR + myZnode;
            if (!zkmanager.exists(fullZnodeToCreate, null)) {
                zkmanager.createEphemeral(fullZnodeToCreate, Utilities.getOwnIp().getBytes());
                Debugger.log("Criei o meu znode " + fullZnodeToCreate);
                computeZnodeBackup(createWatchAndFetchChildrens());
                break;
            }
        }
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
     * Faz um watch a arvore DATABASE_ZNODE -> /widebox/database e devolve os znodes dessa arvore
     */
    private List<String> createWatchAndFetchChildrens() throws KeeperException, InterruptedException {
        return zkmanager.getChildren(DATABASE_ZNODE, this);
    }

    /**
     * Vai calcular o znode que vai servir de backup / secundario
     */
    private void computeZnodeBackup(List<String> databaseZnodes) throws KeeperException, InterruptedException {
        try {
            List<Integer> sortedDatabaseZnodes = convertToIntegerAndSort(databaseZnodes);
            int myZnodeIndex = sortedDatabaseZnodes.indexOf(Integer.parseInt(myZnode));
            int backupZnodeIndex = computeIndexOfZnodeBackup(sortedDatabaseZnodes, myZnodeIndex);
            backupZnode = databaseZnodes.get(backupZnodeIndex);
        } catch (NoBackupZnodeException e) {
            Debugger.log(e.getMessage());
        }
    }

    /**
     * Devolve o indice do znode de backup / secundario
     */
    private int computeIndexOfZnodeBackup(List<Integer> databaseZnodes, int myZnodeIndex) throws NoBackupZnodeException {
        if(databaseZnodes.size() > 1) {
            int lastZnodeIndex = databaseZnodes.size() - 1;
            int nextZnodeIndex = myZnodeIndex + 1;
            return nextZnodeIndex <= lastZnodeIndex ? nextZnodeIndex : 0;
        } else {
            throw new NoBackupZnodeException("There is only on znode on /widebox/database");
        }

    }

    private List<Integer> convertToIntegerAndSort(List<String> databaseZnodes) {
        List<Integer> sortedList = new ArrayList<>();
        for(String znode : databaseZnodes) sortedList.add(Integer.valueOf(znode));
        Collections.sort(sortedList);
        return sortedList;
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
            List<String> znodes = createWatchAndFetchChildrens();
            printZnodes(znodes);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
