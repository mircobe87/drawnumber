package it.homepc.mibe.graphics;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws SVGGraphics2DIOException, FileNotFoundException, UnsupportedEncodingException {
        NumberDrawer drawer = NumberDrawerFactory.getDefaultNumberDrawer("321");
        SVGGraphics2D graphics2D = drawer.draw();

        Writer out = new OutputStreamWriter(new FileOutputStream("test.svg"), "UTF-8");
        graphics2D.stream(out, true);
    }
}
