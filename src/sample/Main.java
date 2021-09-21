package sample;

import algorithms.FastestPathAlgo;
import algorithms.MoveType;
import algorithms.TripPlannerAlgo;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.util.converter.IntegerStringConverter;
import map.Arena;
import map.MapConstants;
import map.MapConstants.IMAGE_DIRECTION;
import map.PictureObstacle;
import robot.Robot;
import robot.RobotConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.UnaryOperator;

public class Main extends Application {
    private final int dim = MapConstants.ARENA_WIDTH;
    private final int scale = ViewConstants.SCALE;
    private final int arenaSize = dim * scale;
    private final int gridSize = arenaSize / (MapConstants.ARENA_WIDTH / MapConstants.OBSTACLE_WIDTH);
    private ArrayList<Obstacle> obsList = new ArrayList<>();

    private static Robot bot;
    private static FastestPathAlgo fast;
    private static TripPlannerAlgo algo;

    private static Arena arena = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
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
        Rectangle robot = new Rectangle(0, 0, 20 * scale, 23 * scale);
        Point robotCoords = RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES;
        robot.setX(robotCoords.getX() * gridSize - robot.getWidth() / 4);
        robot.setY(robotCoords.getY() * gridSize - robot.getHeight() / 4);
        System.out.println(robot.getX());
        robot.setFill(ViewConstants.ROBOT_COLOR);
        robot.setStrokeWidth(20);
        //robot.setX()
        arenaPane.getChildren().addAll(robot);
        //arenaPane.getChildren().addAll(animateRobot());
        //animateRobot(robot);

        /*
        addObstacle(arenaPane,5,10,IMAGE_DIRECTION.SOUTH);
        addObstacle(arenaPane,15,15,IMAGE_DIRECTION.WEST);
        addObstacle(arenaPane,4,4,IMAGE_DIRECTION.NORTH);
        addObstacle(arenaPane,15,5,IMAGE_DIRECTION.WEST);
        addObstacle(arenaPane,10,15,IMAGE_DIRECTION.NORTH);
        */

        // shortest path label
        Label shortestPathLabel = new Label("Shortest path: ");

        // input fields
        Label xLabel = new Label("X Pos:");
        Label yLabel = new Label("Y Pos:");
        Label dirLabel = new Label("Direction:");

