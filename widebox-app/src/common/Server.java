package common;

import java.io.*;

public class Server implements Serializable {
	
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

	@Override
	public String toString() {
		return "ip is " + ip + " and port " + port;
	}


	public byte[] getBytes() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(this);
			out.flush();
			out.close();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Server buildObject(byte[] data) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInput in = new ObjectInputStream(bis);
			in.close();
			return (Server) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
