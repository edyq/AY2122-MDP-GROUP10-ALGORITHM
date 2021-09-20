package sample;

import algorithms.FastestPathAlgo;
import algorithms.MoveType;
import algorithms.TripPlannerAlgo;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import map.Arena;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;

import java.util.ArrayList;

public class Main extends Application {
    private final int dim = MapConstants.ARENA_WIDTH;
    private final int scale = ViewConstants.SCALE;
    private final int arenaSize = dim*scale;
    private final int gridSize = arenaSize/(MapConstants.ARENA_WIDTH/MapConstants.OBSTACLE_WIDTH);

    private static Robot bot;
    private static FastestPathAlgo fast;
    private static TripPlannerAlgo algo;

    private static Arena arena = null;

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        // arena section
        // graphics context
        bot = new Robot(RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES, RobotConstants.ROBOT_DIRECTION.NORTH, false);
        //bot = new Robot(new Point(5,15), RobotConstants.ROBOT_DIRECTION.EAST, false);
        arena = new Arena(bot);
        fast = new FastestPathAlgo(arena);
        algo = new TripPlannerAlgo(arena);

        Pane arenaPane = new Pane();
        arenaPane.setMinWidth(arenaSize);
        arenaPane.setMinHeight(arenaSize);
        arenaPane.setBackground(new Background(new BackgroundFill(drawGridLines(), new CornerRadii(0), null)));

        // draw robot
        //int robotXPos =
        Rectangle robot = new Rectangle(0,0,23*scale, 20*scale);
        robot.setFill(Color.DARKVIOLET);
        robot.setStrokeWidth(20);
        //robot.setX()
        arenaPane.getChildren().addAll(robot);
        //arenaPane.getChildren().addAll(animateRobot());
        animateRobot(robot);

        // shortest path label
        Label shortestPathLabel = new Label("Shortest path: ");

        // buttons
        Button obstacleButton = new Button("Input Obstacles");
        Button simulateButton = new Button("Run Simulation");
        obstacleButton.setPrefWidth(arenaSize/2);
        simulateButton.setPrefWidth(arenaSize/2);

        // button functionality
        //obstacleButton.onMouseClickedProperty()

        // control panel section
        HBox buttonBar = new HBox(obstacleButton, simulateButton);
        HBox.setHgrow(obstacleButton, Priority.ALWAYS);
        HBox.setHgrow(simulateButton, Priority.ALWAYS);
        buttonBar.setFillHeight(true);
        buttonBar.setMinWidth(arenaSize);
        buttonBar.setMinHeight(100);
        // place arena and control panel into vertical box
        VBox vbox = new VBox(arenaPane, shortestPathLabel, buttonBar);

        // pack everything into the stage
        primaryStage.setTitle("Simulator");
        primaryStage.setScene(new Scene(vbox, arenaSize, arenaSize+100));
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    /**
     * Redraw the arena
     * @return
     */
    private ImagePattern drawGridLines() {
        // draw the grid lines first
        Canvas canvas = new Canvas(gridSize, gridSize); // for drawing

        GraphicsContext gc =
                canvas.getGraphicsContext2D();

        gc.setStroke(Color.BLACK);
        gc.strokeRect(0.5, 0.5, gridSize, gridSize);
        gc.setFill(Color.WHITE.deriveColor(1, 1, 1, 0.2));
        gc.fillRect(0, 0, gridSize, gridSize);
        gc.strokeRect(0.5, 0.5, gridSize, gridSize);

        Image image = canvas.snapshot(new SnapshotParameters(), null);
        ImagePattern pattern = new ImagePattern(image, 0, 0, gridSize, gridSize, false);

        gc.setFill(pattern);

        return pattern;

    }

    private void animateRobot(Rectangle robot) {
        //Drawing a Circle
        //Rectangle circle = new Rectangle(0,0,23*scale, 20*scale);
        //Setting the position of the circle
        //circle.setCenterX(600);
        //circle.setCenterY(600);

        //Setting the radius of the circle
        //circle.setRadius(25.0f);

        //Setting the color of the circle
        //robot.setFill(Color.DARKVIOLET);

        //Setting the stroke width of the circle
        //robot.setStrokeWidth(20);

        //Creating a Path
        Path path = new Path();

        //Moving to the starting point
        MoveTo moveTo = new MoveTo(108, 71);

        //Creating 1st line
        LineTo line1 = new LineTo(321, 161);

        //Creating 2nd line
        LineTo line2 = new LineTo(126,232);

        //Creating 3rd line
        LineTo line3 = new LineTo(232,52);

        //Creating 4th line
        LineTo line4 = new LineTo(269, 250);

        //Creating 5th line
        LineTo line5 = new LineTo(108, 71);

        //ArcTo arc = new ArcTo()

        //Adding all the elements to the path
        path.getElements().add(moveTo);
        path.getElements().addAll(line1, line2, line3, line4, line5);

        //Creating the path transition
        PathTransition pathTransition = new PathTransition();

        //Setting the duration of the transition
        pathTransition.setDuration(Duration.millis(1000));

        //Setting the node for the transition
        pathTransition.setNode(robot);

        //Setting the path for the transition
        pathTransition.setPath(path);

        //Setting the orientation of the path
        pathTransition.setOrientation(
                PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);

        //Setting the cycle count for the transition
        pathTransition.setCycleCount(0);

        //Setting auto reverse value to true
        pathTransition.setAutoReverse(false);
        pathTransition.setDuration(Duration.millis(10000));

        pathTransition.setInterpolator(Interpolator.LINEAR);

        //Playing the animation
        pathTransition.play();

        //return circle;
    }

    public ImagePattern createGridPattern(GraphicsContext gc) {

        double w = arenaSize;
        double h = arenaSize;

        gc.setStroke(Color.BLACK);
        gc.setFill(Color.LIGHTGRAY.deriveColor(1, 1, 1, 0.2));
        gc.fillRect(0, 0, w, h);
        gc.strokeRect(0, 0, w, h);

        //mage image = canvas.snapshot(new SnapshotParameters(), null);
        //ImagePattern pattern = new ImagePattern(image, 0, 0, w, h, false);

        //return pattern;
        return null;

    }


    public static void main(String[] args) {
        launch(args);
    }
}
