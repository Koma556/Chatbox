package Server;

import Communication.GetProperties;
import org.omg.PortableServer.THREAD_POLICY_ID;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class SavestateDeamon implements Runnable{
    // this class periodically (2s intervals) saves the whole user database to file
    ConcurrentHashMap<String, User> theDB;
    String file;
    int frequency;
    private boolean done = false;

    public SavestateDeamon(ConcurrentHashMap<String, User> userDB){
        this.theDB = userDB;
        try {
            this.file = (GetProperties.getPropertiesFile().getProperty("server.databasePath"));
            this.frequency = Integer.parseInt(GetProperties.getPropertiesFile().getProperty("server.saveFreq"));
        } catch (IOException e) {
            System.out.print("Please create and specify a database file before starting the application.");
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while(!done){
            try
            {
                // TODO: make sure it saves the object again instead of just a reference to it
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(theDB);
                oos.close();
                fos.close();
                System.out.println("Database saved.");
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
