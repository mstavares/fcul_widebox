package database;

import common.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static common.Utilities.getFileSeparator;

class DatabaseProperties {

    /** Properties file name */
    private static final String PROPERTIES_FILE_NAME = "server.properties";

    /** Properties file path */
    private static final String PROPERTIES_FILE_PATH = "widebox-app"+ getFileSeparator()
            +"config" + getFileSeparator() + PROPERTIES_FILE_NAME;

    /** Number of theatres key */
    private static final String NUMBER_OF_THEATERS_KEY = "NrTh";

    /** Number of rows per theatre key */
    private static final String NUMBER_OF_ROWS_KEY = "NrRw";

    /** Number of columns per theatre key */
    private static final String NUMBER_OF_COLUMNS_KEY = "NrCl";

    /** TimeoutManager key */
    private static final String TIMEOUT_KEY = "timeout";

    /** Properties object which is gonna load the file */
    private Properties properties;

    DatabaseProperties() throws IOException {
        InputStream is = new FileInputStream(PROPERTIES_FILE_PATH);
        properties = new Properties();
        properties.load(is);
    }

    int getNumberOfTheaters() {
        return Integer.parseInt(properties.getProperty(NUMBER_OF_THEATERS_KEY));
    }

    int getRowsPerTheater() {
        return Integer.parseInt(properties.getProperty(NUMBER_OF_ROWS_KEY));
    }

    int getColumnsPerTheater() {
        return Integer.parseInt(properties.getProperty(NUMBER_OF_COLUMNS_KEY));
    }

    int getTimeoutValue() {
        return Integer.parseInt(properties.getProperty(TIMEOUT_KEY));
    }

}
