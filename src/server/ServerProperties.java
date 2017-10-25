package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerProperties {
	
	/** Properties file name */
    private static final String PROPERTIES_FILE_NAME = "server.properties";

    /** Properties file path */
    private static final String PROPERTIES_FILE_PATH = "config/" + PROPERTIES_FILE_NAME;
    
    /** TimeoutManager key */
    private static final String TIMEOUT_KEY = "timeout";

    /** Properties object which is gonna load the file */
    private Properties properties;

    ServerProperties() throws IOException {
        InputStream is = new FileInputStream(PROPERTIES_FILE_PATH);
        properties = new Properties();
        properties.load(is);
    }
    
    int getTimeoutValue() {
        return Integer.parseInt(properties.getProperty(TIMEOUT_KEY));
    }

}
