package algorithms;

import map.Arena;
import map.PictureObstacle;
import robot.Robot;
import robot.RobotConstants;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This class will be used to find the fasted path to visit all obstacles
 */
public class FastestPathAlgo {
    private final Arena arena;


    public FastestPathAlgo(Arena arena) {
        this.arena = arena;
    }

    public int[] planFastestPath() {
        //Map<Integer, PictureObstacle> map = arena.getObstacles();
        ArrayList<PictureObstacle> list = Arena.getObstacles();
        TripPlannerAlgo algo = new TripPlannerAlgo(arena);
        int[] indexArray = IntStream.range(0, list.size()).toArray();
        List<int[]> permutations = getPermutations(indexArray); //getPermutations(Arena.getObstacles().keySet().stream().mapToInt(i -> i).toArray());
        double smallestCost = Double.MAX_VALUE;
        int[] shortestPath = permutations.get(0);
        for (int[] permutation : permutations) {
            //ArrayList<Point> coordinates = new ArrayList<Point>();
            //coordinates.add(RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES);
            //for (int key : permutation) {
            //    coordinates.add(arena.getObstacles().get(key).getCenterCoordinate());
            //}
            //double pathDistance = getPathDistance(coordinates);
            double pathCost = getPathCost(permutation, list, algo);
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
    /*
    private double getPathDistance(ArrayList<Point> coordinates) {
        double pathDistance = 0.0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            pathDistance += getDistance(coordinates.get(i), coordinates.get(i + 1));
        }
        return pathDistance;
    }
     */

    private double getPathCost(int[] path, ArrayList<PictureObstacle> list, TripPlannerAlgo algo) {
        //double pathDistance = 0.0;
        PictureObstacle next;
        Robot bot = arena.getRobot();
        int startX = bot.getX();
        int startY = bot.getY();
        int startAngle = bot.getRobotDirectionAngle();
        double cost;
        algo.constructMap();
        for (int i : path) {
            next = list.get(i);
            algo.planPath(startX, startY, startAngle, next.getX(), next.getY(), next.getImadeDirectionAngle(), RobotConstants.TURN_RADIUS, false, false);
            // do the reverse before finding the next path
            int[] coords = algo.getReverseCoordinates(next);
            //bot.setCenterCoordinate(new Point(coords[0], coords[1]));
            //bot.setDirection(coords[2]);
            startX = coords[0];
            startY = coords[1];
            startAngle = coords[2];
        }
        cost = algo.getTotalCost();
        algo.clearCost();
        bot.setCenterCoordinate(RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES);
        bot.setDirection(RobotConstants.ROBOT_DIRECTION.NORTH);
        return cost;
    }
    /*
    private double getDistance(Point p1, Point p2) {
        return Math.hypot(p1.x - p2.x, p1.y - p2.y);
    }
     */
}
