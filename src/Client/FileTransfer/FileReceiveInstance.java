package Client.FileTransfer;

import Client.UI.FileReceiverWindow.OpenFRWindow;
import Client.UI.TestUI;
import Communication.Message;
import javafx.application.Platform;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import static Client.UI.Controller.listOfFileReceiverProcesses;

public class FileReceiveInstance implements Runnable {
    private Socket sock;
    private String userName, fileName;
    private FileReceiverWrapper wrapper;

    public FileReceiveInstance(Socket sock, String incoming, FileReceiverWrapper wrapper){
        this.sock = sock;
        String[] tmp = incoming.split(":");
        this.userName = tmp[0];
        this.fileName = tmp[1];
        this.wrapper = wrapper;
    }
    @Override
    public void run() {
        if(listOfFileReceiverProcesses == null){
            listOfFileReceiverProcesses = new ConcurrentHashMap<>();
        }
        if(!listOfFileReceiverProcesses.containsKey(sock.getPort())) {
            listOfFileReceiverProcesses.put(sock.getPort(), wrapper);
            wrapper.setUsername(userName);
            // TODO: File transfer accept request;
            // open new stage with file transfer request
            System.out.println(userName+fileName);
            OpenFRWindow fr = new OpenFRWindow(userName, fileName, sock);
            Platform.runLater(fr);
            // answer yes/no to request and ask where to save
            // close if no
            // show progress if yes?
            // confirm when done
            // close socket
            while(!listOfFileReceiverProcesses.get(sock.getPort()).isDone()){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Message reply = new Message("OP_ERR", "Busy with another transfer on same port");
            reply.send(sock);
        }
        listOfFileReceiverProcesses.remove(sock.getPort());
    }
}
