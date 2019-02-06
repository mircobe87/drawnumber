package it.homepc.mibe.graphics.algebra;

public class Point {

    public static float distanceOf(Point p0, Point p1) {
        return p0.distanceOf(p1);
    }

    private float x;
    private float y;

    public Point() {
        x=0;
        y=0;
    }

    public Point(float x, float y) {
        this.x=x;
        this.y=y;
    }

    public Point relativeMove(float deltaX, float deltaY) {
        return new Point(x+deltaX, y+deltaY);
    }

    public float distanceOf(Point p) {
        return (float) Math.sqrt(Math.pow(p.getX()-x, 2) + Math.pow(p.getY()-y, 2));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("(%f;%f)", this.getX(), this.getY());
    }
}
