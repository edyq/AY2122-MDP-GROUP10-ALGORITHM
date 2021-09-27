package robot;

import java.awt.*;

public class RobotConstants {
    public static final int ROBOT_VIRTUAL_WIDTH = 30;
    public static final Point ROBOT_INITIAL_CENTER_COORDINATES = new Point(1, 18);

    public static final int MOVE_COST = 10;
    public static final int TURN_COST_90 = 20;
    public static final int MAX_COST = Integer.MAX_VALUE;
    public static final double TURN_RADIUS = 21;
    public static final double MOVE_SPEED = 21; // in cm per second

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
