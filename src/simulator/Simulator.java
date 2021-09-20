package simulator;

import algorithms.FastestPathAlgo;
import algorithms.TripPlannerAlgo;
import map.Arena;
import map.MapConstants.IMAGE_DIRECTION;
import map.PictureObstacle;
import robot.Robot;
import robot.RobotConstants;

// TODO: switch to javafx

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * This class should be executed when running simulation,
 * which would not connect to the physical robot or any other device.
 */
public class Simulator {
    private static JFrame _appFrame = null;
    private static JPanel _mapCards = null;
    private static JPanel _buttons = null;

    private static Robot bot;
    private static FastestPathAlgo fast;
    private static TripPlannerAlgo algo;

    private static Arena arena = null;
    // TODO: add traversed path;

    /**
     * Initialise the arena and display the application
     */
    public static void main(String[] args) {
        bot = new Robot(RobotConstants.ROBOT_INITIAL_CENTER_COORDINATES, RobotConstants.ROBOT_DIRECTION.NORTH, false);
        //bot = new Robot(new Point(5,15), RobotConstants.ROBOT_DIRECTION.EAST, false);
        arena = new Arena(bot);

        arena.addPictureObstacle(0, 0, IMAGE_DIRECTION.EAST);
        arena.addPictureObstacle(15, 0, IMAGE_DIRECTION.SOUTH);
        arena.addPictureObstacle(18, 18, IMAGE_DIRECTION.NORTH);
        arena.addPictureObstacle(10, 15, IMAGE_DIRECTION.WEST);
        arena.addPictureObstacle(10, 13, IMAGE_DIRECTION.WEST);
        //arena.addPictureObstacle(18,11,IMAGE_DIRECTION.SOUTH);
        arena.addPictureObstacle(2, 11, IMAGE_DIRECTION.SOUTH);
        fast = new FastestPathAlgo(arena);
        algo = new TripPlannerAlgo(arena);
        int[] path = fast.planFastestPath();
        System.out.print("Shortest path: ");
        for (int i : path) System.out.print(i + ", ");
        System.out.println();
        doThePath(path);
        //algo.displayMap();
        //algo.planPath(18,16,270,22);
        //algo.planPath(5,13,0,22);

        display();
    }

    private static void doThePath(int[] path) {
        algo.constructMap();
        Map<Integer, PictureObstacle> map = arena.getObstacles();
        PictureObstacle next;
        int count = 0;
        for (int i : path) {
            next = map.get(i);
            System.out.println("---------------Path " + count + "---------------");
            System.out.println(next.getX() + ", " + next.getY());
            algo.planPath(next.getX(), next.getY(), next.getImadeDirectionAngle(), RobotConstants.TURN_RADIUS, true, true);
            /*
            int x = next.getX();
            int y = next.getY();
            switch (next.getImadeDirectionAngle()) { // simulate backing up
                case 0:
                    bot.setCenterCoordinate(new Point(x + 5, y));
                    bot.setDirection(RobotConstants.ROBOT_DIRECTION.WEST);
                    break;
                case 90:
                    bot.setCenterCoordinate(new Point(x, y - 6));
                    bot.setDirection(RobotConstants.ROBOT_DIRECTION.SOUTH);
                    break;
                case 180:
                    bot.setCenterCoordinate(new Point(x - 6, y));
                    bot.setDirection(RobotConstants.ROBOT_DIRECTION.EAST);
                    break;
                case 270:
                    bot.setCenterCoordinate(new Point(x, y + 6));
                    bot.setDirection(RobotConstants.ROBOT_DIRECTION.NORTH);
                    break;
                default:
            }
             */
            int[] coords = algo.getReverseCoordinates(next);
            bot.setCenterCoordinate(new Point(coords[0], coords[1]));
            bot.setDirection(coords[2]);
            count++;
        }
    }

