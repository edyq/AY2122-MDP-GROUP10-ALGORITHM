package algorithms;

import map.*;
import robot.Robot;
import robot.RobotConstants;

import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

/**
 * This class will be used for planning the maneuver from one point to another
 */
public class TripPlannerAlgo {
    private final Arena arena;
    private final int numCells = MapConstants.ARENA_WIDTH / MapConstants.OBSTACLE_WIDTH;
    private Node[][][] grid; // grid dimensions: x,y,direction (of which there are 4: 0=east,1=north,2=west,3=south)
    private int[][][] turningArray; // array to keep track of whether turns are possible at this position
    private final Map<Node, Node> predMap;

    private PriorityQueue<Node> visitQueue; // min heap priority queue for nodes in the frontier
    private Node currentNode;
    private double totalCost = 0;

    public TripPlannerAlgo(Arena arena) {
        this.arena = arena;
        this.predMap = new HashMap<>();
        /*
        constructMap();

        int robotX = arena.getRobot().getX();
        int robotY = arena.getRobot().getY();
        int robotDirection = arena.getRobot().getRobotDirectionAngle();
        int angleDimension = angleToDimension(robotDirection);

        this.currentNode = grid[robotY][robotX][angleDimension];

        // initialize the arrays
        int numCells = MapConstants.ARENA_WIDTH / MapConstants.OBSTACLE_WIDTH;
        //this.greedyCostArray = new double[numCells][numCells][4];
        this.turningArray = new int[numCells][numCells][4];
        for (int i = 0; i < numCells; i++) {
            for (int j = 0; j < numCells; j++) {
                for (int k = 0; k < 4; k++) {
                    if (canVisit(grid[j][i][k])) {
                        //greedyCostArray[j][i][k] = 0; // if visitable, set the initial cost to 0.
                        grid[j][i][k].setCost(0, 0);
                    } else {
                        //greedyCostArray[j][i][k] = RobotConstants.MAX_COST; // otherwise, infinite cost.
                        grid[j][i][k].setCost(RobotConstants.MAX_COST, RobotConstants.MAX_COST);
                    }
                }
            }
        }

        // initialize frontier queue
        this.visitQueue = new PriorityQueue<>(new NodeComparator());
        this.visitQueue.add(currentNode);
        //greedyCostArray[robotY][robotX][angleDimension] = 0;
        grid[robotY][robotX][angleDimension].setCost(0, 0);
         */
    }

    private void clear(int startX, int startY, int startAngle) {
        predMap.clear();
        constructMap();

        //int robotX = arena.getRobot().getX();
        //int robotY = arena.getRobot().getY();
        //int robotDirection = arena.getRobot().getRobotDirectionAngle();
        int angleDimension = angleToDimension(startAngle);

        this.currentNode = grid[startY][startX][angleDimension];

        // initialize the arrays
        int numCells = MapConstants.ARENA_WIDTH / MapConstants.OBSTACLE_WIDTH;
        //this.greedyCostArray = new double[numCells][numCells][4];
        for (int i = 0; i < numCells; i++) {
            for (int j = 0; j < numCells; j++) {
                for (int k = 0; k < 4; k++) {
                    if (canVisit(grid[j][i][k])) {
                        //greedyCostArray[j][i][k] = 0; // if visitable, set the initial cost to 0.
                        grid[j][i][k].setCost(0, 0);
                    } else {
                        //greedyCostArray[j][i][k] = RobotConstants.MAX_COST; // otherwise, infinite cost.
                        grid[j][i][k].setCost(RobotConstants.MAX_COST, RobotConstants.MAX_COST);
                    }
                }
            }
        }

        // initialize frontier queue
        visitQueue.clear();
        this.visitQueue.add(currentNode);
        //greedyCostArray[robotY][robotX][angleDimension] = 0;
        grid[startY][startX][angleDimension].setCost(0, 0);
    }

