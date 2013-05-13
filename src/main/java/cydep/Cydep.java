package cydep;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public final class Cydep {

    private final Configuration config;
    private final Input input;
    private final Output output;

    public Cydep(Configuration config, Input input, Output output) {
        this.config = config;
        this.input = input;
        this.output = output;
    }

    public void draw() throws IOException {
        int gridSize = input.getGridSize();
        int imageType = BufferedImage.TYPE_BYTE_INDEXED;
        IndexColorModel cm = config.getIndexColorModel();
        BufferedImage image = new BufferedImage(gridSize, gridSize, imageType, cm);
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                double value = input.getValue(x, y);
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
