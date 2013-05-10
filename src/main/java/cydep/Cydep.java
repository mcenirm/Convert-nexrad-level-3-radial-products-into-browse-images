package cydep;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.unidata.geoloc.EarthLocation;

public class Cydep {

    public static void drawToKml(String inputPathname, OutputStream out) throws IOException {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        GeodeticCalculator calc = new GeodeticCalculator();
        RadialDatasetSweep ncRadial = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(FeatureType.RADIAL, inputPathname, null, err);
        ncRadial.calcBounds();
        EarthLocation commonOrigin = ncRadial.getCommonOrigin();
        calc.setStartingGeographicPoint(commonOrigin.getLongitude(), commonOrigin.getLatitude());
        List<String> dataVariableNames = new LinkedList<String>();
        List<VariableSimpleIF> dataVariables = ncRadial.getDataVariables();
        for (VariableSimpleIF variableSimpleIF : dataVariables) {
            dataVariableNames.add(variableSimpleIF.getFullName());
        }
        NetcdfFile ncFile = ncRadial.getNetcdfFile();
        NetcdfDataset ncDataset = NetcdfDataset.wrap(ncFile, NetcdfDataset.getEnhanceAll());
        Variable dataVariable = null;
        for (String dataVariableName : dataVariableNames) {
            Variable variable = ncDataset.findVariable(dataVariableName);
            if (variable != null) {
                dataVariable = variable;
                break;
            }
        }
        if (dataVariable != null) {
            CoordinateAxis1D azimuthAxis = (CoordinateAxis1D) ncDataset.findCoordinateAxis(AxisType.RadialAzimuth);
            String azimuthName = azimuthAxis.getShortName();
            double[] azimuthBound1 = azimuthAxis.getBound1();
            double[] azimuthBound2 = azimuthAxis.getBound2();
            CoordinateAxis1D distanceAxis = (CoordinateAxis1D) ncDataset.findCoordinateAxis(AxisType.RadialDistance);
            String distanceName = distanceAxis.getShortName();
            double[] distanceBound1 = distanceAxis.getBound1();
            double[] distanceBound2 = distanceAxis.getBound2();
            Array data = dataVariable.read();
            Index index = data.getIndex();
            DefaultFeatureCollection collection = new DefaultFeatureCollection();
            Coordinate[] outline = new Coordinate[5];
            SimpleFeatureTypeBuilder simpleFeatureTypeBuilder = new SimpleFeatureTypeBuilder();
            simpleFeatureTypeBuilder.setName(dataVariable.getShortName());
            simpleFeatureTypeBuilder.add(azimuthName, String.class);
            simpleFeatureTypeBuilder.add(distanceName, String.class);
            simpleFeatureTypeBuilder.add("value", Float.class);
            simpleFeatureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84);
            simpleFeatureTypeBuilder.add("outline", Polygon.class);
            final SimpleFeatureType TYPE = simpleFeatureTypeBuilder.buildFeatureType();
            SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(TYPE);
            for (int iAzimuth = 0; iAzimuth < azimuthBound1.length; iAzimuth++) {
                double azimuthFrom = azimuthBound1[iAzimuth];
                if (azimuthFrom > 180.0) {
                    azimuthFrom -= 180.0;
                }
                double azimuthTo = azimuthBound2[iAzimuth];
                if (azimuthTo > 180.0) {
                    azimuthTo -= 180.0;
                }
                for (int iDistance = 0; iDistance < distanceBound1.length; iDistance++) {
                    float value = data.getFloat(index.set(iAzimuth, iDistance));
                    if (!Float.isNaN(value)) {
                        double distanceFrom = distanceBound1[iDistance];
                        double distanceTo = distanceBound2[iDistance];
                        calc.setDirection(azimuthFrom, distanceFrom);
                        Point2D dest = calc.getDestinationGeographicPoint();
                        outline[0] = new Coordinate(dest.getX(), dest.getY());
                        calc.setDirection(azimuthFrom, distanceTo);
                        dest = calc.getDestinationGeographicPoint();
                        outline[1] = new Coordinate(dest.getX(), dest.getY());
                        calc.setDirection(azimuthTo, distanceTo);
                        dest = calc.getDestinationGeographicPoint();
                        outline[2] = new Coordinate(dest.getX(), dest.getY());
                        calc.setDirection(azimuthTo, distanceFrom);
                        dest = calc.getDestinationGeographicPoint();
                        outline[3] = new Coordinate(dest.getX(), dest.getY());
                        outline[4] = outline[0];
                        Polygon polygon = geometryFactory.createPolygon(outline);
                        simpleFeatureBuilder.set(azimuthName, azimuthAxis.getCoordName(iAzimuth));
                        simpleFeatureBuilder.set(distanceName, distanceAxis.getCoordName(iDistance));
                        simpleFeatureBuilder.set("value", value);
                        simpleFeatureBuilder.set("outline", polygon);
                        SimpleFeature feature = simpleFeatureBuilder.buildFeature(null);
                        collection.add(feature);
                    }
                }
            }
            Encoder encoder = new Encoder(new KMLConfiguration());
            encoder.setIndenting(true);
            encoder.encode(collection, KML.kml, out);
        }
    }
    static Formatter err = new Formatter(System.err);

    public static void show(String label, Object object) {
        Util.dump(label, object, err, true);
    }
}
