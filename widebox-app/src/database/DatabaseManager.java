package database;

import common.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static common.Utilities.getFileSeparator;

public class DatabaseManager implements TimeoutListener.Timeout {

	/** Database file name */
	private static final String DATABASE_FILE_NAME = "database.dat";

	/** Database folder name */
	private static final String DATABASE_FOLDER = "widebox-app" + getFileSeparator() + "database";

	/** Database file path */
	private static final String DATABASE_PATH = DATABASE_FOLDER + getFileSeparator() + DATABASE_FILE_NAME;

	/** HashMap which contains all theaters available */
	private Map<Integer, Seat[][]> database = new HashMap<>();

	/** HashMap which does the match between the theater name and the theater id */
	private Map<String, Integer> theaters = new HashMap<>();

	/** Database server properties object */
	private DatabaseProperties properties;

	/** This timeout is used to store the database in file */
	private TimeoutManager timeoutManager;

	/** This object is used to save requests in memory and also in file */
	private Log log;

	DatabaseManager() throws IOException, ClassNotFoundException {
		properties = new DatabaseProperties();
		if(!isDatabaseCreated()) {
			Debugger.log("Did not find the database");
			createDatabaseFolder();
			createDatabase();
			writeDatabaseToFile();
		} else {
			Debugger.log("Database found");
			restoreDatabaseFromFile();
		}
		Debugger.log("Database operation finished");
		loadTheaters();
		log = new Log();
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

	private void createDatabaseFolder() {
		Debugger.log("Creating the database folder");
		new File(DATABASE_FOLDER).mkdir();
	}

	private void createDatabase() {
		Debugger.log("Start creating database");
		for(int theater = 0; theater < properties.getNumberOfTheaters(); theater++) {
			database.put(theater, new Seat[properties.getRowsPerTheater()][properties.getColumnsPerTheater()]);
			for(int row = 0; row < properties.getRowsPerTheater(); row++) {
				for(int column = 0; column < properties.getColumnsPerTheater(); column++) {
					database.get(theater)[row][column] = new Seat();
				}
			}
		}
		Debugger.log("Created Database sucessfully");
	}

	private void restoreDatabaseFromFile() throws IOException, ClassNotFoundException {
		Debugger.log("Restoring database from file");
		File databaseFile = new File(DATABASE_PATH);
		FileInputStream fis = new FileInputStream(databaseFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		database = (HashMap<Integer, Seat[][]>) ois.readObject();
		ois.close();
		fis.close();
		Debugger.log("Restored Database from file sucessfully");
	}

	private Map<Integer,Boolean[][]> updateDatabaseFromLog(HashMap<Integer, Theater> database) {
		//TODO
		return null;
	}

	private void writeDatabaseToFile() throws IOException {
		Debugger.log("Start writing map to file");
		File databaseFile = new File(DATABASE_PATH);
		FileOutputStream fos = new FileOutputStream(databaseFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(database);
		oos.flush();
		oos.close();
		fos.close();
		Debugger.log("Finished writing map to file");
	}

	Map<String, Integer> getTheaters() {
		Debugger.log("Returning all theaters");
		return theaters;
	}

	Seat[][] getTheaterInfo(int theaterId) {
		Debugger.log("Returning Theater " + theaterId);
		return database.get(theaterId);
	}

	boolean reserveSeat(int theaterId, int clientId, int row, int column) {
		Debugger.log("Reserving seat row: "+ row + " and column: " + column + " for clientId " + clientId);
		Seat seat = database.get(theaterId)[row][column];
		if(seat.isFree()) {
			seat.reserveSeat(clientId);
			log.appendReserveAction(theaterId, clientId, row, column);
			return true;
		}
		return false;
	}

	boolean acceptReservedSeat(int theaterId, int clientId, int row, int column) {
		Debugger.log("Accepting reservation seat row: "+ row + " and column: " + column + " for clientId " + clientId);
		Seat seat = database.get(theaterId)[row][column];
		if(seat.isReserved() && seat.getClientId() == clientId) {
			seat.setOccupied();
			log.appendAcceptAction(theaterId, clientId, row, column);
			return true;
		}
		return false;
	}

	boolean cancelReservation(int theaterId, int clientId, int row, int column) {
		Debugger.log("Canceling reservation seat row: "+ row + " and column: " + column + " for clientId " + clientId);
		Seat seat = database.get(theaterId)[row][column];
		if(seat.isReserved() && seat.getClientId() == clientId) {
			seat.freeSeat();
			log.appendCancelAction(theaterId, clientId, row, column);
			return true;
		}
		return false;
	}

	@Override
	public void timeout() {
		/*
		try {
			writeDatabaseToFile();
			Debugger.log("A base de dados foi guardada com sucesso.");
		} catch (IOException e) {
			Debugger.log("Ocorreu um erro ao guardar a BD em ficheiro.");
			e.printStackTrace();
		}
		*/
	}

}