    /**
     * Checks if a node can be visited.
     */
    private boolean canVisit(Node node) {
        return !node.isPicture() && !node.isVirtualObstacle() && !node.isVisited();
    }

    /**
     * Calculate the number of nodes required to perform a turn.
     * i.e. if turn radius = 24 cm,
     * X X X
     * X
     * X   O
     * <p>
     * Assuming the car is aligned to the center of the grid (i.e. x=5,y=5),
     * 5+24 = 29; the turning circle will be centered at x=29,y=5. (If we are facing north and turning right)
     * Thus, the robot will require 3 grid units worth of space in either direction to complete a turn.
     * i.e. after changing directions, will require ceiling(turnRadius/10)-1 blocks of room to turn.
     */
    private int calculateTurnSize(double turnRadius) {
        double gridSize = MapConstants.OBSTACLE_WIDTH;
        return (int) Math.ceil(turnRadius / gridSize);
    }

    /**
     * given the node of a picture obstacle, get the goal node position
     *
     */
    private int[] getGoalNodePosition(int x, int y, int dir) {
        int dist = AlgoConstants.DISTANCE_FROM_GOAL;
        int[] coords = new int[3];
        switch (dir) {
            case 0:
                coords[0] = x + dist;
                coords[1] = y;
                coords[2] = 180;
                break;
            case 90:
                coords[0] = x;
                coords[1] = y - dist;
                coords[2] = 270;
                break;
            case 180:
                coords[0] = x - dist;
                coords[1] = y;
                coords[2] = 0;
                break;
            case 270:
                coords[0] = x;
                coords[1] = y + dist;
                coords[2] = 90;
                break;
            default:
                return null;
        }
        return coords;
    }

