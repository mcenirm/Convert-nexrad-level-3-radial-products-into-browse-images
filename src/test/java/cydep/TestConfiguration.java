package cydep;

import java.awt.Color;
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
        new Trial(0.0, 0),
        new Trial(Double.NaN, 0),
        new Trial(0.001, new Color(191, 255, 233).getRGB()),
        new Trial(0.01, new Color(80, 210, 250).getRGB()),
        new Trial(0.1, new Color(221, 255, 153).getRGB()),
        new Trial(1.0, new Color(170, 255, 0).getRGB()),
        new Trial(2.0, new Color(255, 255, 112).getRGB()),
        new Trial(3.0, new Color(247, 227, 0).getRGB()),
        new Trial(4.0, new Color(230, 153, 0).getRGB()),
        new Trial(5.0, new Color(240, 47, 34).getRGB()),
        new Trial(6.0, new Color(171, 0, 0).getRGB()),
        new Trial(7.0, new Color(54, 37, 0).getRGB()),
        new Trial(0.0, 0x00000000),
        new Trial(0.0011, 0xffbfffe9),
        new Trial(0.011, 0xFF50D2FA),
        new Trial(0.11, 0xFFDDFF99),
        new Trial(1.01, 0xFFAAFF00),
        new Trial(2.01, 0xFFFFFF70),
        new Trial(3.01, 0xFFF7E300),
        new Trial(4.01, 0xFFE69900),
        new Trial(5.01, 0xFFF02F22),
        new Trial(6.01, 0xFFAB0000),
        new Trial(7.01, 0xFF362500),
        new Trial(1000000.0, 0xFF362500)
    };
}
