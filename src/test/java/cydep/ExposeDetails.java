package cydep;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Formatter;
import java.util.List;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;

public class ExposeDetails {

    @Test
    public void exposeDetails() throws IOException {
        Cydep cydep = new Cydep(ExposeDetails.class.getClassLoader().getResource("Level3_KDVN_DPR_20130418_0408.nids").toString());
        PrintStream out = System.out;
        Formatter f = new Formatter(out);
        cydep.init(f);
        RadialDatasetSweep radialDatasetSweep = cydep.getRadialDatasetSweep();
        NetcdfDataset netcdfDataset = cydep.getNetcdfDataset();
        Util.dump("rds", radialDatasetSweep, f, true);
        Util.dump("ncd", netcdfDataset, f, true);
        List<Attribute> globalAttributes = netcdfDataset.getGlobalAttributes();
        for (Attribute attribute : globalAttributes) {
            Util.dump("attr", attribute, f, true);
        }
    }
}