    /**
     * Plans a path from the car position to the selected picture obstacle using a modified A* algorithm.
     * <p>
     * Generating the 4 possible successors of the current cell
     * <p>
     * N
     * |
     * |
     * W----Cell----E
     * |
     * |
     * S
     * <p>
     * Cell-->Popped Cell (i, j)
     * N -->  North       (i-1, j)
     * S -->  South       (i+1, j)
     * E -->  East        (i, j+1)
     * W -->  West        (i, j-1)
     * <p>
     * Rules:
     * A turn cannot occur if the unit has not traveled in its current direction n nodes,
     * where n is the minimum number of nodes required for a turn to occur.
     * <p>
     * A turn cannot occur if there are obstacles within the turning area.
     * <p>
     * The robot must be facing the direction specified by endDirection by the end of the path.
     * <p>
     * input: the x,y, and direction of the picture obstacle, the robot's turn radius
     */
    public ArrayList<MoveType> planPath(int startX, int startY, int startAngle, int pictureX, int pictureY, int pictureDirInDegrees, double turnRadius, boolean doBacktrack, boolean print) {
        clear(startX, startY, startAngle);
        int[] goal = getGoalNodePosition(pictureX, pictureY, pictureDirInDegrees);
        int endX = goal[0];
        int endY = goal[1];
        ArrayList<MoveType> path = null;
        int endDirInDegrees = goal[2];
        boolean goalFound = false;                                  // false since goal node has not yet been found
        int endAngleDimension = angleToDimension(endDirInDegrees);  // which dimension of the 3d array does the goal node lie in
        Node goalNode = grid[endY][endX][endAngleDimension];        // fetch the goal node from the grid array.
        int maxTurnCount = calculateTurnSize(turnRadius);            // get the number of grids that the car needs to move straight after changing directions (for a legal turn)
        // this is the counter for the turnArray. Only when turnArray[y][x] = turnMaxCount is a turn allowed to be made.
        int x, y, dim;

        //Robot r = arena.getRobot();
        turningArray[startY][startX][angleToDimension(startAngle)] = maxTurnCount - 2; // make robot starting position half the required grids to turn.
        Node nextNode;
        int[] forwardLocation, leftLocation, rightLocation;
        int nextX, nextY, nextDim, currentTurnCount;
        double currentGCost, hCost, gCost;
        // lets start searching, baby
        // TODO: need a way to revisit nodes, especially if the goal is found but in an undesirable state (i.e. we are still making a turn)
        //System.out.println("Pathing...");
        while (!goalFound && !visitQueue.isEmpty()) {
            //System.out.println(visitQueue.size());
            currentNode = visitQueue.remove(); // Fetch the head of the priority queue

            x = currentNode.getX();
            y = currentNode.getY();
            dim = currentNode.getDim(); // 0 = east, 1 = north, 2 = west, 3 = south (counter-clockwise)
            currentTurnCount = turningArray[y][x][dim];
            //currentCost = currentNode.getCost();
            currentGCost = currentNode.getGCost();

            if (currentNode == goalNode) {      // we have found the goal
                // TODO: consider multiple goal states? (i.e. the nodes to the left and right of the goal node as well)
                if (currentTurnCount < maxTurnCount - 2) { // we are still turning when we reached the goal state - thus, we cannot end the search.
                    //System.out.println("Goal reached, but still turning");
                    continue; // go to the next iteration, and hope we rediscover the goal node again when we are not turning this time
                }
                //System.out.println("Found goal");
                goalFound = true; // otherwise, we are good to go (search over)!
                break;
            }

            forwardLocation = getForwardNode(x, y, dim);
            if (currentTurnCount != maxTurnCount) { // if we haven't reached a turning point, we can only consider nodes forwards
                // get the forward facing node
                if (forwardLocation != null) { // if this is a valid location to search, add it to the queue
                    nextX = forwardLocation[0];
                    nextY = forwardLocation[1];
                    nextDim = forwardLocation[2];

                    nextNode = grid[nextY][nextX][nextDim];     // put the node into the pred map for backtracking later on
                    predMap.put(nextNode, currentNode);

                    gCost = currentGCost + greedy(currentNode, nextNode); // calculate the respective g-cost and h-cost
                    hCost = heuristic(currentNode, goalNode, endAngleDimension);

                    grid[nextY][nextX][nextDim].setCost(hCost, gCost); // set the cost for the next node and then add to the priority queue
                    visitQueue.add(grid[nextY][nextX][nextDim]);

                    turningArray[nextY][nextX][nextDim] = currentTurnCount + 1; // increment the turn count for the next location.
                }
            } else { // otherwise, we can turn here and should consider the nodes forwards, to the respective left, and the respective right of the current node.
                leftLocation = getLeftNode(x, y, dim);
                rightLocation = getRightNode(x, y, dim);
                if (forwardLocation != null) { // if this is a valid location to search, add it to the queue
                    nextX = forwardLocation[0];
                    nextY = forwardLocation[1];
                    nextDim = forwardLocation[2];

                    nextNode = grid[nextY][nextX][nextDim];
                    predMap.put(nextNode, currentNode);

                    gCost = currentGCost + greedy(currentNode, nextNode);
                    hCost = heuristic(currentNode, goalNode, endAngleDimension);

                    nextNode.setCost(hCost, gCost); // set the cost for the next node and then add to the priority queue
                    visitQueue.add(nextNode);
                    turningArray[nextY][nextX][nextDim] = currentTurnCount; // we can still make a turn, so turning array is max.
                }
                if (leftLocation != null) { // if this is a valid location to search, add it to the queue
                    nextX = leftLocation[0];
                    nextY = leftLocation[1];
                    nextDim = leftLocation[2];

                    nextNode = grid[nextY][nextX][nextDim];
                    predMap.put(nextNode, currentNode);

                    gCost = currentGCost + greedy(currentNode, nextNode);
                    hCost = heuristic(currentNode, goalNode, endAngleDimension);

                    nextNode.setCost(hCost, gCost); // set the cost for the next node and then add to the priority queue
                    visitQueue.add(nextNode);
                    turningArray[nextY][nextX][nextDim] = 0; // turn has started, set to 0.
                    if (nextNode == goalNode) continue;
                }
                if (rightLocation != null) { // if this is a valid location to search, add it to the queue
                    nextX = rightLocation[0];
                    nextY = rightLocation[1];
                    nextDim = rightLocation[2];

                    nextNode = grid[nextY][nextX][nextDim];
                    predMap.put(nextNode, currentNode);

                    gCost = currentGCost + greedy(currentNode, nextNode);
                    hCost = heuristic(currentNode, goalNode, endAngleDimension);

                    nextNode.setCost(hCost, gCost); // set the cost for the next node and then add to the priority queue
                    visitQueue.add(nextNode);
                    turningArray[nextY][nextX][nextDim] = 0; // turn has started, set to 0.
                    if (nextNode == goalNode) continue;
                }
            }
            currentNode.setHasBeenVisited(true);
        }
        if (!goalFound) {
            //System.out.println("Path could not be found to the specified node");
            this.totalCost += 9999;
            return null;
        }
        if (doBacktrack) {
            path = backtrack(goalNode, turnRadius, print);
        }
        if (print && doBacktrack) {
            //System.out.println("path found yey");
            System.out.println("Total cost: " + goalNode.getGCost());
            System.out.println("Nodes expanded: " + predMap.size());
        }
        this.totalCost += goalNode.getGCost();
        return path;
    }

