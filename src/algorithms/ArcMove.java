package algorithms;

public class ArcMove extends MoveType{
    private double radiusX;
    private double radiusY;
    private boolean turnLeft;

    public ArcMove(double x1, double y1, double x2, double y2, int dirInDegrees, double radiusX, double radiusY, boolean isLine, boolean turnLeft) {
        super(x1, y1, x2, y2, dirInDegrees, isLine, false);
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.turnLeft = turnLeft;
    }

    public double getRadiusX() {
        return radiusX;
    }

    public double getRadiusY() {
        return radiusY;
    }

    public boolean isTurnLeft() {
        return turnLeft;
    }

    @Override
    public double getLength() {
        return 2*Math.PI*radiusX*0.25;
    }

    @Override
    public String toString() {
        if (turnLeft)
            return "Arc: Turning left, " + super.toString();
        else
            return "Arc: Turning right, " + super.toString();
    }
}
