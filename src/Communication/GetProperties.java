package Communication;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GetProperties {
    public static Properties getPropertiesFile() throws IOException {

        //to load application's properties, we use this class
        Properties mainProperties = new Properties();

        FileInputStream file;
        String path = "./server.properties";

        //load the file handle for main.properties
        file = new FileInputStream(path);

        //load all the properties from this file
        mainProperties.load(file);

        //we have loaded the properties, so close the file handle
        file.close();

        return mainProperties;
    }
}