    /**
     * Calculate the coordinates to reverse to
     * @param obs picture obstacle
     * @return array, [xCoord, yCoord, finalCarAngleInDegrees]
     */
    public int[] getReverseCoordinates(PictureObstacle obs) {
        int x = obs.getX();
        int y = obs.getY();
        int minReverse = AlgoConstants.DISTANCE_FROM_GOAL+calculateTurnSize(RobotConstants.TURN_RADIUS)-1;
        int[] goalArray;
        switch (obs.getImadeDirectionAngle()) { // TODO: replace with an algorithm to determine the best back up distance. (Maybe reverse to the closest legal position to the next goal?)
            case 0:
                goalArray = new int[]{x + minReverse, y, 180};
                break;
            case 90:
                goalArray = new int[]{x, y - minReverse, 270};
                break;
            case 180:
                goalArray = new int[]{x - minReverse, y, 0};
                break;
            case 270:
                goalArray = new int[]{x, y + minReverse, 90};
                break;
            default:
                goalArray = null;
        }
        return goalArray;
    }

    public void clearCost() {
        this.totalCost = 0;
    }

    public double getTotalCost() {
        return totalCost;
    }

    private int[] getForwardNode(int x, int y, int dim) {
        int[] pair;
        switch (dim) {
            case 0: // east, x+1,y
                pair = new int[]{x + 1, y, dim};
                break;
            case 1: // north, x,y-1
                pair = new int[]{x, y - 1, dim};
                break;
            case 2: // west, x-1,y
                pair = new int[]{x - 1, y, dim};
                break;
            case 3: // south, x,y-1
                pair = new int[]{x, y + 1, dim};
                break;
            default: // error
                pair = null;
                break;
        }
        if (pair != null && isValidLocation(pair[0], pair[1], dim)) return pair;
        else return null;
    }

    // get node to the left of the current node (considering the direction facing)
    private int[] getLeftNode(int x, int y, int dim) {
        int[] pair;
        switch (dim) {
            case 0: // east, x,y-1 (facing north)
                pair = new int[]{x, y - 1, 1};
                break;
            case 1: // north, x-1,y (facing west)
                pair = new int[]{x - 1, y, 2};
                break;
            case 2: // west, x,y+1 (facing south)
                pair = new int[]{x, y + 1, 3};
                break;
            case 3: // south, x+1,y (facing east)
                pair = new int[]{x + 1, y, 0};
                break;
            default: // error
                pair = null;
                break;
        }
        if (pair != null && isValidLocation(pair[0], pair[1], pair[2])) return pair;
        else return null;
    }

