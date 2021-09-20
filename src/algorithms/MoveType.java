package algorithms;

public abstract class MoveType {
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private int dirInDegrees;
    private boolean isLine;

    public MoveType(double x1, double y1, double x2, double y2, int dirInDegrees, boolean isLine) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.dirInDegrees = dirInDegrees;
        this.isLine = isLine;
    }

    public abstract double getLength();

    public double getX1() {
        return x1;
    }

    public boolean isLine() {
        return isLine;
    }

    public int getDirInDegrees() {
        return dirInDegrees;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    @Override
    public String toString() {
        return "<" + x1 + ", " + y1 + ">, <" + x2 + ", " + y2 + ">, dir = " + dirInDegrees;
    }

    public abstract double getRadius();
}
