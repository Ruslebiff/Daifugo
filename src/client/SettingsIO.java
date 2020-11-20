package client;

import java.io.*;
import java.util.Properties;

public class SettingsIO {
    public Properties prop = new Properties();

    public void saveSettings(String serverAddress, String nickName){
        try (OutputStream output = new FileOutputStream("./config.properties")) {

            Properties prop = new Properties();

            // set the properties value
            prop.setProperty("serverAddress", serverAddress);
            prop.setProperty("nickName", nickName);

            // save properties to root folder
            prop.store(output, null);

            System.out.println("Saved settings: " + prop);

        } catch (IOException io) {
            io.printStackTrace();
        }
    }


    public void readSettings(){
        try (InputStream input = new FileInputStream("./config.properties")) {

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            System.out.println("Loaded settings:");
            prop.forEach((key, value) -> System.out.println("Key : " + key + ", Value : " + value));

        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Could not load config file. Default settings will be used.");
        }
    }
}
