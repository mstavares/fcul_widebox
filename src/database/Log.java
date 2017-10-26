package database;

import common.Seat;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static database.Log.OperationType.ACCEPT_ACTION;
import static database.Log.OperationType.CANCEL_ACTION;
import static database.Log.OperationType.RESERVE_ACTION;

class Log {

    /** Database file name */
    private static final String LOG_FILE_NAME = "database.log";

    /** Database file path */
    private static final String LOG_PATH = "database/" + LOG_FILE_NAME;

    /** String used to separate data on logs */
    private static final String SEP_STR = " ";

    /** File text format */
    private static final String FILE_FORMAT = "UTF-8";

    /** enum with operation types */
    public enum OperationType {RESERVE_ACTION, ACCEPT_ACTION, CANCEL_ACTION}

    /** Object used to write on database.log file */
    private PrintWriter writer;

    Log() throws IOException {
        initializeLog();
        writer = new PrintWriter(LOG_PATH, FILE_FORMAT);
    }

    private void initializeLog() throws IOException {
        File logFile = new File(LOG_PATH);
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
    }

    private void writeToFile(String log) {
        writer.println(log);
    }

    private void processLine(Map<Integer, Seat[][]> database, String[] parsedData) {
        OperationType op = OperationType.valueOf(parsedData[0]);
        int theaterId = Integer.parseInt(parsedData[1]);
        int clientId = Integer.parseInt(parsedData[2]);
        int row = Integer.parseInt(parsedData[3]);
        int column = Integer.parseInt(parsedData[4]);
        Seat seat = database.get(theaterId)[row][column];
        if(op == RESERVE_ACTION) {
            seat.reserveSeat(clientId);
        } else if (op == ACCEPT_ACTION) {
            if(seat.isReserved() && seat.getClientId() == clientId)
                seat.setOccupied();
        } else {
            if(seat.isReserved())
                seat.freeSeat();
        }
    }

    void appendReserveAction(int theaterId, int clientId, int row, int column) {
        writeToFile(RESERVE_ACTION + SEP_STR + theaterId + SEP_STR + clientId + SEP_STR + row + SEP_STR + column);
    }

    void appendAcceptAction(int theaterId, int clientId, int row, int column) {
        writeToFile(ACCEPT_ACTION + SEP_STR + theaterId + SEP_STR + clientId + SEP_STR + row + SEP_STR + column);
    }

    void appendCancelAction(int theaterId, int clientId, int row, int column) {
        writeToFile(CANCEL_ACTION + SEP_STR + theaterId + SEP_STR + clientId + SEP_STR + row + SEP_STR + column);
    }

    void readLog(Map<Integer, Seat[][]> database) throws IOException {
        FileReader fileReader = new FileReader(LOG_PATH);
        BufferedReader bufRead = new BufferedReader(fileReader);
        String line = null;
        while ((line = bufRead.readLine()) != null) {
            String[] parsedData = line.split(SEP_STR);
            processLine(database, parsedData);
        }
    }

}
