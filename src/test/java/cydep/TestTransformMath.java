package cydep;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTransformMath {

    @Test
    public void gogo() {
        int distanceAxisLength = 5;
        double distanceGap = 250.0;
        Trial[] trials = {
            new Trial(0, 0, -1125.0, 1125.0),
            new Trial(1, 0, -875.0, 1125.0),
            new Trial(2, 0, -625.0, 1125.0),
            new Trial(3, 0, -375.0, 1125.0),
            new Trial(4, 0, -125.0, 1125.0),
            new Trial(5, 0, 125.0, 1125.0),
            new Trial(6, 0, 375.0, 1125.0),
            new Trial(7, 0, 625.0, 1125.0),
            new Trial(8, 0, 875.0, 1125.0),
            new Trial(9, 0, 1125.0, 1125.0),
            new Trial(3, 1, -375.0, 875.0),
            new Trial(3, 2, -375.0, 625.0),
            new Trial(3, 3, -375.0, 375.0),
            new Trial(3, 4, -375.0, 125.0),
            new Trial(4, 4, -125.0, 125.0),
            new Trial(5, 4, 125.0, 125.0),
            new Trial(6, 4, 375.0, 125.0),
            new Trial(0, 9, -1125.0, -1125.0),
            new Trial(1, 9, -875.0, -1125.0),
            new Trial(2, 9, -625.0, -1125.0),
            new Trial(3, 9, -375.0, -1125.0),
            new Trial(4, 9, -125.0, -1125.0),
            new Trial(5, 9, 125.0, -1125.0),
            new Trial(6, 9, 375.0, -1125.0),
            new Trial(7, 9, 625.0, -1125.0),
            new Trial(8, 9, 875.0, -1125.0),
            new Trial(9, 9, 1125.0, -1125.0)
        };
        List<Trial> trialsAsList = Arrays.asList(trials);
        Collections.shuffle(trialsAsList);
        trialsAsList.toArray(trials);
        AffineTransform at = new AffineTransform();
        at.scale(distanceGap, -distanceGap);
        at.translate(0.5 - distanceAxisLength, 0.5 - distanceAxisLength);
        for (int i = 0; i < trials.length; i++) {
            Trial trial = trials[i];
            Point2D ptDst = new Point2D.Double();
            at.transform(trial.given, ptDst);
            assertEquals(String.format("mismatch trial i %s given %s,%s", i, trial.given.x, trial.given.y), trial.expect, ptDst);
        }
    }

    class Trial {

        Point given;
        Point2D.Double expect;

        public Trial(int givenX, int givenY, double expectX, double expectY) {
            this.given = new Point(givenX, givenY);
            this.expect = new Point2D.Double(expectX, expectY);
        }
    }
}
