package runner;

import algorithms.TripPlannerAlgo;
import utils.CommMgr;

/**
 * This class should be executed when running the actual robot
 * Network connection will be established between the laptop running this algo
 * and the RPI on the car/robot
 */
public class Runner {
    private static TripPlannerAlgo algo;

    private static void main(String[] args) {
        CommMgr comm = CommMgr.getCommMgr();

        // receive obstacle positions from rpi
        comm.connectToRPi();
        String obstaclePositions = comm.recieveMsg();


        // adjust the position of the robot if the img_recog returns null
        // move 1cm forward every time and retake picture

        // replan the path if encounters bull's eye
    }
}
