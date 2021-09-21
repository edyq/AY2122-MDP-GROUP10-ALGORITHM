package utils;

import java.net.Socket;
import java.io.*;
import java.net.UnknownHostException;

/**
 * This class manages the network connection between the laptop that runs the algo (this java project)
 * and the RPI which will transfer the commands to the STM controller
 */

// should 0902 mean: 90 degrees to the left?

public class CommMgr {
    private static CommMgr commMgr = null;
    private static Socket conn = null;

    private BufferedWriter writer;
    private BufferedReader reader;

    private CommMgr() {
    }

    public static CommMgr getCommMgr() {
        if (commMgr == null) {
            commMgr = new CommMgr();
        }
        return commMgr;
    }

    public static void main(String[] args) {
        System.out.println("\nTesting...");
        CommMgr commMgr = new CommMgr();
        commMgr.openConnection();
        commMgr.sendInst("090", CommConstants.INSTRUCTION_TYPE.FORWARD_LEFT);
    }

    public void openConnection() {
        System.out.println("\nOpening connection...");
        try {
            conn = new Socket(CommConstants.RPI_IP, CommConstants.RPI_PORT);

            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(conn.getOutputStream())));
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            System.out.println("openConnection() --> " + "Connection established!!!");
            return;
        } catch (UnknownHostException e) {
            System.out.println("openConnection() --> UnknownHostException");
        } catch (IOException e) {
            System.out.println("openConnection() --> IOException");
        } catch (Exception e) {
            System.out.println("openConnection() --> Exception");
            System.out.println(e.toString());
        }
        System.out.println("Failed to establish connection!!!");
    }

    public void closeConnection() {
        System.out.println("\nClosing connection...");
        try {
            reader.close();
            if (conn != null) {
                conn.close();
                conn = null;
            }
            System.out.println("Connection closed!!!");
        } catch (IOException e) {
            System.out.println("closeConnection() --> IOException");
        } catch (NullPointerException e) {
            System.out.println("closeConnection() --> NullPointerException");
        } catch (Exception e) {
            System.out.println("closeConnection() --> Exception");
            System.out.println(e.toString());
        }
    }

    public void sendInst(String value, CommConstants.INSTRUCTION_TYPE instType) {
        System.out.println("\nSending an instruction...");
        try {
            String outputInst = value + CommConstants.INSTRUCTION_TYPE.encode(instType);
            System.out.println("Instruction sent: " + outputInst);
            writer.write(outputInst);
            writer.flush();
        } catch (IOException e) {
            System.out.println("sendInst() --> IOException");
        } catch (Exception e) {
            System.out.println("sendInst() --> Exception");
            System.out.println(e.toString());
        }
    }

    public String recvMsg() {
        System.out.println("\nReceiving a message...");

        try {
            StringBuilder sb = new StringBuilder();
            String input = reader.readLine();

            if (input != null && input.length() > 0) {
                sb.append(input);
                System.out.println("Message received: " + sb.toString());
                return sb.toString();
            }
        } catch (IOException e) {
            System.out.println("recvMsg() --> IOException");
        } catch (Exception e) {
            System.out.println("recvMsg() --> Exception");
            System.out.println(e.toString());
        }
        return null;
    }

    public boolean isConnected() {
        return conn.isConnected();
    }
}