    private int[] getRightNode(int x, int y, int dim) {
        int[] pair;
        switch (dim) {
            case 0: // east, x,y+1 (facing south)
                pair = new int[]{x, y + 1, 3};
                break;
            case 1: // north, x+1,y (facing east)
                pair = new int[]{x + 1, y, 0};
                break;
            case 2: // west, x,y-1 (facing north)
                pair = new int[]{x, y - 1, 1};
                break;
            case 3: // south, x-1,y (facing west)
                pair = new int[]{x - 1, y, 2};
                break;
            default: // error
                pair = null;
                break;
        }
        if (pair != null && isValidLocation(pair[0], pair[1], pair[2])) return pair;
        else return null;
    }

    /**
     * Heuristic algorithm using manhattan distance from start node to end node.
     */
    private double heuristic(Node n1, Node n2, int endDim) {
        // TODO: if using multiple goals, heuristic should be the minimum value of the h-cost to each end node.
        int abs1 = Math.abs(n1.getX() - n2.getX());
        int abs2 = Math.abs(n1.getY() - n2.getY());
        // prefer nodes in the same direction as the end direction
        double directionWeight = (n1.getDim() == endDim) ? 1 : 1.5; // TODO: doing this makes the path hug obstacles more tightly. May want to remove
        return (abs1 + abs2) * RobotConstants.MOVE_COST * directionWeight;
    }

    /**
     * Greedy algorithm to calculate the path cost. Additional weight
     * on turning to prefer a straight path when possible.
     */
    private double greedy(Node n1, Node n2) {
        int turnCost = 0;

        // check to see if turning is required to get to that direction (not in the same angle dimension)
        if (n1.getDim() != n2.getDim()) { // TODO: if we are allowing 180 degree turns, must change this to be a variable turn cost.
            turnCost = RobotConstants.TURN_COST_90;
        }
        // return the sum of the cost to move 1 node + the cost to turn (if turning is done)
        return RobotConstants.MOVE_COST + turnCost;
    }

    /**
     * backtrack from the goal node to get the path
     */
    private ArrayList<MoveType> backtrack(Node end, double turnRadius, boolean print) {
        // TODO: give in terms of line segments
        Node curr, next;
        ArrayList<Node> path = new ArrayList<>();
        ArrayList<MoveType> pathSegments = new ArrayList<>();
        path.add(end);
        curr = predMap.get(end);
        int midpoint = MapConstants.OBSTACLE_WIDTH / 2;
        double[] lineEnd = new double[]{end.getX() * MapConstants.OBSTACLE_WIDTH + midpoint, end.getY() * MapConstants.OBSTACLE_WIDTH + midpoint}; // keep track of the end point(x2,y2) of the line segment
        //System.out.println(lineEnd[1]);
        double[] lineStart = new double[2];
        //System.out.println(lineEnd[0] + ", " + lineEnd[1]);
        int prevDir = curr.getDim();
        double newEndX, newEndY;
        int dirInDegrees;
        while (curr != null) {
            path.add(curr);
            next = predMap.get(curr);
            dirInDegrees = prevDir*90;
            if (next == null) {
                lineStart[0] = curr.getX() * MapConstants.OBSTACLE_WIDTH + midpoint;
                lineStart[1] = curr.getY() * MapConstants.OBSTACLE_WIDTH + midpoint;
                pathSegments.add(new LineMove(lineStart[0], lineStart[1], lineEnd[0], lineEnd[1], dirInDegrees, true));
                // TODO: add a curve in between also
            } else if (next.getDim() != prevDir) { // if direction changes, record the point at which that occurs
                lineStart[0] = next.getX() * MapConstants.OBSTACLE_WIDTH + midpoint;
                lineStart[1] = next.getY() * MapConstants.OBSTACLE_WIDTH + midpoint;
                switch (prevDir) {
                    case 0: // east
                        lineStart[0] += turnRadius;
                        break;
                    case 1: // north
                        lineStart[1] -= turnRadius;
                        break;
                    case 2: // west
                        lineStart[0] -= turnRadius;
                        break;
                    case 3: // south
                        lineStart[1] += turnRadius;
                        break;
                    default: // wut
                }
                pathSegments.add(new LineMove(lineStart[0], lineStart[1], lineEnd[0], lineEnd[1], dirInDegrees, true));
                prevDir = next.getDim();
                lineEnd[0] = next.getX() * MapConstants.OBSTACLE_WIDTH + midpoint;
                lineEnd[1] = next.getY() * MapConstants.OBSTACLE_WIDTH + midpoint;
                switch (prevDir) {
                    case 0: // east
                        lineEnd[0] -= turnRadius;
                        break;
                    case 1: // north
                        lineEnd[1] += turnRadius;
                        break;
                    case 2: // west
                        lineEnd[0] += turnRadius;
                        break;
                    case 3: // south
                        lineEnd[1] -= turnRadius;
                        break;
                    default: // wut
                }
                // add the turn to the list
                pathSegments.add(new ArcMove(lineEnd[0], lineEnd[1], lineStart[0], lineStart[1], dirInDegrees, RobotConstants.TURN_RADIUS, false ));
            }
            curr = next;
        }

        Collections.reverse(path); // reverse the path and put it in the correct order
        if (print) printPath(path);
        Collections.reverse(pathSegments);
        //for (MoveType line : pathSegments) System.out.println(line.toString());
        return pathSegments;
    }

