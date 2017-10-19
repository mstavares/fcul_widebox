package database;

import java.util.Map;

public class FileManager {
	
	public FileManager(){
		
	}
	
	
	public Map<Integer,Boolean[][]> restoreDatabaseFromFile(){
		
	}
	
	
	public Map<Integer,Boolean[][]> updateDatabaseFromLog(Map<Integer,Boolean[][]> database){
		
	}
	
	
	public boolean writeDatabaseToFile(Map<Integer,Boolean[][]> database){
		
	}
	
	
	/**
	 *  Adds a operation to the log
	 * @param opcode The opcode of the action
	 * @param clientId The client id
	 * @return True if the operation was added to the log
	 */
	public boolean log(int opcode, int clientId){
		
	}
	
}
