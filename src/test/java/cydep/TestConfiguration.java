package cydep;

import java.awt.image.IndexColorModel;
import java.io.FileNotFoundException;
import static org.junit.Assert.*;
import org.junit.Test;

public class TestConfiguration {

    @Test
    public void testLoadDPR() throws FileNotFoundException {
        final String configPathname = Resources.getFileResourceAsPathname("DPR.conf");
        final Configuration config = new Configuration(configPathname);
        final String dataVariableName = config.getDataVariableName();
        assertEquals("data variable name from configuration is wrong", "DigitalInstantaneousPrecipitationRate", dataVariableName);
        final IndexColorModel indexColorModel = config.getIndexColorModel();
        final String ICMFC = "index color model from configuration";
        assertNotNull("null " + ICMFC, indexColorModel);
        assertTrue(ICMFC + " is not valid", indexColorModel.isValid());
        assertEquals(ICMFC + " map size is wrong", 11, indexColorModel.getMapSize());
        assertEquals(ICMFC + " transparent index is wrong", 0, indexColorModel.getTransparentPixel());
        assertEquals(ICMFC + " wrong rgb ", 0, config.getRGB(0));
        for (int i = 0; i < trials.length; i++) {
            Trial trial = trials[i];
            int rgb = config.getRGB(trial.given);
            assertEquals(String.format(ICMFC + " wrong rgb i %s given %s", i, trial.given), trial.expected, rgb);
        }
    }

    class Trial {

        final double given;
        final int expected;

        public Trial(double given, int expected) {
            this.given = given;
            this.expected = expected;
        }
    }
    final Trial[] trials = {
        new Trial(0.0, 0x00000000),
        new Trial(0.001, 0x00bfffe9),
        new Trial(0.01, 0x0050D2FA),
        new Trial(0.1, 0x00DDFF99),
        new Trial(1.0, 0x00AAFF00),
        new Trial(2.0, 0x00FFFF70),
        new Trial(3.0, 0x00F7E300),
        new Trial(4.0, 0x00E69900),
        new Trial(5.0, 0x00F02F22),
        new Trial(6.0, 0x00AB0000),
        new Trial(7.0, 0x00362500),
        new Trial(1000000.0, 0x00362500)
    };
}