    private boolean isValidLocation(int x, int y, int dim) {
        if (x >= 0 && x < numCells && y >= 0 && y < numCells) {
            Node n = grid[y][x][dim];
            return canVisit(n);
        } else {
            return false;
        }
    }

    /**
     * Instantiate the grid map.
     */
    public void constructMap() {
        //Map<Integer, PictureObstacle> pictureObstacleMap = arena.getObstacles();
        ArrayList<PictureObstacle> pictureObstacleList = arena.getObstacles();

        grid = new Node[numCells][numCells][4]; // instantiate the grid (we assume it is a square grid), and that we have 4 possible cardinal directions
        // fill up the grid map
        for (int i = 0; i < numCells; i++) {
            for (int j = 0; j < numCells; j++) {
                for (int k = 0; k < 4; k++) {
                    grid[i][j][k] = new Node(false, false, j, i, k);
                }
            }
        }

        int angleDimension, x, y, id;
        // set picture nodes to isObstacle = true
        for (PictureObstacle pictures : pictureObstacleList) { //pictureObstacleMap.values()
            x = pictures.getX();
            y = pictures.getY();
            id = pictureObstacleList.indexOf(pictures);//pictures.getKey();
            angleDimension = angleToDimension(pictures.getImadeDirectionAngle()); // calculate the correct angle dimension the picture node is set in.

            grid[y][x][angleDimension].setPicture(true);
            grid[y][x][angleDimension].setPictureId(id);
            int[][] pairs = getVirtualObstaclePairs(x, y, AlgoConstants.BORDER_THICKNESS);
            int xVirtual, yVirtual;
            // set the surrounding nodes to be virtual obstacles
            for (int[] pair : pairs) {
                xVirtual = pair[0];
                yVirtual = pair[1];
                for (int i = 0; i < 4; i++) {
                    if (xVirtual >= 0 && xVirtual < numCells && yVirtual >= 0 && yVirtual < numCells) { // is the given pair a valid location
                        grid[yVirtual][xVirtual][i].setVirtualObstacle(true);
                    }
                }
            }


            //System.out.printf("Node %d at %d, %d\n", id, x, y);
        }

        //constructMap();

        int robotX = arena.getRobot().getX();
        int robotY = arena.getRobot().getY();
        int robotDirection = arena.getRobot().getRobotDirectionAngle();
        angleDimension = angleToDimension(robotDirection);

        this.currentNode = grid[robotY][robotX][angleDimension];

        // initialize the arrays
        int numCells = MapConstants.ARENA_WIDTH / MapConstants.OBSTACLE_WIDTH;
        //this.greedyCostArray = new double[numCells][numCells][4];
        this.turningArray = new int[numCells][numCells][4];
        for (int i = 0; i < numCells; i++) {
            for (int j = 0; j < numCells; j++) {
                for (int k = 0; k < 4; k++) {
                    if (canVisit(grid[j][i][k])) {
                        //greedyCostArray[j][i][k] = 0; // if visitable, set the initial cost to 0.
                        grid[j][i][k].setCost(0, 0);
                    } else {
                        //greedyCostArray[j][i][k] = RobotConstants.MAX_COST; // otherwise, infinite cost.
                        grid[j][i][k].setCost(RobotConstants.MAX_COST, RobotConstants.MAX_COST);
                    }
                }
            }
        }

        // initialize frontier queue
        this.visitQueue = new PriorityQueue<>(new NodeComparator());
        this.visitQueue.add(currentNode);
        //greedyCostArray[robotY][robotX][angleDimension] = 0;
        grid[robotY][robotX][angleDimension].setCost(0, 0);

        //System.out.println("Map construction finished");
        //System.out.println(greedy(0,0,18,18));
    }

