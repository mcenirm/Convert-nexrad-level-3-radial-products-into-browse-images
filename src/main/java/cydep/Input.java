package cydep;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Formatter;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

final class Input {

    private final String pathname;
    private final String dataVariableName;
    private final Formatter errlog;
    private final RadialDatasetSweep radialDataSweep;
    private final NetcdfFile netcdfFile;
    private final NetcdfDataset netcdfDataset;
    private final CoordinateAxis1D azimuthAxis;
    private final CoordinateAxis1D distanceAxis;
    private final int distanceAxisLength;
    private final Variable dataVariable;
    private final Array dataArray;

    Input(String pathname, String dataVariableName, Formatter errlog) throws IOException {
        this.pathname = pathname;
        this.dataVariableName = dataVariableName;
        this.errlog = errlog;
        this.radialDataSweep = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(FeatureType.RADIAL, pathname, null, errlog);
        this.netcdfFile = radialDataSweep.getNetcdfFile();
        this.netcdfDataset = NetcdfDataset.wrap(netcdfFile, NetcdfDataset.getEnhanceAll());
        this.azimuthAxis = (CoordinateAxis1D) netcdfDataset.findCoordinateAxis(AxisType.RadialAzimuth);
        this.distanceAxis = (CoordinateAxis1D) netcdfDataset.findCoordinateAxis(AxisType.RadialDistance);
        this.distanceAxisLength = distanceAxis.getShape(0);
        this.dataVariable = netcdfDataset.findVariable(dataVariableName);
        this.dataArray = dataVariable.read();
    }

    int getDistanceAxisLength() {
        return distanceAxisLength;
    }

    double getDistanceAxisGap() {
        return distanceAxis.getCoordValue(1) - distanceAxis.getCoordValue(0);
    }

    double getValue(Point2D cartesian) {
        final double distance = getDistanceFromPoint(cartesian);
        final double azimuth = getAzimuthFromPoint(cartesian);
        final int distanceIndex = findDistanceElement(distance);
        final int azimuthIndex = findAzimuthElement(azimuth);
        return getValue(azimuthIndex, distanceIndex);
    }

    double getAzimuthFromPoint(Point2D cartesian) {
        /*
         * x and y are swapped because atan2 assume counter-clockwise from 
         * positive x-axis, but compass is clockwise from north
         */
        final double azimuth = (360.0 + Math.toDegrees(Math.atan2(cartesian.getX(), cartesian.getY()))) % 360.0;
        return azimuth;
    }

    double getDistanceFromPoint(Point2D cartesian) {
        final double distance = cartesian.distance(0, 0);
        return distance;
    }

    int findDistanceElement(double distance) {
        return distanceAxis.findCoordElement(distance);
    }

    int findAzimuthElement(double azimuth) {
        return azimuthAxis.findCoordElement(azimuth);
    }

    double getValue(int azimuthIndex, int distanceIndex) {
        final Index index = dataArray.getIndex();
        try {
            final double value = dataArray.getDouble(index.set(azimuthIndex, distanceIndex));
            return value;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return Double.NaN;
        }
    }
}
