package map;

import robot.Robot;
import robot.RobotConstants;

import javax.swing.*;
import java.awt.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the arena in which the car will run
 */
public class Arena extends JPanel {
    //private static Map<Integer, PictureObstacle> obstacles;
    private static ArrayList<PictureObstacle> obstacles;
    //private static Node[][][] grid;
    private static Robot bot;
    //private static int numberOfObstacles;

    public Arena(Robot bot) {
        this.bot = bot;
        System.out.printf("Bot at %d, %d\n", bot.getX(), bot.getY());
        //obstacles = new HashMap<Integer, PictureObstacle>();
        obstacles = new ArrayList<>();
        //numberOfObstacles = 0;
    }

    public boolean addPictureObstacle(int x, int y, MapConstants.IMAGE_DIRECTION imageDirection) {
        int numGrids = MapConstants.ARENA_WIDTH/MapConstants.OBSTACLE_WIDTH;
        PictureObstacle obstacle = new PictureObstacle(x, y, imageDirection);
        if (x<0 || x>=numGrids || y<0 || y>=numGrids) {
            System.out.println("Position is out of bounds");
            return false;
        }
        if (overlapWithCar(obstacle)) {
            System.out.printf("Cannot add obstacle centered at <%d, %d> due to overlap with car\n", x, y);
            return false;
        }
        //for (PictureObstacle i : obstacles.values()) {
        for (PictureObstacle i : obstacles) {
            if (overlapWithObstacle(i, obstacle)) {
                System.out.printf("Cannot add obstacle centered at <%d, %d> due to overlap with obstacle\n", x, y);
                return false;
            }
        }
        //obstacles.put(numberOfObstacles, obstacle);
        obstacles.add(obstacle);
        System.out.printf("Added obstacle centered at <%d, %d>\n", x, y);
        //numberOfObstacles++;
        return true;

        //return true;
    }

    private boolean overlapWithCar(PictureObstacle obstacle) {
        int minimumGap = (RobotConstants.ROBOT_VIRTUAL_WIDTH - MapConstants.OBSTACLE_WIDTH) / 2 / MapConstants.OBSTACLE_WIDTH;

        return (Math.abs(obstacle.getX() - bot.getX()) < minimumGap+1 && Math.abs(obstacle.getY() - bot.getY()) < minimumGap+1);
    }

    private boolean overlapWithObstacle(PictureObstacle obs1, PictureObstacle obs2) {
        return (obs1.getX() == obs2.getX()) && (obs1.getY() == obs2.getY());
        //return (Math.abs(obs1.getX() - obs2.getX()) < MapConstants.OBSTACLE_WIDTH
        //        || Math.abs(obs1.getY() - obs2.getY()) < MapConstants.OBSTACLE_WIDTH);
    }

    //public Map<Integer, PictureObstacle> getObstacles() {
    //    return obstacles;
    //}


    public static ArrayList<PictureObstacle> getObstacles() {
        return obstacles;
    }

    public Robot getRobot() {
        return bot;
    }
}