    private int angleToDimension(int angle) {
        return angle / 90;
    }

    /**
     * get the locations of the virtual obstacles in terms of pairs [x,y] given a specific x,y
     * TODO: set thickness of virtual obstacles
     */
    private int[][] getVirtualObstaclePairs(int x, int y, int thickness) {
        int numCol = 1+2*thickness;
        int numPairs = numCol*numCol-1; // how many pairs we must generate

        int[][] pairArray = new int[numPairs][];
        int[][] coordinateArray = new int[numCol][numCol];
        int dim = coordinateArray.length;
        int relativeCenter = dim / 2;
        int counter = 0;
        for (int y1 = 0; y1 < dim; y1++) {
            for (int x1 = 0; x1 < dim; x1++) {
                if (x1 != relativeCenter || y1 != relativeCenter) {
                    pairArray[counter] = new int[]{x+x1-thickness,y+y1-thickness};
                    counter++;
                }
            }
        }
        return pairArray;
    }

    public void printPath(List<Node> path) {
        char[][] printArray = new char[20][20];
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                //if (x == arena.getRobot().getX() && y == arena.getRobot().getY()) printArray[y][x] = 'R';
                if (grid[y][x][0].isPicture()) printArray[y][x] = 'E';
                else if (grid[y][x][1].isPicture()) printArray[y][x] = 'N';
                else if (grid[y][x][2].isPicture()) printArray[y][x] = 'W';
                else if (grid[y][x][3].isPicture()) printArray[y][x] = 'S';
                else if (grid[y][x][0].isVirtualObstacle()) printArray[y][x] = '/';
                else printArray[y][x] = '-';
            }
        }
        for (Node n : path) {
            int dir = n.getDim();
            switch (dir) {
                case 0:
                    printArray[n.getY()][n.getX()] = '>';
                    break;
                case 1:
                    printArray[n.getY()][n.getX()] = '^';
                    break;
                case 2:
                    printArray[n.getY()][n.getX()] = '<';
                    break;
                case 3:
                    printArray[n.getY()][n.getX()] = 'v';
                    break;
                default:
                    printArray[n.getY()][n.getX()] = 'x';
                    break;
            }
            if (n.getX() == arena.getRobot().getX() && n.getY() == arena.getRobot().getY())
                printArray[n.getY()][n.getX()] = 'R';
        }
        printArray[path.get(0).getY()][path.get(0).getX()] = 'R';

        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                System.out.print(printArray[y][x] + "  ");
            }
            System.out.println();
        }
    }
}

