package Client.FileTransfer;

import Client.UI.FileReceiverWindow.FileReceiverCountdown;
import Client.UI.FileReceiverWindow.OpenFRWindow;
import Client.UI.TestUI;
import Communication.Message;
import javafx.application.Platform;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import static Client.UI.Controller.listOfFileReceiverProcesses;

public class FileReceiveInstance implements Runnable {
    private Socket sock;
    private String userName, fileName, fileSize;
    private FileReceiverWrapper wrapper;

    public FileReceiveInstance(Socket sock, String incoming, FileReceiverWrapper wrapper){
        this.sock = sock;
        String[] tmp = incoming.split(":");
        this.userName = tmp[0];
        this.fileName = tmp[1];
        this.fileSize = tmp[2];
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
            // open new stage with file transfer request
            //System.out.println(userName+fileName);
            OpenFRWindow fr = new OpenFRWindow(userName, fileName, fileSize, sock);
            Platform.runLater(fr);
            // request timeout frame
            Thread frcdsp = new Thread(new FileReceiverCountdownSupport(sock.getPort()));
            frcdsp.start();
            while(!listOfFileReceiverProcesses.get(sock.getPort()).isDone()){
                if(listOfFileReceiverProcesses.get(sock.getPort()).isAccepted()) {
                    InputStream in = null;
                    try {
                        in = sock.getInputStream();
                        // Writing the file to disk
                        // Instantiating a new output stream object
                        String filepath = fileName;
                        OutputStream output = new FileOutputStream(filepath);

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                        //System.out.println("File received and saved to "+ filepath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    listOfFileReceiverProcesses.get(sock.getPort()).setDone(true);
                    // update label
                    Thread frsdl = new Thread(new FileReceiverSetDoneLabel(sock.getPort()));
                    Platform.runLater(frsdl);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            Message reply = new Message("OP_ERR", "Busy with another transfer on same port");
            reply.send(sock);
        }
        //System.out.println("Receiver Instance closing.");
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        listOfFileReceiverProcesses.remove(sock.getPort());
    }
}
