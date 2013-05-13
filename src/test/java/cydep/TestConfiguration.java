package cydep;

import java.awt.image.IndexColorModel;
import java.io.FileNotFoundException;
import static org.junit.Assert.*;
import org.junit.Test;

public class TestConfiguration {

    static final String DPRNAME = "Level3_KDVN_DPR_20130418_0408";

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
    }
}
