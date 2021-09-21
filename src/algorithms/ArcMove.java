package algorithms;

public class ArcMove extends MoveType{
    private double radius;

    public ArcMove(double x1, double y1, double x2, double y2, int dirInDegrees, double radius, boolean isLine) {
        super(x1, y1, x2, y2, dirInDegrees, isLine, false);
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public double getLength() {
        return 2*Math.PI*radius*0.25;
    }

    @Override
    public String toString() {
        return "Arc: " + super.toString();
    }
}
