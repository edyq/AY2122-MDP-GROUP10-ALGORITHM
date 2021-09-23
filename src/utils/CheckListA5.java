package utils;

public class CheckListA5 {
    private static CommMgr comm = CommMgr.getCommMgr();

    public static void main(String[] args) {
        comm.sendMsg(":A:0100"); // move forward 10 cm // NOTE: must change the header to properly match
        waitForSTM();
        String returnVal;
        while (true) {
            comm.sendMsg("<STRING TO DO SCAN HERE");
            returnVal = waitForImg();
            if (!returnVal.equals("bullseye") && !returnVal.equals("null")) break;
            comm.sendMsg(":A:0006"); // perform rotation
            waitForSTM();
        }
        System.out.println("Image detected: " + returnVal);
    }

    private static boolean waitForSTM() {
        while (true) {
            String msg = comm.recieveMsg();
            if (msg.equals("A")) break;
        }
        return true;
    }

    private static String waitForImg() {
        String msg;
        while (true) {
            msg = comm.recieveMsg();
            if (msg.startsWith(":B:")) break; // change with appropraite header. Or change to just break when something is recieved
        }
        return msg;
    }
}
