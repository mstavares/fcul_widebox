package common;

public enum InstanceType {
	APP("App"), DATABASE("Database");
	
	private String fileName;
	
	private InstanceType(String fileName){
		this.fileName = fileName;
	}
	
	public String getFileName(){
		return fileName;
	}
	
}