        TextField xField = new TextField();
        TextField yField = new TextField();

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([0-9][0-9]*)?")) {
                return change;
            }
            return null;
        };

        xField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));
        yField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));


        ObservableList<String> options = FXCollections.observableArrayList(
                "North", "South", "East", "West");
        ComboBox directionBox = new ComboBox(options);
        directionBox.getSelectionModel().selectFirst();

        // buttons
        Button obstacleButton = new Button("Add Obstacle");
        EventHandler<ActionEvent> addObstacle = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                String dir = (String) directionBox.getValue();
                System.out.println(Integer.parseInt(xField.getText()));
                addObstacle(arenaPane, Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()), IMAGE_DIRECTION.valueOf(dir.toUpperCase()));
            }
        };
        obstacleButton.setOnAction(addObstacle);

        Button simulateButton = new Button("Run Simulation");
        EventHandler<ActionEvent> runSimulation = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                runSimulation(shortestPathLabel, robot);
            }
        };

        simulateButton.setOnAction(runSimulation);


        GridPane buttonBar = new GridPane();
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.add(xLabel, 0, 0);
        buttonBar.add(yLabel, 1, 0);
        buttonBar.add(dirLabel, 2, 0);
        buttonBar.add(xField, 0, 1);
        buttonBar.add(yField, 1, 1);
        buttonBar.add(directionBox, 2, 1);
        buttonBar.add(obstacleButton, 0, 2, 3, 1);
        buttonBar.add(simulateButton, 3, 2, 3, 1);
        obstacleButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        simulateButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(15);
        buttonBar.getColumnConstraints().addAll(cc, cc, cc, cc);
        buttonBar.setMinWidth(arenaSize);
        buttonBar.setMinHeight(100);

        // place arena and control panel into vertical box
        VBox vbox = new VBox(arenaPane, shortestPathLabel, buttonBar);

        // pack everything into the stage
        primaryStage.setTitle("Simulator");
        primaryStage.setScene(new Scene(vbox, arenaSize, arenaSize + 100));
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    public void runSimulation(Label label, Rectangle robot) {
        ArrayList<ArrayList<MoveType>> moveList = new ArrayList<>();
        ArrayList<PictureObstacle> pictureList = Arena.getObstacles();
        SequentialTransition seqT = new SequentialTransition();

        algo.constructMap();

        // first, get the shortest path.
        int[] fastestPath = fast.planFastestPath();
        String text = "Shortest path: ";
        int[] startCoords = new int[3];
        startCoords[0] = bot.getX();
        startCoords[1] = bot.getY();
        startCoords[2] = bot.getRobotDirectionAngle();
        double turnRadius = RobotConstants.TURN_RADIUS;
        PictureObstacle n;
        for (int i : fastestPath) {
            n = pictureList.get(i);
            text += "<" + n.getX() + ", " + n.getY() + ">, ";
            moveList.add(algo.planPath(startCoords[0], startCoords[1], startCoords[2], n.getX(), n.getY(), n.getImadeDirectionAngle(), turnRadius, true, true));
            startCoords = algo.getReverseCoordinates(n);
        }
        label.setText(text);

        for (ArrayList<MoveType> moves : moveList) {
            seqT.getChildren().add(getPathAnimation(robot, moves));
        }
        seqT.play();
    }

    /**
     * Redraw the arena
     *
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

    private PathTransition getPathAnimation(Rectangle robot, ArrayList<MoveType> pathList) {
        //Creating a Path
        Path path = new Path();

        //Moving to the starting point
        MoveType start = pathList.get(0);
        double startX = robot.getX();
        double startY = robot.getY();
        int startDir = start.getDirInDegrees();
        double radius;
        int endDir;
        System.out.print(robot.getX());
        MoveTo moveTo = new MoveTo(startX, startY);
        path.getElements().add(moveTo);
        System.out.println(startX);

        for (MoveType move : pathList) {
            if (move.isLine()) { // moving straight
                LineTo line = new LineTo(move.getX2() * gridSize, move.getY2() * gridSize);
                path.getElements().add(line);
                //System.out.println(move.getX2()*gridSize);
                //System.out.println(startX);
            } else { // its a turn
                ArcTo turn = new ArcTo();
                radius = move.getRadius() * scale;
                turn.setRadiusX(radius);
                turn.setRadiusY(radius);
                endDir = move.getDirInDegrees();
                if ((startDir == 0 || endDir == 0) && (startDir == 90 || endDir == 90)) {
                    turn.setX(move.getX1() * gridSize + radius);
                    turn.setY(move.getY1() * gridSize - radius);
                } else if ((startDir == 90 || endDir == 90) && (startDir == 180 || endDir == 180)) {
                    turn.setX(move.getX1() * gridSize - radius);
                    turn.setY(move.getY1() * gridSize - radius);
                } else if ((startDir == 180 || endDir == 180) && (startDir == 270 || endDir == 270)) {
                    turn.setX(move.getX1() * gridSize - radius);
                    turn.setY(move.getY1() * gridSize + radius);
                } else if ((startDir == 270 || endDir == 270) && (startDir == 0 || endDir == 0)) {
                    turn.setX(move.getX1() * gridSize + radius);
                    turn.setY(move.getY1() * gridSize + radius);
                }
                path.getElements().add(turn);
            }
        }


        //Adding all the elements to the path

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
        //pathTransition.play();
        return pathTransition;
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

    private void addObstacle(Pane arenaPane, int x, int y, IMAGE_DIRECTION dir) {
        boolean success = arena.addPictureObstacle(x, y, dir);
        if (success) {
            Obstacle obs = new Obstacle(x, y, dir);
            obs.addToPane(arenaPane);
            obsList.add(obs);
        }
    }

    private class Obstacle {
        Rectangle obstacle;
        Rectangle indicator;
        Label idLabel;

        //StackPane stack;
        public Obstacle(int x, int y, IMAGE_DIRECTION dir) {
            int xPos = x * gridSize;
            int yPos = y * gridSize;
            //stack = new StackPane();
            obstacle = new Rectangle(xPos, yPos, gridSize, gridSize);
            obstacle.setFill(ViewConstants.OBSTACLE_COLOR);
            switch (dir) {
                case NORTH:
                    indicator = new Rectangle(xPos, yPos, gridSize, gridSize / 10);
                    break;
                case SOUTH:
                    indicator = new Rectangle(xPos, yPos + (gridSize - gridSize / 10), gridSize, gridSize / 10);
                    break;
                case EAST:
                    indicator = new Rectangle(xPos + (gridSize - gridSize / 10), yPos, gridSize / 10, gridSize);
                    break;
                case WEST:
                    indicator = new Rectangle(xPos, yPos, gridSize / 10, gridSize);
                    break;
                default: // ???
                    indicator = null;
            }
            indicator.setFill(ViewConstants.IMAGE_INDICATOR_COLOR);
            idLabel = new Label(String.valueOf(obsList.size() + 1));
            idLabel.setAlignment(Pos.CENTER);
            idLabel.setFont(new Font(5 * scale));
            idLabel.setTextFill(ViewConstants.OBSTACLE_TEXT_COLOR);
            idLabel.setTranslateX(xPos + (gridSize / 4));
            idLabel.setTranslateY(yPos + (gridSize / 4));
            //stack.getChildren().addAll(obstacle, idLabel);
            //stack.set
        }

        public void setText(String text) {
            idLabel.setText(text);
        }

        public void addToPane(Pane pane) {
            pane.getChildren().addAll(obstacle, indicator, idLabel);
        }

        public void removeFromPane(Pane pane) {
            pane.getChildren().removeAll(obstacle, indicator, idLabel);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
