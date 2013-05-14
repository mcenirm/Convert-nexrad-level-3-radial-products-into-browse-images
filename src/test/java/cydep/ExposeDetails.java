package cydep;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Formatter;
import java.util.List;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;

public class ExposeDetails {

    @Test
    public void exposeDetails() throws IOException {
        PrintStream out = System.out;
        Formatter f = new Formatter(out);
        final String pathname = ExposeDetails.class.getClassLoader().getResource("Level3_KDVN_DPR_20130418_0408.nids").toString();
        Cydep1 cydep = new Cydep1(pathname, f);
        RadialDatasetSweep radialDatasetSweep = cydep.getRadialDatasetSweep();
        NetcdfDataset netcdfDataset = cydep.getNetcdfDataset();
        Util.dump("rds", radialDatasetSweep, f, true);
        Util.dump("ncd", netcdfDataset, f, true);
        List<Attribute> globalAttributes = netcdfDataset.getGlobalAttributes();
        for (Attribute attribute : globalAttributes) {
            Util.dump("attr", attribute, f, true);
        }
        CoordinateAxis1D distanceAxis = cydep.getDistanceAxis();
        Util.dump("distance axis", distanceAxis, f, true);
        double[] v = distanceAxis.getCoordValues();
        double minD = v[0];
        double maxD = v[v.length - 1];
        int numD = v.length;
        double gapD = v[1] - v[0];
        f.format("# distance %s values from %s to %s with gap %s\n", numD, minD, maxD, gapD);
        int[] sampleIndexes = {0, 1, 2, 9, 10, 11, numD / 2, numD - 2, numD - 1};
        for (int i = 0; i < sampleIndexes.length; i++) {
            int index = sampleIndexes[i];
            double calc = minD + gapD * index;
            double sample = v[index];
            f.format("  %s %s %s\n", index, sample, calc);
        }
        CoordinateAxis1D azimuthAxis = cydep.getAzimuthAxis();
        Util.dump("azimuth axis", azimuthAxis, f, true);
        double[] a = azimuthAxis.getCoordValues();
        double minA = a[0];
        double maxA = a[a.length - 1];
        int numA = a.length;
        double gapA = a[1] - a[0];
        f.format("# azimuth %s values from %s to %s with gap %s\n", numA, minA, maxA, gapA);
        int[] sampleAzimuthIndexes = {0, 1, 2, 9, 10, 11, numA / 2, numA - 2, numA - 1};
        for (int i = 0; i < sampleAzimuthIndexes.length; i++) {
            int index = sampleAzimuthIndexes[i];
            double calc = minA + gapA * index;
            double sample = a[index];
            f.format("  %s %s %s\n", index, sample, calc);
        }
    }
}
