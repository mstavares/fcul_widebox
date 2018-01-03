package database;

import common.Debugger;
import common.Seat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static common.Utilities.getFileSeparator;
import static database.FileManager.OperationType.ACCEPT_ACTION;

class FileManager {
	
	/** Folder name */
	private static final String DATABASE_FOLDER = "database";
	
    /** Log file name */
    private static final String LOG_FILE_NAME = "database.log";

    /** Log file path */
    private static final String LOG_PATH = DATABASE_FOLDER + getFileSeparator() + LOG_FILE_NAME;

    /** String used to separate data on logs */
    private static final String SEP_STR = " ";
    
    
	/** Data file name */
	private static final String DATABASE_FILE_NAME = "database.dat";

	/** Data file path */
	private static final String DATABASE_PATH = DATABASE_FOLDER + getFileSeparator() + DATABASE_FILE_NAME;

	/** Backup file name */
	private static final String DATABASE_BAK_FILE_NAME = "database.bak";
	
	/**Backup file path */
	private static final String DATABASE_BAK_PATH = DATABASE_FOLDER + getFileSeparator() + DATABASE_BAK_FILE_NAME;

    /** enum with operation types */
    public enum OperationType {ACCEPT_ACTION}
    
	private DatabaseProperties properties;

    FileManager(DatabaseProperties properties){
        this.properties = properties;
    }
    
    
    /**
     * Writes the database structure to disk in a safe way.
     * WARNING: This deletes the log file without applying it on the structure.
     */
	public void writeDatabaseToFile(Map<Integer, Seat[][]> database) throws IOException {
		Debugger.log("Started writing map to file");
		File datFile = new File(DATABASE_PATH);
		File bakFile = new File(DATABASE_BAK_PATH);
		File logFile = new File(LOG_PATH);
		
		if (bakFile.exists()){
			bakFile.delete();
			Debugger.log("Deleted database.bak file.");
		}
		
		Debugger.log("Started writing database.bak file.");
		FileOutputStream fos = new FileOutputStream(bakFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(database);
		oos.flush();
		oos.close();
		fos.close();
		Debugger.log("Finished writing database.bak file.");
		
		if (logFile.exists()){
			logFile.delete();
			Debugger.log("Deleted database.log file.");
		}
		
		if (datFile.exists()){
			datFile.delete();
			Debugger.log("Deleted database.dat file.");
		}
		
		if (bakFile.renameTo(datFile) ){
			Debugger.log("Renamed database.bak to database.dat");
		}else{
			Debugger.log("Error renaming database.bak to database.dat");
		}
		
		Debugger.log("Finished writing map to file");
	}
	
	
    private void writeToLog(String log) throws IOException {
    	FileWriter writer = new FileWriter(LOG_PATH, true);
        writer.write(log + "\n");
        writer.flush();
        writer.close();
    }
    
    
    private void processLine(Map<Integer, Seat[][]> database, String[] parsedData) {
        OperationType op = OperationType.valueOf(parsedData[0]);
        int theaterId = Integer.parseInt(parsedData[1]);
        int clientId = Integer.parseInt(parsedData[2]);
        int row = Integer.parseInt(parsedData[3]);
        int column = Integer.parseInt(parsedData[4]);
        Seat seat = database.get(theaterId)[row][column];
        if (op == ACCEPT_ACTION)
        	seat.setOccupied(clientId);
    }
    
    
    public void appendAcceptActionToLog(int theaterId, int clientId, int row, int column) throws IOException {
    	writeToLog(ACCEPT_ACTION + SEP_STR + theaterId + SEP_STR + clientId + SEP_STR + row + SEP_STR + column);
    }
    
    
    /**
     * Updates the database structure with the log entries.
     * The update structure is automatically saved to disk in a safe way.
     */
    private void updateDatabaseFromLog(Map<Integer, Seat[][]> database) throws IOException {
    	Debugger.log("Started updating datase with log");
    	
    	FileReader fileReader = new FileReader(LOG_PATH);
        BufferedReader bufRead = new BufferedReader(fileReader);
        String line = null;
        while ((line = bufRead.readLine()) != null) {
            String[] parsedData = line.split(SEP_STR);
            processLine(database, parsedData);
        }
        bufRead.close();
        
        Debugger.log("Finished updating database with log");
        writeDatabaseToFile(database);
    }

    
	private void existsDatabaseFolder() {
		File folder = new File(DATABASE_FOLDER);
		if (!folder.exists()){
			Debugger.log("Creating the database folder");
			new File(DATABASE_FOLDER).mkdir();
		}
	}

	public Map<Integer, Seat[][]> createEmptyDatabase() {
		Map<Integer, Seat[][]> database = new HashMap<Integer, Seat[][]>();
		Debugger.log("Started creating empty database.");
		for(int theater = 0; theater < properties.getNumberOfTheaters(); theater++) {
			database.put(theater, new Seat[properties.getRowsPerTheater()][properties.getColumnsPerTheater()]);
			for(int row = 0; row < properties.getRowsPerTheater(); row++) {
				for(int column = 0; column < properties.getColumnsPerTheater(); column++) {
					database.get(theater)[row][column] = new Seat();
				}
			}
		}
		Debugger.log("Created database sucessfully.");
		return database;
	}

	public Map<Integer, Seat[][]> restoreDatabase() throws IOException {
		Debugger.log("Restoring database from files");
		
		existsDatabaseFolder();
		
		Map<Integer, Seat[][]> database;
		File datFile = new File(DATABASE_PATH);
		File bakFile = new File(DATABASE_BAK_PATH);
		File logFile = new File(LOG_PATH);

		
		if ( datFile.exists() ){
			if ( logFile.exists() ){
				if ( bakFile.exists() ){
					//dat + log + bak
					database = restoreDat();
					updateDatabaseFromLog(database);
				}else{
					//dat + log
					database = restoreDat();
					updateDatabaseFromLog(database);
				}
			}else{
				if ( bakFile.exists() ){
					//dat + bak
					datFile.delete();
					bakFile.renameTo(datFile);
					database = restoreDat();
				}else{
					//dat
					database = restoreDat();
				}
			}
		}else{
			if ( logFile.exists() ){
				if ( bakFile.exists() ){
					//log + bak
					database = createEmptyDatabase();
					updateDatabaseFromLog(database);
				}else{
					//log
					database = createEmptyDatabase();
					updateDatabaseFromLog(database);
				}
			}else{
				if ( bakFile.exists() ){
					//bak
					bakFile.renameTo(datFile);
					database = restoreDat(); //TODO problems when it starts? create empty log?
				}else{
					//nothing
					database = createEmptyDatabase();
					writeDatabaseToFile(database);
					
				}
			}
		}
		

		database = createEmptyDatabase();
		Debugger.log("Restored Database from files sucessfully.");
		return database;
	}


	@SuppressWarnings("unchecked")
	private Map<Integer, Seat[][]> restoreDat() throws IOException {
		Debugger.log("Restoring .dat file");
		Map<Integer, Seat[][]> database = new HashMap<Integer, Seat[][]>();
		File databaseFile = new File(DATABASE_PATH);
		FileInputStream fis = new FileInputStream(databaseFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		try {
			database = (HashMap<Integer, Seat[][]>) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IOException("Error reading database file.");
		}finally{
			ois.close();
			fis.close();
		}
		Debugger.log("Finished restoring .dat file");
		return database;
	}
}
