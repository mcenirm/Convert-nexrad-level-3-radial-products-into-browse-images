package cydep;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Formatter;
import javax.imageio.ImageIO;
import org.junit.Test;
import ucar.nc2.NetcdfFileWriter;

public class TryGridImage {

    @Test
    public void tryGridNC() throws IOException {
        final String name = "Level3_KDVN_DPR_20130418_0408";
        final String pathname = Resources.getFileResourceAsPathname(name + ".nids");
        PrintStream out = System.out;
        Formatter f = new Formatter(out);
        Cydep1 cydep = new Cydep1(pathname, f);
        File outDir = new File(cydep.getPathname()).getParentFile();
        String imageFormat = "png";
        File gridFile = new File(outDir, name + "." + imageFormat);
        BufferedImage image = cydep.drawToGrid(imageFormat);
        ImageIO.write(image, imageFormat, gridFile);
    }
}
