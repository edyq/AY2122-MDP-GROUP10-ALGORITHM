package robot;

import map.MapConstants;

import java.awt.*;

public class RobotConstants {
    public static final int ROBOT_VIRTUAL_WIDTH = 30;
    public static final Point ROBOT_INITIAL_CENTER_COORDINATES = new Point(1+ MapConstants.ARENA_BORDER_SIZE, 18+ MapConstants.ARENA_BORDER_SIZE);
    //
    public static final int MOVE_COST = 10;
    public static final int REVERSE_COST = 10;
    public static final int TURN_COST_90 = 60;
    public static final int MAX_COST = Integer.MAX_VALUE;
    //public static final double TURN_RADIUS = 21;
    public static final double LEFT_TURN_RADIUS_Y = 17; //16;
    public static final double LEFT_TURN_RADIUS_X = 28; //30;
    public static final double RIGHT_TURN_RADIUS_Y = 17; //15;
    public static final double RIGHT_TURN_RADIUS_X = 28; //27;
    public static final double MOVE_SPEED = 21; // in cm per second
    public static final double STRAIGHT_LINE_MULTIPLIER = 1.1;

    public enum ROBOT_DIRECTION{
        NORTH, EAST, SOUTH, WEST;

        public static char print(RobotConstants.ROBOT_DIRECTION d) {
            switch (d) {
                case NORTH:
                    return 'N';
                case EAST:
                    return 'E';
                case SOUTH:
                    return 'S';
                case WEST:
                    return 'W';
                default:
                    return 'X';
            }
        }
    }
}
