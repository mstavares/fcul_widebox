package database;

import common.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager implements TimeoutListener.Timeout {

	/** HashMap which contains all theaters available */
	private Map<Integer, Seat[][]> database = new HashMap<>();

	/** HashMap which does the match between the theater name and the theater id */
	private Map<String, Integer> theaters = new HashMap<>();

	/** Database server properties object */
	private DatabaseProperties properties;

	/** This timeout is used to store the database in file */
	private TimeoutManager timeoutManager;

	/** This object is used to save requests in memory and also in file */
	private FileManager fileManager;

	private DatabaseSynchronizer databaseSynchronizer;

	DatabaseManager() throws IOException, ClassNotFoundException {
		properties = new DatabaseProperties();
		
		loadTheaters();
		
		fileManager = new FileManager(properties);
		database = fileManager.restoreDatabase();

		databaseSynchronizer = new DatabaseSynchronizer();

		timeoutManager = new TimeoutManager(this, properties.getTimeoutValue());
		timeoutManager.runRepeatly();
	}

	private void loadTheaters() {
		for(int i = 0; i < properties.getNumberOfTheaters(); i++) {
			theaters.put("Theater_" + i, i);
		}
	}

	
	Map<String, Integer> getTheaters() {
		Debugger.log("Returning all theaters");
		return theaters;
	}

	Seat[][] getTheaterInfo(int theaterId) {
		Debugger.log("Returning Theater " + theaterId);
		return database.get(theaterId);
	}


	boolean acceptReservedSeat(int theaterId, int clientId, int row, int column) {
		Debugger.log("Accepting reservation seat row: "+ row + " and column: " + column + " for clientId " + clientId);
		Seat seat = database.get(theaterId)[row][column];
		if(seat.isFree()) {
			//TODO stuff de TFD para tornar isto mais eficiente?
			try {
				boolean isReplicated = databaseSynchronizer.sendToBackupServer(theaterId, clientId, row, column);
				if(isReplicated) {
					fileManager.appendAcceptActionToLog(theaterId, clientId, row, column);
					seat.setOccupied(clientId);
					Debugger.log("Seat accepted successfully");
					return true;
				} else {
					Debugger.log("This entry was not replicated");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Debugger.log("This seat is not free.");
		}
		return false;
	}
	

	@Override
	public void timeout() {
		/*/TODO ir fazendo writeDatabaseToFile
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
