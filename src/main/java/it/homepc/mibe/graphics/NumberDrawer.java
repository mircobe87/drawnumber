package it.homepc.mibe.graphics;

import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.Document;

public interface NumberDrawer {
    byte[] draw() throws SVGGraphics2DIOException;
}
