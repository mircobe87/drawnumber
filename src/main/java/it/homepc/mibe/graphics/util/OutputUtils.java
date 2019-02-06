package it.homepc.mibe.graphics.util;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.Element;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class OutputUtils {

    private static final int MEGABYTE = 1024*1024;

    public static void save(String filename, SVGGraphics2D graphics) throws FileNotFoundException, SVGGraphics2DIOException {
        Element root = graphics.getRoot();
        root.setAttributeNS(null, "viewBox", String.format("0 0 %.0f %.0f", Math.ceil(graphics.getSVGCanvasSize().getWidth()), Math.ceil(graphics.getSVGCanvasSize().getHeight())));
        Writer out = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8);
        graphics.stream(root, out, true, true);
    }

    public static InputStream save(SVGGraphics2D graphics) throws SVGGraphics2DIOException {
        Element root = graphics.getRoot();
        root.setAttributeNS(null, "viewBox", String.format("0 0 %.0f %.0f", Math.ceil(graphics.getSVGCanvasSize().getWidth()), Math.ceil(graphics.getSVGCanvasSize().getHeight())));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(MEGABYTE);
        Writer out = new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8);
        graphics.stream(root, out, true, true);
        byte[] data = byteArrayOutputStream.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(data);
        return inputStream;
    }
}
