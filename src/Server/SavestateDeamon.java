package Server;

import Communication.GetProperties;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class SavestateDeamon implements Runnable{
    // this class periodically (frequency intervals) saves the whole user database to filePath
    private ConcurrentHashMap<String, User> theDB;
    private String filePath;
    private int frequency;
    private boolean done = false;
    private File database;

    public SavestateDeamon(ConcurrentHashMap<String, User> userDB){
        this.theDB = userDB;
        try {
            this.filePath = (GetProperties.getPropertiesFile().getProperty("server.databasePath"));
            this.frequency = Integer.parseInt(GetProperties.getPropertiesFile().getProperty("server.saveFreq"));
        } catch (IOException e) {
            System.out.print("Please create and specify a database file before starting the application.");
            System.exit(1);
        }
        this.database = new File(filePath);
    }

    @Override
    public void run() {
        while(!done){
            try
            {
                // the deamon will delete and then recreate the database file, overwriting everything in it
                database.delete();
                database.getParentFile().mkdirs();
                try {
                    database.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Couldn't create database filePath under "+filePath);
                }
                // now it creates the streams
                FileOutputStream fos = new FileOutputStream(filePath);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                // and finally saves the data inside the object
                oos.writeObject(theDB);
                oos.close();
                fos.close();
                //System.out.println("Database saved.");
            }catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
            try {
                Thread.sleep(frequency);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    // call it when you want to stop the thread
    public void stop(){
        System.out.println("STOPPING THE PRESSES");
        System.out.flush();
        done = true;
    }
}
