package cydep;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Formatter;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestInput {

    @Test
    public void testLoadDPR() throws IOException {
        String pathname = Resources.getDPRPathname();
        Formatter errlog = new Formatter(System.err);
        Input input = new Input(pathname, Resources.DPRVARNAME, errlog);
        assertNotNull("null input", input);
        double distanceAxisGap = input.getDistanceAxisGap();
        assertEquals("distance axis gap mismatch", 250.0, distanceAxisGap, 0.000000001);
        int distanceAxisLength = input.getDistanceAxisLength();
        assertEquals("distance axis length", 920, distanceAxisLength);
        input.getValue(new Point2D.Double(0.0, 0.0));
        for (int i = 0; i < samples.length; i++) {
            Trial trial = samples[i];
            double value = input.getValue(trial.given);
            assertEquals("value mismatch i %s given %s,%s", trial.expected, value, 0.000000001);
        }
    }

    class Trial {

        final Point2D.Double given;
        final double expected;

        public Trial(double expected, double givenX, double givenY) {
            this.expected = expected;
            this.given = new Point2D.Double(givenX, givenY);
        }
    }
    Trial[] samples = {
        new Trial(Double.NaN, -125.0, 125.0),
        new Trial(Double.NaN, 125.0, 125.0),
        new Trial(Double.NaN, -125.0, -125.0),
        new Trial(Double.NaN, 125.0, -125.0)
    };
}
