package algorithms;

import map.Arena;
import map.PictureObstacle;
import robot.Robot;
import robot.RobotConstants;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This class will be used to find the fasted path to visit all obstacles
 */
public class FastestPathAlgo {
    private Arena arena;


    public FastestPathAlgo(Arena arena) {
        this.arena = arena;
    }

    public int[] planFastestPath() {
        Map<Integer, PictureObstacle> map = arena.getObstacles();
        TripPlannerAlgo algo = new TripPlannerAlgo(arena);
        List<int[]> permutations = getPermutations(arena.getObstacles().keySet().stream().mapToInt(i -> i).toArray());
        double smallestCost = Double.MAX_VALUE;
        int[] shortestPath = permutations.get(0);
        for (int[] permutation : permutations) {
            ArrayList<Point> coordinates = new ArrayList<Point>();
            //coordinates.add(RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES);
            //for (int key : permutation) {
            //    coordinates.add(arena.getObstacles().get(key).getCenterCoordinate());
            //}
            //double pathDistance = getPathDistance(coordinates);
            double pathCost = getPathCost(permutation, map, algo);
            if (pathCost < smallestCost) {
                smallestCost = pathCost;
                shortestPath = permutation;
            }
        }
        System.out.println("Shortest path cost: " + smallestCost);
        return shortestPath;
    }

    private List<int[]> getPermutations(int[] nodes) {
        List<int[]> permutations = new ArrayList<>();
        generateHeapPermutations(nodes, permutations, nodes.length);
        return permutations;
    }

    private void swap(int[] array, int index1, int index2) {
        int temp = array[index1];
        array[index1] = array[index2];
        array[index2] = temp;
    }

    private void generateHeapPermutations(int[] permutation, List<int[]> permutations, int n) {
        if (n <= 0) {
            permutations.add(permutation);
            return;
        }
        int[] tempPermutation = Arrays.copyOf(permutation, permutation.length);
        for (int i = 0; i < n; i++) {
            swap(tempPermutation, i, n - 1);
            generateHeapPermutations(tempPermutation, permutations, n - 1);
            swap(tempPermutation, i, n - 1);
        }
    }

    private double getPathDistance(ArrayList<Point> coordinates) {
        double pathDistance = 0.0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            pathDistance += getDistance(coordinates.get(i), coordinates.get(i + 1));
        }
        return pathDistance;
    }

    private double getPathCost(int[] path, Map<Integer, PictureObstacle> map, TripPlannerAlgo algo) {
        //double pathDistance = 0.0;
        PictureObstacle next;
        Robot bot = arena.getRobot();
        double cost = 0.0;
        for (int i : path) {
            next = map.get(i);
            //System.out.println("---------------Path " + count + "---------------");
            //System.out.println(next.getX() + ", " + next.getY());
            algo.planPath(next.getX(), next.getY(), next.getImadeDirectionAngle(), RobotConstants.TURN_RADIUS, false, false);
            int x = next.getX();
            int y = next.getY();
            switch (next.getImadeDirectionAngle()) { // simulate backing up
                case 0:
                    bot.setCenterCoordinate(new Point(x + 4, y));
                    bot.setDirection(RobotConstants.ROBOT_DIRECTION.WEST);
                    break;
                case 90:
                    bot.setCenterCoordinate(new Point(x, y - 4));
                    bot.setDirection(RobotConstants.ROBOT_DIRECTION.SOUTH);
                    break;
                case 180:
                    bot.setCenterCoordinate(new Point(x - 4, y));
                    bot.setDirection(RobotConstants.ROBOT_DIRECTION.EAST);
                    break;
                case 270:
                    bot.setCenterCoordinate(new Point(x, y + 4));
                    bot.setDirection(RobotConstants.ROBOT_DIRECTION.NORTH);
                    break;
                default:
            }
        }
        cost = algo.getTotalCost();
        algo.clearCost();
        bot.setCenterCoordinate(RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES);
        bot.setDirection(RobotConstants.ROBOT_DIRECTION.NORTH);
        return cost;
    }

    private double getDistance(Point p1, Point p2) {
        return Math.hypot(p1.x - p2.x, p1.y - p2.y);
    }
}
