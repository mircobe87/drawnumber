package it.homepc.mibe.graphics;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

public class DefaultNumberDrawer implements NumberDrawer {

    private static final int CANVAS_GRID_SIZE = 1024;
    private static final int DRAW_GRID_MARGIN = 32;
    private static final int DRAW_STROKE_SIZE = 4;
    private static final int DRAW_TICS_SIZE   = 16;

    private Dimension dimension;
    private Map<Integer, Integer> occurenceMap;
    private int[] digits;

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
        getDigitPositions(svg, f).forEach( (i, point) -> svg.drawString(i+"", point[0], point[1]));
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
            svg.setColor(Color.getHSBColor((float) i/10, 1, 1f));
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

        return svg;
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
