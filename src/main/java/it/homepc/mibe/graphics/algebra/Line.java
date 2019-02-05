package it.homepc.mibe.graphics.algebra;

public class Line {

    private float m;
    private float q;
    private Point origin;

    public Line(Point origin, float m) {
        this.origin = origin;
        this.m = m;
        if (isHorizontal()) {
            this.q = origin.getY();
        } else if (isVertical()) {
            this.q = 0;
        } else {
            this.q = origin.getY() - this.m * origin.getX();
        }
    }

    public Line(Point p0, Point p1) {
        float deltaX = p1.getX()-p0.getX();
        float deltaY = p1.getY()-p0.getY();

        this.m = deltaX == 0 ? Float.POSITIVE_INFINITY * Math.signum(deltaY) : deltaY/deltaX;
        this.q = deltaX == 0 ? 0 : p0.getY() - this.m * p0.getX();
        this.origin = p0;
    }

    public boolean isVertical() {
        return Float.isInfinite(m);
    }

    public boolean isHorizontal() {
        return m == 0;
    }

    public float getM() {
        return m;
    }

    public float getQ() {
        return q;
    }

    public Line getParpendicular(Point p) {
        Line perpendicular;
        if (this.isHorizontal()) {
            perpendicular = new Line(p, Float.POSITIVE_INFINITY);
        } else if (this.isVertical()) {
            perpendicular = new Line(p, 0);
        } else {
            perpendicular = new Line(p, -1/this.getM());
        }
        return perpendicular;
    }

    public Line getInverse() {
        if (isVertical()) {
            return new Line(this.origin, 0);
        } else if (isHorizontal()) {
            return new Line(this.origin, Float.POSITIVE_INFINITY);
        } else {
            return new Line(new Point(this.origin.getY(), this.origin.getX()), 1/this.getM());
        }
    }

    public Point intersection(Line l) throws InfiniteSolutionException, NoSolutionException {
        if (this.equals(l)) {
            throw new InfiniteSolutionException();
        } else if (this.isParallelTo(l)) {
            throw new NoSolutionException();
        } else {
            if (isHorizontal() && l.isVertical()) {
                return new Point(l.origin.getX(), this.origin.getY());
            } else if (isVertical() && l.isHorizontal()) {
                return new Point(this.origin.getX(), l.origin.getY());
            } else if (isHorizontal()) {
                return new Point(l.getInverse().apply(this.origin.getY()), this.origin.getY());
            } else if (isVertical()) {
                return new Point(this.origin.getX(), l.apply(this.origin.getX()));
            } else if (l.isHorizontal()) {
                return new Point(this.getInverse().apply(l.origin.getY()), l.origin.getY());
            } else if (l.isVertical()) {
                return new Point(l.origin.getX(), this.apply(l.origin.getX()));
            } else {
                return new Point(
                        (l.getQ() - this.getQ()) / (this.getM() - l.getM()),
                        (l.getQ() * this.getM() - this.getQ() * l.getM()) / (this.getM() - l.getM())
                );
            }
        }
    }

    public boolean isParallelTo(Line l) {
        if (l == null) return false;
        return l.getM() == this.getM();
    }

    public float apply(float xValue) throws InfiniteSolutionException, NoSolutionException {
        if (isHorizontal()) {
            return this.origin.getY();
        } else if (isVertical()) {
            if (xValue == this.origin.getX()) {
                throw new InfiniteSolutionException();
            } else {
                throw new NoSolutionException();
            }
        } else {
            return this.getM()*xValue+this.getQ();
        }
    }

    public boolean hasOwnPoint(Point p) {
        boolean test;
        try {
            test = this.apply(p.getX()) == p.getY();
        } catch (Exception e) {
            test = false;
        }
        return test;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Line) {
            return ((Line) obj).getM() == this.getM() && ((Line) obj).getQ() == this.getQ();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("[o: %s; m: %f; q:%f]", this.origin, this.m, this.q);
    }
}
