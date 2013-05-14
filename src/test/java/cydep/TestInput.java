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
        for (int i = 0; i < trials.length; i++) {
            Trial trial = trials[i];
            double value = input.getValue(trial.given);
            String msg = String.format("value mismatch i %s given %s,%s", i, trial.given.x, trial.given.y);
            try {
                assertEquals(msg, trial.expected, value, 0.0001);
            }
            catch (AssertionError e) {
                double azimuth = input.getAzimuthFromPoint(trial.given);
                double distance = input.getDistanceFromPoint(trial.given);
                int azimuthIndex = input.findAzimuthElement(azimuth);
                int distanceIndex = input.findDistanceElement(distance);
                errlog.format("index [%s][%s] azi %s dist %s\n", azimuthIndex, distanceIndex, azimuth, distance);
                throw e;
            }
        }
    }

    class Trial {

        final Point2D.Double given;
        final double expected;

        public Trial(double givenX, double givenY, double expected) {
            this.given = new Point2D.Double(givenX, givenY);
            this.expected = expected;
        }
    }
    Trial[] trials = {
        new Trial(-125.0, 125.0, Double.NaN),
        new Trial(125.0, 125.0, Double.NaN),
        new Trial(-125.0, -125.0, Double.NaN),
        new Trial(125.0, -125.0, Double.NaN),
        new Trial(143604.67, 119409.20, 0.307),
        new Trial(44728.02, -133149.69, 0.022),
        new Trial(-175200.99, -177720.80, Double.NaN),
        new Trial(-153018.22, -143054.83, 0.173),
        new Trial(189877.31, 118407.53, 0.064),
        new Trial(94767.80, -101465.10, 0.116),
        new Trial(-92945.35, 207099.19, Double.NaN),
        new Trial(-54490.16, -35230.11, 0.327),
        new Trial(159015.06, 141263.46, 0.082),
        new Trial(-224938.87, 91276.47, Double.NaN),
        new Trial(0.0, 0.0, Double.NaN)
    };
}