    /**
     * Initialise the application
     */
    private static void display() {
        // Initialise main frame for display
        _appFrame = new JFrame();
        _appFrame.setTitle("Simulator");
        _appFrame.setSize(new Dimension(700, 750));
        _appFrame.setResizable(false);

        // Create the CardLayout for storing the different maps
        _mapCards = new JPanel(new CardLayout());

        // Create the JPanel for the buttons
        _buttons = new JPanel();

        // Add _mapCards & _buttons to the main frame's content pane
        Container contentPane = _appFrame.getContentPane();
        contentPane.add(_mapCards, BorderLayout.CENTER);
        contentPane.add(_buttons, BorderLayout.PAGE_END);

        // Initialise the main map view
        initMainLayout();

        // Initialise the buttons
        initButtonsLayout();

        // Display the application
        _appFrame.setVisible(true);
        _appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Initialise the main arena view
     */
    private static void initMainLayout() {
        _mapCards.add(arena, "INITIAL_ARENA");
        // TODO: add traversed path
        CardLayout cl = (CardLayout) _mapCards.getLayout();
        cl.show(_mapCards, "INITIAL_ARENA");
    }

    /**
     * Initialise the button layout structure
     */
    private static void initButtonsLayout() {
        _buttons.setLayout(new GridLayout(2, 1));
        addButtons();
    }

    /**
     * Create the required buttons
     * 1. load obstacles => static input, receive from network
     * 2. fastest path
     */
    private static void addButtons() {
        // Button: Load Obstacles from File
        JButton loadObstaclesFromFileButton = new JButton("Load Obstacles from File");
        formatButton(loadObstaclesFromFileButton);
        loadObstaclesFromFileButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JDialog loadObsDialog = new JDialog(_appFrame, "Load Obstacles from File", true);
                loadObsDialog.setSize(600, 80);
                loadObsDialog.setLayout(new FlowLayout());

                final JTextField loadFile = new JTextField(15);
                JButton load = new JButton("Load");

                load.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        loadObsDialog.setVisible(false);
                        // filename = loadFile.getText();
                        // TODO: implement loadObstaclesFromFile(Arena arena, String filename);

                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "INITIAL_ARENA");
                        arena.repaint();
                    }
                });

                loadObsDialog.add(new JLabel("Filename: "));
                loadObsDialog.add(loadFile);
                loadObsDialog.add(load);
                loadObsDialog.setVisible(true);
            }
        });
        _buttons.add(loadObstaclesFromFileButton);

        // Button: Load Preset Obstacles
        JButton loadPresetObstaclesButton = new JButton("Load Preset Obstacles");
        formatButton(loadPresetObstaclesButton);
        loadPresetObstaclesButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // TODO: implement loadPresetObstacles(Arena arena);
                // loadPresetObstacles();

                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "INITIAL_ARENA");
                arena.repaint();
            }
        });
        _buttons.add(loadPresetObstaclesButton);

        // Button: Input Obstacles Manually
        JButton manualInputObsButton = new JButton("Input Obstacles Manually");
        formatButton(manualInputObsButton);
        manualInputObsButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JDialog loadObsDialog = new JDialog(_appFrame, "Input Obstacles Manually", true);
                loadObsDialog.setSize(600, 300);
                loadObsDialog.setLayout(new FlowLayout());

                // TODO: add input fields for
                // 1. center coordinate
                // 2. image direction
                final JTextField obstacle1 = new JTextField(15);
                final JTextField obstacle2 = new JTextField(15);
                final JTextField obstacle3 = new JTextField(15);
                final JTextField obstacle4 = new JTextField(15);
                final JTextField obstacle5 = new JTextField(15);
                JButton load = new JButton("Load");

                load.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        loadObsDialog.setVisible(false);
                        // obstacle1Obj = new PictureObstacle(obstacle1.getText()) etc ...
                        // TODO: implement loadObstaclesFromManualInput(Arena arena, ArrayList<PictureObstacle> obstacles);

                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "INITIAL_ARENA");
                        arena.repaint();
                    }
                });
                loadObsDialog.add(new JLabel("Obstacle 1: "));
                loadObsDialog.add(obstacle1);
                loadObsDialog.add(new JLabel("Obstacle 2: "));
                loadObsDialog.add(obstacle2);
                loadObsDialog.add(new JLabel("Obstacle 3: "));
                loadObsDialog.add(obstacle3);
                loadObsDialog.add(new JLabel("Obstacle 4: "));
                loadObsDialog.add(obstacle4);
                loadObsDialog.add(new JLabel("Obstacle 5: "));
                loadObsDialog.add(obstacle5);
                loadObsDialog.add(load);
                loadObsDialog.setVisible(true);
            }
        });
        _buttons.add(manualInputObsButton);

        // Button: Simulate Fastest Path
        JButton fastestPathButton = new JButton("Fastest Path");
        formatButton(fastestPathButton);
        fastestPathButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                loadDefaultObstacles();

                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "INITIAL_ARENA");
                new FastestPath().execute();
            }
        });
        _buttons.add(fastestPathButton);
    }

    /**
     * Set font for buttons
     */
    private static void formatButton(JButton btn) {
        btn.setFont(new Font("Ariel", Font.BOLD, 13));
        btn.setFocusPainted(false);
    }

    /**
     * Load default obstacles
     */
    private static void loadDefaultObstacles() {
        // TODO: implement
    }

    /**
     * Fastest path
     */
    private static class FastestPath extends SwingWorker<Integer, String> {
        protected Integer doInBackground() throws Exception {
            // TODO: implement
            return 111;
        }
    }

}
