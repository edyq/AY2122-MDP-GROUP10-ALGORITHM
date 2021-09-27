package utils;

import algorithms.ArcMove;
import algorithms.FastestPathAlgo;
import algorithms.MoveType;
import algorithms.TripPlannerAlgo;
import map.Arena;
import map.MapConstants;
import map.PictureObstacle;
import robot.Robot;
import robot.RobotConstants;
import utils.CommConstants.INSTRUCTION_TYPE;

import java.util.ArrayList;

public class PathToCommand {
    static Robot bot = new Robot(RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES, RobotConstants.ROBOT_DIRECTION.NORTH, false);
    static Arena arena = new Arena(bot);
    static CommMgr comm = CommMgr.getCommMgr();

    static FastestPathAlgo fast = new FastestPathAlgo(arena);
    static TripPlannerAlgo algo = new TripPlannerAlgo(arena);

    public static void main(String[] args) {
        arena.addPictureObstacle(18, 18, MapConstants.IMAGE_DIRECTION.NORTH);
        arena.addPictureObstacle(10, 15, MapConstants.IMAGE_DIRECTION.WEST);
        arena.addPictureObstacle(10, 13, MapConstants.IMAGE_DIRECTION.WEST);
        arena.addPictureObstacle(18,11, MapConstants.IMAGE_DIRECTION.SOUTH);
        arena.addPictureObstacle(2, 11, MapConstants.IMAGE_DIRECTION.SOUTH);

        int[] path = fast.planFastestPath();

        comm.connectToRPi();

        doThePath(path);


    }

    private static void doThePath(int[] path) {
        algo.constructMap();
        ArrayList<PictureObstacle> map = arena.getObstacles();
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
            arrayList = algo.planPath(startX, startY, startAngle, next.getX(), next.getY(), next.getImadeDirectionAngle(), RobotConstants.TURN_RADIUS, true, true);
            sendMovesToRobot(arrayList);
            int[] coords = algo.getReverseCoordinates(next);
            //bot.setCenterCoordinate(new Point(coords[0], coords[1]));
            //bot.setDirection(coords[2]);
            startX = coords[0];
            startY = coords[1];
            startAngle = coords[2];
            count++;
        }
    }

    private static void sendMovesToRobot(ArrayList<MoveType> moveList) {
        String formatted;
        INSTRUCTION_TYPE instructionType = null;
        for (MoveType move : moveList) {
            int measure = 0;
            if (move.isLine()) {
                measure = (int) move.getLength();
                formatted = String.format("%03d", measure);
                if (move.isReverse()) {
                    instructionType = INSTRUCTION_TYPE.BACKWARD;
                    comm.sendMsg(formatted + INSTRUCTION_TYPE.encode(instructionType));
                } else {
                    instructionType = INSTRUCTION_TYPE.FORWARD;
                    comm.sendMsg(formatted+INSTRUCTION_TYPE.encode(instructionType));
                }

            } else {
                measure = move.getDirInDegrees();
                formatted = String.format("%03d", measure);
                ArcMove moveConverted = (ArcMove) move;
                if (moveConverted.isTurnLeft()) {
                    instructionType = INSTRUCTION_TYPE.FORWARD_LEFT;
                    comm.sendMsg(formatted+INSTRUCTION_TYPE.encode(instructionType));
                    comm.sendMsg("0008");
                } else {
                    instructionType = INSTRUCTION_TYPE.FORWARD_RIGHT;
                    comm.sendMsg(formatted+INSTRUCTION_TYPE.encode(instructionType));
                    comm.sendMsg("0008");
                }
            }
            comm.recieveMsg();
        }
    }

    private static void moveForwards(int dist) {

    }
}
