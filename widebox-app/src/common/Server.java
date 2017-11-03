package common;

public class Server {
	
	private String ip;
	private int port;
	
	
	public Server(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	
	public String getIp() {
		return ip;
	}
	
	
	public int getPort() {
		return port;
	}
	
}
