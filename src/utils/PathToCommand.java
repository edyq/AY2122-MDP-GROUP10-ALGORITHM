package utils;

import algorithms.*;
import map.Arena;
import map.MapConstants;
import map.Node;
import map.PictureObstacle;
import robot.Robot;
import robot.RobotConstants;
import utils.CommConstants.INSTRUCTION_TYPE;
import java.util.ArrayList;

public class PathToCommand {
    static Robot bot = new Robot(RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES, RobotConstants.ROBOT_DIRECTION.NORTH, false);
    static Arena arena = new Arena(bot);
    static CommMgr comm = CommMgr.getCommMgr();

    static FastestPathAlgoTest fast = new FastestPathAlgoTest(arena);
    //static GreedyFastestPathAlgo greedy = new GreedyFastestPathAlgo(arena);
    static TripPlannerAlgo algo = new TripPlannerAlgo(arena);


    public static void main(String[] args) {

        /*
        // add impossible node
        arena.addPictureObstacle(0,0,MapConstants.IMAGE_DIRECTION.NORTH);

        arena.addPictureObstacle(5 , 0, MapConstants.IMAGE_DIRECTION.SOUTH);
        arena.addPictureObstacle(16, 3, MapConstants.IMAGE_DIRECTION.WEST);
        arena.addPictureObstacle(9, 12, MapConstants.IMAGE_DIRECTION.WEST);
        arena.addPictureObstacle(18, 8, MapConstants.IMAGE_DIRECTION.SOUTH);
        //arena.addPictureObstacle(8, 19, MapConstants.IMAGE_DIRECTION.EAST);
        //arena.addPictureObstacle(19, 15, MapConstants.IMAGE_DIRECTION.WEST);
        //arena.addPictureObstacle(2, 7, MapConstants.IMAGE_DIRECTION.EAST);
        //arena.addPictureObstacle(1, 6,MapConstants.IMAGE_DIRECTION.SOUTH);
        //arena.addPictureObstacle(19, 15, MapConstants.IMAGE_DIRECTION.WEST);
        */

        comm.connectToRPi();

        // receive obstacles from android
        recvObstacles();
        int[] path = fast.planFastestPath();

        doThePath(path);
        System.out.println("No more possible nodes to visit. Pathing finished");
        comm.endConnection();
    }

    private static void doThePath(int[] path) {
        algo.constructMap();
        ArrayList<PictureObstacle> map = Arena.getObstacles();
        Robot r = arena.getRobot();
        int startX = r.getX();
        int startY = r.getY();
        int startAngle = r.getRobotDirectionAngle();
        PictureObstacle next;
        ArrayList<MoveType> arrayList;
        int count = 0;
        for (int i : path) {
            next = map.get(i);
            System.out.println("---------------Path " + count + "---------------");
            System.out.println(next.getX() + ", " + next.getY());
            arrayList = algo.planPath(startX, startY, startAngle, next.getX(), next.getY(), next.getImadeDirectionAngle(),true, true, true);
            if (arrayList != null) {// if there is a path
                sendMovesToRobot(arrayList, i);
                int[] coords = algo.getEndPosition();
                startX = coords[0];
                startY = coords[1];
                startAngle = coords[2];
                count++;
            } else {
                System.out.println("No path found, trying to path to the next obstacle");
            }
        }
    }

    private static void sendMovesToRobot(ArrayList<MoveType> moveList, int i) {
        int tryCount = 4;
        ArrayList<MoveType> backwardMoveList;
        int[] coords;

        int[] backwardCoords;

        String commandsToSend = encodeMoves(moveList);

        sendToRobot(commandsToSend);
        String str = takeImage();
        // retry if image taken is null
        while ((str.equals("null") || str.equals("bulls")) && tryCount > 0) {
            tryCount--;
            // try to go backwards by 1.
            coords = algo.getEndPosition();
            backwardCoords = algo.getReversePos(coords[0], coords[1], coords[2]/90);
            if (backwardCoords == null) break; // if no backwards position available, just break.
            System.out.println("Reversing to retake picture...");
            backwardMoveList = algo.planPath(coords[0], coords[1], coords[2], backwardCoords[0], backwardCoords[1], backwardCoords[2]*90, false,true, true);
            if (backwardMoveList != null) { // if we can't reverse, just break from the loop.
                commandsToSend = encodeMoves(backwardMoveList);
                sendToRobot(commandsToSend);
                str = takeImage();
            } else {
                break;
            }
        }
        sendImageToAndroid(i, str);
    }

