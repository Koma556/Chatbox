package Communication;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Message {

    private String operation;
    private String data;
    private StringBuilder incomingJson;
    private String toParse;
    private JSONParser parser;
    private JSONObject jsonObject = new JSONObject();
    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    // if the getters return null the body knows no message was received
    public String getOperation() {
        return operation;
    }

    public String getData() {
        return data;
    }

    // Constructor called when sending a message
    @SuppressWarnings("unchecked")
    public Message(String operation, String data) {
        this.operation = operation;
        jsonObject.put("OP_CODE", operation);
        this.data = data;
        jsonObject.put("DATA", data);
    }

    // Constructor called when receiving one, only instances the object
    public Message(){
    }

    public void setFields(String operation, String data) {
        this.operation = operation;
        jsonObject.put("OP_CODE", operation);
        this.data = data;
        jsonObject.put("DATA", data);
    }

    // Call this method on a populated Message Object
    public void send (Socket server) throws IOException {
        if(operation != null && data != null)
        try {
            if(writer == null)
                writer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
            writer.write(jsonObject.toJSONString());
            // puts a line separator
            writer.newLine();
            writer.flush();
            operation = null;
            data = null;
        } catch (IOException e) {
            throw new IOException();
        }
        else
            // should never enter this branch
            System.out.println("Missing parameters in the send method on the Message object, you might be trying to send a message built for receiving or with a missing INetAddress.");
    }

    private void parseIncomingJson(){
        parser = new JSONParser();
        try {
            jsonObject = (JSONObject) parser.parse(toParse);
            this.operation = (String) jsonObject.get("OP_CODE");
            this.data = (String) jsonObject.get("DATA");
            if(!operation.equals("OP_HEARTBEAT"))
                System.out.println("Received message: " + toParse);
        }catch (Exception e) {e.printStackTrace();}
    }

    // Call the receive method on an empty Message object
    // this method first receives, then parses a new JSON
    public void receive(Socket server) throws SocketTimeoutException{
        try {
            server.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if(!server.isClosed()) {
            try {
                if (reader == null)
                    reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
                // read until line separator only if there's data in input
                if (reader.ready()) {
                    toParse = reader.readLine();
                    this.parseIncomingJson();
                }
            } catch (SocketTimeoutException ea) {
                throw new SocketTimeoutException();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
