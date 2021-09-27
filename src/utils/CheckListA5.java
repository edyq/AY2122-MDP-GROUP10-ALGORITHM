package utils;

public class CheckListA5 {
    private static CommMgr comm = CommMgr.getCommMgr();

    public static void main(String[] args) {
        comm.connectToRPi();
        comm.sendMsg(":STM:0100"); // move forward 10 cm // NOTE: must change the header to properly match
        String returnVal;
        returnVal = comm.recieveMsg();
        System.out.println(returnVal);
        //while (true) {
        comm.sendMsg(":IMG:scan");
        returnVal = waitForImg();
        //if (!returnVal.equals("bullseye") && !returnVal.equals("null")) //break;
        comm.sendMsg(":STM:0006"); // perform rotation
        returnVal = comm.recieveMsg();
        //}
        System.out.println("Image detected: " + returnVal);
    }

    private static String waitForSTM() {
        String msg;
        while (true) {
            msg = comm.recieveMsg();
            if (msg.equals("A")) break;
        }
        return msg;
    }

    private static String waitForImg() {
        String msg;
        while (true) {
            msg = comm.recieveMsg();
            if (msg != null) break; // change with appropraite header. Or change to just break when something is recieved
        }
        return msg;
    }
}
