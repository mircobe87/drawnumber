package it.homepc.mibe.graphics;

import it.homepc.mibe.graphics.algebra.InfiniteSolutionException;
import it.homepc.mibe.graphics.algebra.Line;
import it.homepc.mibe.graphics.algebra.NoSolutionException;
import it.homepc.mibe.graphics.algebra.Point;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultNumberDrawer implements NumberDrawer {

    private static final int X = 0;
    private static final int Y = 1;

    private static final int CANVAS_GRID_SIZE = 1024;
    private static final int DRAW_GRID_MARGIN = 32;
    private static final int DRAW_STROKE_SIZE = 4;
    private static final int DRAW_ARC_STROKE_SIZE = 2;
    private static final int DRAW_TICS_SIZE   = 16;

    private Dimension dimension;
    private Map<Integer, Integer> occurenceMap;
    private int[] digits;
    private int[] nextPointIndex = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private double[][][] points;
    private Color[] digitColor;

    DefaultNumberDrawer(String strNum) {
        int len = strNum.length();
        digits = new int[len];

        this.dimension = new Dimension(CANVAS_GRID_SIZE, CANVAS_GRID_SIZE);
        this.occurenceMap = new TreeMap<>();

        for (int i=0; i<10; i++) {
            occurenceMap.put(i, 0);
        }
        for (int i=0; i<len; i++) {
            char c = strNum.charAt(i);
            int n = c - '0';
            digits[i] = n;
            occurenceMap.put(n, occurenceMap.get(n) + 1);
        }

        final double radius = (double) CANVAS_GRID_SIZE /2 - DRAW_GRID_MARGIN;
        final double offset = (double) CANVAS_GRID_SIZE /2;
        final int maxOccurence = getMaxOccurenceValue();
        points = new double[10][maxOccurence][2];
        for (int i=0; i<10; i++) {
            for (int turn=0; turn < maxOccurence; turn++) {
                points[i][turn][X] =   radius * Math.cos(i*2*Math.PI/10 + turn*2*Math.PI/10/maxOccurence) + offset;
                points[i][turn][Y] = - radius * Math.sin(i*2*Math.PI/10 + turn*2*Math.PI/10/maxOccurence) + offset;
            }
        }

        digitColor = new Color[10];
        for(int i=0; i<10; i++) {
            digitColor[i] = Color.getHSBColor((float) i/10, 1, 1f);
        }
    }

    private double[] popPointForDigit(int digit) {
        return points[digit][nextPointIndex[digit]++];
    }

    private double[] peekPointForDigit(int digit) {
        return points[digit][nextPointIndex[digit]];
    }

    private int getMaxOccurenceValue() {
        return occurenceMap.values().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    private int getMinOccurenceValue() {
        return occurenceMap.values().stream().min(Comparator.reverseOrder()).orElse(0);
    }

    private SVGGraphics2D drawBackground(SVGGraphics2D svg) {
        svg.setPaint(Color.black);
        svg.fill(new Ellipse2D.Double(0, 0, dimension.getWidth(), dimension.getHeight()));
        return svg;
    }

    private SVGGraphics2D drawGrid(SVGGraphics2D svg) {
        svg.setStroke(new BasicStroke(DRAW_STROKE_SIZE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        // draw the white circle.
        svg.setPaint(Color.white);
        svg.draw(new Ellipse2D.Double(
                DRAW_GRID_MARGIN,
                DRAW_GRID_MARGIN,
                dimension.getWidth()  - 2*DRAW_GRID_MARGIN,
                dimension.getHeight() - 2*DRAW_GRID_MARGIN
        ));

        // draw the tics
        List<Line2D.Double> tics = getGridTics();
        tics.forEach(svg::draw);

        // draw the text
        Font f = new Font("Monospaced", Font.BOLD, DRAW_TICS_SIZE);
        svg.setFont(f);
        getDigitPositions(svg, f).forEach( (i, point) -> svg.drawString(i+"", point[X], point[Y]));
        return svg;
    }

    private Map<Integer, float[]> getDigitPositions(SVGGraphics2D svg, Font font) {
        FontMetrics metrics = svg.getFontMetrics(font);
        Map<Integer, float[]> pointMap = new TreeMap<>();
        occurenceMap.keySet().forEach(i -> {
            float radius = (float)CANVAS_GRID_SIZE/2 - (3f/4)*DRAW_GRID_MARGIN;
            float alpha = i * 2 * (float)Math.PI / 10 + 2*(float)Math.PI/20;
            float offset = (float)CANVAS_GRID_SIZE/2;
            Rectangle2D boundingBox = metrics.getStringBounds(i+"", svg);
            float txtOffsetX = - (float) boundingBox.getWidth()/2;
            float txtOffsetY = (float) boundingBox.getHeight()/4;
            pointMap.put(
                    i,
                    new float[] {
                            radius*(float) Math.cos(alpha) + offset + txtOffsetX,
                            - radius*(float) Math.sin(alpha) + offset + txtOffsetY
                    }
            );
        });
        return pointMap;
    }

    private SVGGraphics2D drawColorRing(SVGGraphics2D svg) {
        List<Arc2D.Double> arcs = getColorCircleSections();
        svg.setStroke(new BasicStroke(DRAW_TICS_SIZE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        for (int i=0, len=arcs.size(); i<len; i++) {
            svg.setColor(digitColor[i]);
            svg.draw(arcs.get(i));
        }
        return svg;
    }

    private List<Line2D.Double> getGridTics() {
        final double radius = (double) CANVAS_GRID_SIZE /2 - DRAW_GRID_MARGIN + DRAW_TICS_SIZE;
        final double offset = (double) CANVAS_GRID_SIZE /2;
        return occurenceMap.keySet().stream().map( i -> {
            double alpha = i * 2 * Math.PI / 10;
            double x0 =   radius * Math.cos(alpha);
            double y0 = - radius * Math.sin(alpha);
            double x1 =   (radius - DRAW_TICS_SIZE) * Math.cos(alpha);
            double y1 = - (radius - DRAW_TICS_SIZE) * Math.sin(alpha);
            return new Line2D.Double(x0+offset, y0+offset, x1+offset, y1+offset);
        }).collect(Collectors.toList());
    }

    private List<Arc2D.Double> getColorCircleSections() {
        final Rectangle2D arcFrame = new Rectangle2D.Double(
                DRAW_GRID_MARGIN - (double)DRAW_TICS_SIZE/2,
                DRAW_GRID_MARGIN - (double)DRAW_TICS_SIZE/2,
                CANVAS_GRID_SIZE - 2*(DRAW_GRID_MARGIN - (double)DRAW_TICS_SIZE/2),
                CANVAS_GRID_SIZE - 2*(DRAW_GRID_MARGIN - (double)DRAW_TICS_SIZE/2)
        );
        return occurenceMap.keySet().stream().map( i -> {
            double alpha = i * 36;
            double archWidth = 36;
            return new Arc2D.Double(
                    arcFrame,
                    alpha,
                    archWidth,
                    Arc2D.OPEN
            );
        }).collect(Collectors.toList());
    }

    private SVGGraphics2D drawArcs(SVGGraphics2D svg) {
        GradientPaint gradient;
        for (int i=0, len = digits.length-1; i<len; i++) {
            double[] pointStart = popPointForDigit(digits[i]);
            double[] pointEnd   = peekPointForDigit(digits[i+1]);
            gradient = new GradientPaint((float)pointStart[X], (float)pointStart[Y], digitColor[digits[i]], (float)pointEnd[X], (float)pointEnd[Y], digitColor[digits[i+1]]);
            svg.setPaint(gradient);
            svg.setStroke(new BasicStroke(DRAW_ARC_STROKE_SIZE));
            svg.draw(computeArc(pointStart, pointEnd));
        }
        return svg;
    }

    private Shape computeArc(double[] startPoint, double[] endPoint) {
        Point center = new Point((float)CANVAS_GRID_SIZE/2, (float)CANVAS_GRID_SIZE/2);
        Point p0 = new Point((float)startPoint[X], (float)startPoint[Y]);
        Point p1 = new Point((float)endPoint[X], (float)endPoint[Y]);

        Line constrLine0 = new Line(center, p0).getParpendicular(p0);
        Line constrLine1 = new Line(center, p1).getParpendicular(p1);

        try {
            Point arcCenter = constrLine0.intersection(constrLine1);
            float arcRadius = arcCenter.distanceOf(p0);
            Rectangle2D arcBBox = new Rectangle2D.Float(
                    arcCenter.getX()-arcRadius,
                    arcCenter.getY()-arcRadius,
                    2*arcRadius,
                    2*arcRadius
            );
            float startAlpha = normalizeCorner(arcCenter, p0);
            float endAlpha   = normalizeCorner(arcCenter, p1);
            float delta = computeCornerExtend(startAlpha, endAlpha);

            return new Arc2D.Float(
                    arcBBox,
                    startAlpha,
                    delta,
                    Arc2D.OPEN
            );

        } catch (Exception e) {
            return new Line2D.Float(p0.getX(), p0.getY(), p1.getX(), p1.getY());
        }
    }

//    private float normalizeCorner(Point center, Point p) {
//        Line constrLine = new Line(center, p);
//        float startAlpha;
//        if (p.getY()-center.getY() >= 0) {
//            // I or II
//            if (p.getX()-center.getX() >= 0) {
//                // I
//                startAlpha = (float) Math.atan(constrLine.getM()) * 180f/(float)Math.PI;
//            } else {
//                // II
//                startAlpha = (float) Math.atan(constrLine.getM()) * 180f/(float)Math.PI + 180f;
//            }
//        } else {
//            // III or IV
//            if (p.getX()-center.getX() >= 0) {
//                // IV
//                startAlpha = (float) Math.atan(constrLine.getM()) * 180f/(float)Math.PI + 360f;
//            } else {
//                // III
//                startAlpha = (float) Math.atan(constrLine.getM()) * 180f/(float)Math.PI + 180f;
//            }
//        }
//        return startAlpha;
//    }

    private float normalizeCorner(Point center, Point p) {
        Line constrLine = new Line(center, p);
        float startAlpha;
        if (p.getY()-center.getY() >= 0) {
            // I or II
            if (p.getX()-center.getX() >= 0) {
                // I
                startAlpha = 360f - (float) Math.atan(constrLine.getM()) * 180f/(float)Math.PI;
            } else {
                // II
                startAlpha = - (float) Math.atan(constrLine.getM()) * 180f/(float)Math.PI + 180f;
            }
        } else {
            // III or IV
            if (p.getX()-center.getX() >= 0) {
                // IV
                startAlpha = - (float) Math.atan(constrLine.getM()) * 180f/(float)Math.PI;
            } else {
                // III
                startAlpha = 180f - (float) Math.atan(constrLine.getM()) * 180f/(float)Math.PI;
            }
        }
        return startAlpha;
    }

    private float computeCornerExtend(float start, float end) {
        if (Math.abs(end-start) > 180) {
            return Math.signum(start-end) * (360 - Math.abs(end-start));
        } else {
            return end - start;

        }
    }

    @Override
    public SVGGraphics2D draw() {
        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        svgGenerator.setSVGCanvasSize(dimension);

        svgGenerator = drawBackground(svgGenerator);
        svgGenerator = drawColorRing(svgGenerator);
        svgGenerator = drawArcs(svgGenerator);
        svgGenerator = drawGrid(svgGenerator);

        return svgGenerator;
    }
}
