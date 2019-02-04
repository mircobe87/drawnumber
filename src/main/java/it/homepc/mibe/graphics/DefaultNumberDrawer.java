package it.homepc.mibe.graphics;

import java.awt.geom.Ellipse2D;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.awt.Graphics2D;
import java.awt.Color;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

public class DefaultNumberDrawer implements NumberDrawer {

    private int height;
    private int width;
    private Map<Integer, Integer> occurenceMap;

    DefaultNumberDrawer(int size, String strNum) {
        this.height = size;
        this.width = size;
        this.occurenceMap = new TreeMap<>();
        for (int i=0, len = strNum.length(); i<len; i++) {
            char c = strNum.charAt(i);
            int n = c - '0';
            if (!occurenceMap.containsKey(n)){
                occurenceMap.put(n, 1);
            } else {
                occurenceMap.put(n, occurenceMap.get(n) + 1);
            }
        }
    }

    private int getMaxOccurenceValue() {
        return occurenceMap.values().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    private int getMinOccurenceValue() {
        return occurenceMap.values().stream().min(Comparator.reverseOrder()).orElse(0);
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

        // Ask the test to render into the SVG Graphics2D implementation.
        svgGenerator.setPaint(Color.red);
        svgGenerator.fill(new Ellipse2D.Double(0, 0, width, height));

        return svgGenerator;
    }
}
