package cydep;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public final class Cydep {

    private final Configuration config;
    private final Input input;
    private final Output output;
    private final int gridSize;
    private final double gridGap;
    private final AffineTransform affine;

    public Cydep(Configuration config, Input input, Output output) {
        this.config = config;
        this.input = input;
        this.output = output;
        this.gridSize = 2 * input.getDistanceAxisLength();
        this.gridGap = input.getDistanceAxisGap();
        this.affine = new AffineTransform();
        affine.scale(gridGap, -gridGap);
        final double offset = 0.5 - gridSize / 2.0;
        affine.translate(offset, offset);
    }

    public void draw() throws IOException {
        int imageType = BufferedImage.TYPE_BYTE_INDEXED;
        IndexColorModel cm = config.getIndexColorModel();
        BufferedImage image = new BufferedImage(gridSize, gridSize, imageType, cm);
        Point ptSrc = new Point();
        Point2D ptDst = new Point2D.Double();
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                ptSrc.setLocation(x, y);
                affine.transform(ptSrc, ptDst);
                double value = input.getValue(ptDst);
                if (!Double.isNaN(value)) {
                    int rgb = config.getRGB(value);
                    image.setRGB(x, y, rgb);
                }
            }
        }
        String formatName = output.getFormatName();
        ImageOutputStream out = output.getImageOutputStream();
        ImageIO.write(image, formatName, out);
    }
}