    private static String encodeMoves(ArrayList<MoveType> moveList) {
        String commandsToSend = ":STM:0008,";
        INSTRUCTION_TYPE instructionType;
        String formatted;

        for (MoveType move : moveList) {
            int measure = 0;
            if (move.isLine()) {
                measure = (int) move.getLength();//Math.round(move.getLength() * RobotConstants.STRAIGHT_LINE_MULTIPLIER);
                formatted = String.format("%03d", measure);
                if (move.isReverse()) {
                    instructionType = INSTRUCTION_TYPE.BACKWARD;
                } else {
                    instructionType = INSTRUCTION_TYPE.FORWARD;
                }
                //msg = ":STM:" + formatted + INSTRUCTION_TYPE.encode(instructionType);
                commandsToSend += formatted + INSTRUCTION_TYPE.encode(instructionType) + ",";
            } else {
                ArcMove moveConverted = (ArcMove) move;
                if (moveConverted.isTurnLeft()) {
                    instructionType = INSTRUCTION_TYPE.FORWARD_LEFT;
                } else {
                    instructionType = INSTRUCTION_TYPE.FORWARD_RIGHT;
                }
                //msg = ":STM:090" + INSTRUCTION_TYPE.encode(instructionType);
                commandsToSend += "090" + INSTRUCTION_TYPE.encode(instructionType) + ",";
            }
        }
        return commandsToSend.substring(0,commandsToSend.length()-1);
    }

    // send to robot the completed list of paths.
    private static void sendToRobot(String cmd) {
        comm.sendMsg(cmd);
        String receiveMsg = null;
        //LocalDateTime then = LocalDateTime.now();
        //while (receiveMsg == null || !receiveMsg.equals("ACK")) {
        //    receiveMsg = comm.recieveMsg();
        //    if (ChronoUnit.SECONDS.between(then, LocalDateTime.now()) >= 5) {
        //        System.out.println(receiveMsg);
        //        System.out.println("Resending message....");
        //        comm.sendMsg(cmd);
        //        then = LocalDateTime.now();
        //    }
        //}
        //System.out.println(receiveMsg);
        //receiveMsg = null;

        // buffer so we can space commands
        try {
            Thread.sleep(500);//time is in ms (1000 ms = 1 second)
        } catch (InterruptedException e) {e.printStackTrace();}
        sendPathToAndroid();
        while (receiveMsg == null || !receiveMsg.equals("A")) {
            receiveMsg = comm.recieveMsg();
        }

        System.out.println("Message: " + receiveMsg + "\n");
        // TODO: Testing this
        try {
            Thread.sleep(500);//time is in ms (1000 ms = 1 second)
        } catch (InterruptedException e) {e.printStackTrace();}
    }

    private static void sendImageToAndroid(int obstacleID, String image) {
        String msg;
        msg = ":AND:TARGET," + (obstacleID+1) + "," + image;
        comm.sendMsg(msg);
        try { // try to wait
            Thread.sleep(500);//time is in ms (1000 ms = 1 second)
        } catch (InterruptedException e) {e.printStackTrace();}
    }

    private static String takeImage() {
        comm.sendMsg(":IMG:scan");
        System.out.println("Scanning...");
        String receiveMsg = null;
        while (receiveMsg == null || receiveMsg.isEmpty()) {
            receiveMsg = comm.recieveMsg();
        }
        System.out.println("Message: " + receiveMsg + "\n");
        return receiveMsg;
    }

    private static void sendPathToAndroid() {
        ArrayList<Node> path = algo.getNodePath();
        String pathString = ":AND:PATH,";
        //PATH|x,y,0-270|
        for (Node n : path) {
            pathString += "|" + (n.getX()-MapConstants.ARENA_BORDER_SIZE) + "," + (n.getY()-MapConstants.ARENA_BORDER_SIZE) + "," + n.getDim()*90;
        }
        comm.sendMsg(pathString);
    }

    private static void recvObstacles() {
        String receiveMsg = null;
        System.out.println("Waiting to receive obstacle list...");
        while (receiveMsg == null || !receiveMsg.startsWith("POS")) {
            receiveMsg = comm.recieveMsg();
        }
        System.out.println("Received Obstacles String: " + receiveMsg + "\n");

        // "POS|3,4,N|4,5,E|5,6,S|9,4,N|9,10,E"
        String[] positions = receiveMsg.split("\\|");

        for (int i = 1; i < positions.length; i++) {
            String[] obs = positions[i].split(",");
            arena.addPictureObstacle(Integer.parseInt(obs[0]), Integer.parseInt(obs[1]), MapConstants.IMAGE_DIRECTION.getImageDirection(obs[2]));
        }
    }
}
