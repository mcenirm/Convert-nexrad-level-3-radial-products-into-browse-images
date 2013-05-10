package cydep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

public class TestCydep {

    public static final String NAME = "Level3_KDVN_DPR_20130418_0408";

    @Test
    public void testDPR() throws IOException, URISyntaxException {
        String dpr = TestCydep.class.getClassLoader().getResource(NAME + ".nids").toString();
        File dir = new File(new URI(dpr)).getParentFile();
        final File file = new File(dir, NAME + ".kml");
        final FileOutputStream out = new FileOutputStream(file);
        Cydep.drawToKml(dpr, out);
    }
}
