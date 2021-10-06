package utils;

import algorithms.TripPlannerAlgo;
import map.Arena;
import robot.Robot;
import robot.RobotConstants;

public class test {
    public static void main(String[] args) {
        int x = 300;
        Robot bot = new Robot(RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES, RobotConstants.ROBOT_DIRECTION.NORTH, false);
        Arena arena = new Arena(bot);
        TripPlannerAlgo algo = new TripPlannerAlgo(arena);
        int[] start = algo.getEndPosition();
        algo.planPath(7,13,0,6,13,0, false, true,true);
    }
}
