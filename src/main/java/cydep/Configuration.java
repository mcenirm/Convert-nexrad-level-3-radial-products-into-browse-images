package cydep;

import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.TreeMap;

public final class Configuration {

    private final String pathname;
    private final File file;
    private final String dataVariableName;
    private final NavigableMap<Double, Color> colormap;
    static final String FORMAT =
            "DataVariableName\n"
            + "cutoff1 red1 green1 blue1\n"
            + ".\n.\n.\n"
            + "cutoffN redN greenN blueN\n";

    public Configuration(String pathname) throws FileNotFoundException {
        this.pathname = pathname;
        this.file = new File(pathname);
        Scanner scanner = new Scanner(this.file);
        this.dataVariableName = scanner.nextLine();
        this.colormap = new TreeMap<Double, Color>();
        while (scanner.hasNext()) {
            double cutoff = scanner.nextDouble();
            short red = scanner.nextShort();
            checkUnsignedByte(red, scanner.radix());
            short green = scanner.nextShort();
            checkUnsignedByte(green, scanner.radix());
            short blue = scanner.nextShort();
            checkUnsignedByte(blue, scanner.radix());
            Color color = new Color(red, green, blue);
            colormap.put(cutoff, color);
        }
    }

    static void checkUnsignedByte(short value, int radix) {
        if (value < 0 || value > 255) {
            throw new InputMismatchException(String.format("Value out of range. Value:\"%s\" Radix:%s", value, radix));
        }
    }

    IndexColorModel getIndexColorModel() {
        int[] cmap = new int[colormap.size() + 1];
        int i = 1;
        for (Map.Entry<Double, Color> entry : colormap.entrySet()) {
            Integer rgb = entry.getValue().getRGB();
            cmap[i++] = rgb;
        }
        return new IndexColorModel(8, cmap.length, cmap, 0, false, 0, DataBuffer.TYPE_BYTE);
    }

    int getRGB(double value) {
        if (Double.isNaN(value)) {
            return 0;
        }
        Map.Entry<Double, Color> entry = colormap.floorEntry(value);
        if (null == entry) {
            return 0;
        }
        return entry.getValue().getRGB();
    }

    public String getDataVariableName() {
        return dataVariableName;
    }
}
