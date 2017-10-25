package database;

import common.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager implements TimeoutListener.Timeout {

	/** Database file name */
	private static final String DATABASE_FILE_NAME = "database.dat";

	/** Database file path */
	private static final String DATABASE_PATH = "database/" + DATABASE_FILE_NAME;

	/** HashMap which contains all theaters available */
	private Map<Integer, Seat[][]> database = new HashMap<>();

	/** HashMap which does the match between the theater name and the theater id */
	private Map<String, Integer> theaters = new HashMap<>();

	/** Database server properties object */
	private DatabaseProperties properties;

	private TimeoutManager timeoutManager;

	DatabaseManager() throws IOException, ClassNotFoundException {
		if(!isDatabaseCreated()) {
			properties = new DatabaseProperties();
			createDatabase();
		} else {
			restoreDatabaseFromFile();
		}
		loadTheaters();
		timeoutManager = new TimeoutManager(this, properties.getTimeoutValue());
		timeoutManager.runRepeatly();
	}

	private void loadTheaters() {
		for(int i = 0; i < properties.getNumberOfTheaters(); i++) {
			theaters.put("Theater_" + i, i);
		}
	}

	private boolean isDatabaseCreated() {
		return new File(DATABASE_PATH).exists();
	}

	private void createDatabase() {
		for(int theater = 0; theater < properties.getNumberOfTheaters(); theater++) {
			database.put(theater, new Seat[properties.getRowsPerTheater()][properties.getColumnsPerTheater()]);
		}
	}

	private void restoreDatabaseFromFile() throws IOException, ClassNotFoundException {
		File databaseFile = new File(DATABASE_PATH);
		FileInputStream fis = new FileInputStream(databaseFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		database = (HashMap<Integer, Seat[][]>) ois.readObject();
		ois.close();
		fis.close();
	}

	private Map<Integer,Boolean[][]> updateDatabaseFromLog(HashMap<Integer, Theater> database) {
		//TODO
		return null;
	}

	private void writeDatabaseToFile() throws IOException {
		File databaseFile = new File(DATABASE_PATH);
		FileOutputStream fos = new FileOutputStream(databaseFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(database);
		oos.flush();
		oos.close();
		fos.close();
	}

	Map<String, Integer> getTheaters() {
		return theaters;
	}

	Seat[][] getTheaterInfo(int theaterId) {
		return database.get(theaterId);
	}

	boolean reserveSeat(int theaterId, int clientId, int row, int column) {
		Seat seat = database.get(theaterId)[row][column];
		if(seat.isFree()) {
			seat.reserveSeat(clientId);
			return true;
		}
		return false;
	}

	boolean acceptReservedSeat(int theaterId, int clientId, int row, int column) {
		Seat seat = database.get(theaterId)[row][column];
		if(seat.isReserved() && seat.getClientId() == clientId) {
			seat.setOccupied();
			return true;
		}
		return false;
	}

	boolean cancelReservation(int theaterId, int clientId, int row, int column) {
		Seat seat = database.get(theaterId)[row][column];
		if(seat.isReserved() && seat.getClientId() == clientId) {
			seat.freeSeat();
			return true;
		}
		return false;
	}

	/**
	 * Adds a operation to the log
	 * @param opcode The opcode of the action
	 * @param clientId The client id
	 * @return True if the operation was added to the log
	 */
	public boolean log(int opcode, int clientId){
		//TODO
		return false;
	}

	@Override
	public void timeout() {
		try {
			writeDatabaseToFile();
		} catch (IOException e) {
			Debugger.log("Ocorreu um erro ao guardar a BD em ficheiro.");
		}
	}

}
