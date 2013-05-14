package cydep;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Formatter;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public final class Input {

    private final String pathname;
    private final String dataVariableName;
    private final Formatter errlog;
    private final RadialDatasetSweep radialDataSweep;
    private final NetcdfFile netcdfFile;
    private final NetcdfDataset netcdfDataset;
    private final CoordinateAxis1D azimuthAxis;
    private final CoordinateAxis1D distanceAxis;
    private final Variable azimuthVariable;
    private final Variable distanceVariable;
    private final Variable dataVariable;
    private final Array dataArray;

    public Input(String pathname, String dataVariableName, Formatter errlog) throws IOException {
        this.pathname = pathname;
        this.dataVariableName = dataVariableName;
        this.errlog = errlog;
        this.radialDataSweep = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(FeatureType.RADIAL, pathname, null, errlog);
        this.netcdfFile = radialDataSweep.getNetcdfFile();
        this.netcdfDataset = NetcdfDataset.wrap(netcdfFile, NetcdfDataset.getEnhanceAll());
        this.azimuthAxis = (CoordinateAxis1D) netcdfDataset.findCoordinateAxis(AxisType.RadialAzimuth);
        this.distanceAxis = (CoordinateAxis1D) netcdfDataset.findCoordinateAxis(AxisType.RadialDistance);
        this.azimuthVariable = netcdfDataset.findVariable(azimuthAxis.getFullName());
        this.distanceVariable = netcdfDataset.findVariable(distanceAxis.getFullName());
        this.dataVariable = netcdfDataset.findVariable(dataVariableName);
        this.dataArray = dataVariable.read();
    }

    int getDistanceAxisLength() {
        return distanceAxis.getShape(0);
    }

    double getDistanceAxisGap() {
        return distanceAxis.getCoordValue(1) - distanceAxis.getCoordValue(0);
    }

    double getValue(Point2D ptDst) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
