package utils;

/**
 * This class manages the network connection between the laptop that runs the algo (this java project)
 * and the RPI which will transfer the commands to the STM controller
 */
public class CommMgr {
    public static final String forward = "0000";
    public static final String backward = "0001";
    public static final String forwardLeft = "0902";
    public static final String forwardRight = "0903";
}
