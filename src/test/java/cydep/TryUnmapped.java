package cydep;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Formatter;
import javax.imageio.ImageIO;
import org.junit.Test;

public class TryUnmapped {

    @Test
    public void tryUnmapped() throws IOException {
        final String name = "Level3_KDVN_DPR_20130418_0408";
        final String pathname = Resources.getFileResourceAsPathname(name + ".nids");
        PrintStream out = System.out;
        Formatter f = new Formatter(out);
        Cydep cydep = new Cydep(pathname, f);
        File outDir = new File(cydep.getPathname()).getParentFile();
        String imageFormat = "png";
        File gridFile = new File(outDir, name + ".unmapped." + imageFormat);
        BufferedImage image = cydep.drawUnmapped();
        ImageIO.write(image, imageFormat, gridFile);
    }
}
