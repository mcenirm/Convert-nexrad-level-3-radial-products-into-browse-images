package cydep;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Formatter;
import org.junit.Test;
import ucar.nc2.NetcdfFileWriter;

public class TryGridNC {

    @Test
    public void tryGridNC() throws IOException {
        final String name = "Level3_KDVN_DPR_20130418_0408";
        final String pathname = Resources.getFileResourceAsPathname(name + ".nids");
        PrintStream out = System.out;
        Formatter f = new Formatter(out);
        Cydep cydep = new Cydep(pathname, f);
        File outDir = new File(cydep.getPathname()).getParentFile();
        File gridFile = new File(outDir, name + ".grid.nc");
        NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, gridFile.getPath());
        cydep.drawToGrid(writer);
        writer.close();
    }
}
