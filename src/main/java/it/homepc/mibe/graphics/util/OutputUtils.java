package it.homepc.mibe.graphics.util;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;

import java.io.*;

public class OutputUtils {

    private static final int MEGABYTE = 1024*1024;

    public static void save(String filename, byte[] svgFile) throws IOException, TranscoderException {
        SVGTranscoder transcoder = new SVGTranscoder();
        OutputStream outputStream = new FileOutputStream(filename);
        Writer writer = new OutputStreamWriter(outputStream);

        Reader reader = new InputStreamReader(new ByteArrayInputStream(svgFile));
        TranscoderInput input = new TranscoderInput(reader);
        TranscoderOutput output = new TranscoderOutput(writer);
        transcoder.transcode(input, output);
        outputStream.flush();
        outputStream.close();
    }

    public static void savePNG(String filename, byte[] svgFile) throws IOException, TranscoderException {
        PNGTranscoder transcoder = new PNGTranscoder();
        OutputStream outputStream = new FileOutputStream(filename);

        InputStream inputStream = new ByteArrayInputStream(svgFile);
        TranscoderInput input = new TranscoderInput(inputStream);
        TranscoderOutput output = new TranscoderOutput(outputStream);
        transcoder.transcode(input, output);
        outputStream.flush();
        outputStream.close();
    }

    public static InputStream savePNG(byte[] svgFile) throws IOException, TranscoderException {
        PNGTranscoder transcoder = new PNGTranscoder();

        InputStream inputStream = new ByteArrayInputStream(svgFile);
        TranscoderInput input = new TranscoderInput(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(MEGABYTE);
        TranscoderOutput output = new TranscoderOutput(outputStream);

        transcoder.transcode(input, output);

        outputStream.flush();
        outputStream.close();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}
