package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static Server.Core.done;

public class ServerAcceptThread implements Runnable {
    private int port;
    private ServerSocket connector;
    private ConcurrentHashMap myDatabase;
    private ExecutorService clientHandlers;

    public ServerAcceptThread(int port, ServerSocket connector, ConcurrentHashMap myDatabase, ExecutorService clientHandlers) {
        this.port = port;
        this.connector = connector;
        this.myDatabase = myDatabase;
        this.clientHandlers = clientHandlers;
    }

    @Override
    public void run() {
        while(!done){
            System.out.println("Accepting a new connection on port "+port+".");
            Socket sock = null;
            try {
                sock = connector.accept();
            } catch (IOException e) {
                System.out.println("Connector socket closed, closing server.");
            }
            if(sock != null) {
                Runnable clientInstance = new ClientInstance(myDatabase, sock, port);
                clientHandlers.execute(clientInstance);
            }
        }
    }
}
