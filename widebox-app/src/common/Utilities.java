package common;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Utilities {
	
	private static int port;
	
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }
    
    
	public static String getOwnIp() {
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> strings = runtimeMxBean.getInputArguments();
		
		String IPADDRESS_PATTERN = 
		        "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		
		Matcher matcher = null;
		for(String ipString : strings) {
			if(ipString.startsWith("-Djava.rmi.server.hostname")) {
				matcher = pattern.matcher(ipString);
				if (matcher.find()) {
				    return matcher.group();
				} else{
				    return "0.0.0.0";
				}
			}
		}
		return "0.0.0.0";
	}
	
	
	public static int getPort() {
		return port;
	}
	
	
	public static void setPort(int porta) {
		port = porta;
	}
}
