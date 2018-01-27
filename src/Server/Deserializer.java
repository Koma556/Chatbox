package Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Deserializer {

    public static ConcurrentHashMap deserialize(String path)
    {
        ConcurrentHashMap map = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (ConcurrentHashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            System.exit(1);
        }

        // Display content using Iterator
        
        Set set = map.entrySet();
        Iterator iterator = set.iterator();
              while(iterator.hasNext())

        {
            Map.Entry mentry = (Map.Entry) iterator.next();
            System.out.print("key: " + mentry.getKey() + " & Value: ");
            System.out.println(mentry.getValue());
        }


        return map;
    }
}
