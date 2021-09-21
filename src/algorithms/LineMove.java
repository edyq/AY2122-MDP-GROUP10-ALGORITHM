package algorithms;

public class LineMove extends MoveType {
    public LineMove(double x1, double y1, double x2, double y2, int dirInDegrees, boolean isLine) {
        super(x1, y1, x2, y2, dirInDegrees, isLine);
    }

    @Override
    public double getLength() {
        double x1 = getX1();
        double y1 = getY1();
        double x2 = getX2();
        double y2 = getY2();

        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    @Override
    public String toString() {
        return "Line: " + super.toString();
    }

    @Override
    public double getRadius() {
        return 0;
    }
}
