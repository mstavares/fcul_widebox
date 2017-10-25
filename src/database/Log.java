package database;

import java.io.File;
import java.io.IOException;

public class Log {

    /** Database file name */
    private static final String LOG_FILE_NAME = "database.log";

    /** Database file path */
    private static final String LOG_PATH = "database/" + LOG_FILE_NAME;

    /** enum with operation types */
    public enum OperationType {RESERVE_ACTION, ACCEPT_ACTION, CANCEL_ACTION}

    Log() throws IOException {
        initializeLog();
    }

    private void initializeLog() throws IOException {
        File logFile = new File(LOG_PATH);
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
    }



    public void appendReserveAction(int theaterId, int clientId, int row, int column) {

    }

    public void appendAcceptAction(int theaterId, int clientId, int row, int column) {

    }

    public void appendCancelAction(int theaterId, int clientId, int row, int column) {

    }
}
