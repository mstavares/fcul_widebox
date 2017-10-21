package database;

import java.util.Map;

public class FileManager {
	
	public FileManager(){
		
	}
	
	
	public Map<Integer,Boolean[][]> restoreDatabaseFromFile(){
		//TODO
		return null;
	}
	
	
	public Map<Integer,Boolean[][]> updateDatabaseFromLog(Map<Integer,Boolean[][]> database){
		//TODO
		return null;
	}
	
	
	public boolean writeDatabaseToFile(Map<Integer,Boolean[][]> database){
		//TODO
		return false;
	}
	
	
	/**
	 *  Adds a operation to the log
	 * @param opcode The opcode of the action
	 * @param clientId The client id
	 * @return True if the operation was added to the log
	 */
	public boolean log(int opcode, int clientId){
		//TODO
		return false;
	}
	
}
