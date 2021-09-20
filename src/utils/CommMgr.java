package utils;

import java.io.*;
import java.net.Socket;

/**
 * This class manages the network connection between the laptop that runs the algo (this java project)
 * and the RPI which will transfer the commands to the STM controller
 */
public class CommMgr {
    private static Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;

    private static CommMgr instance;

    private CommMgr() {}

    // lazy initialization
    public static CommMgr getCommMgr() {
        if (instance == null) {
            instance = new CommMgr();
        }
        return instance;
    }

    public boolean connectToRPi() {
        try {
            socket = new Socket(CommConstants.HOST_ADDRESS, CommConstants.PORT);
            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream())));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connection established with RPi");
            return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean endConnection() {
        try {
            socket.close();
            writer.close();
            reader.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMsg(String msg) {
        try {
            System.out.println("Sending message: " + msg);
            writer.write(msg);
            writer.flush();
            System.out.println("Message sent");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            // retry send
            boolean result;
            while (true) {
                result = instance.connectToRPi();
                if (result) break;
            }
            return instance.sendMsg(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String recieveMsg() {
        try {
            String msg = reader.readLine();
            return msg;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
