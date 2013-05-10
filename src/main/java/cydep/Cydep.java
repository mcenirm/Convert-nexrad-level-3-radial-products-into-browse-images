package cydep;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.StyleBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
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

    private String pathname;
    private RadialDatasetSweep radialDatasetSweep;
    private NetcdfDataset netcdfDataset;

    /**
     * @return the pathname
     */
    public String getPathname() {
        return pathname;
    }

    /**
     * @return the radialDatasetSweep
     */
    public RadialDatasetSweep getRadialDatasetSweep() {
        return radialDatasetSweep;
    }

    /**
     * @return the netcdfDataset
     */
    public NetcdfDataset getNetcdfDataset() {
        return netcdfDataset;
    }

    public Cydep(String pathname) {
        this.pathname = pathname;
    }

    public void init(Formatter err) throws IOException {
        this.radialDatasetSweep = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(FeatureType.RADIAL, getPathname(), null, err);
        getRadialDatasetSweep().calcBounds();
        this.netcdfDataset = NetcdfDataset.wrap(getRadialDatasetSweep().getNetcdfFile(), NetcdfDataset.getEnhanceAll());
    }

    public static void drawToPng(String inputPathname, OutputStream out) throws IOException {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        GeodeticCalculator calc = new GeodeticCalculator(DefaultGeographicCRS.WGS84);
        Cydep cydep = new Cydep(inputPathname);
        cydep.init(err);
        RadialDatasetSweep ncRadial = cydep.getRadialDatasetSweep();
        EarthLocation commonOrigin = ncRadial.getCommonOrigin();
        calc.setStartingGeographicPoint(commonOrigin.getLongitude(), commonOrigin.getLatitude());
        List<String> dataVariableNames = new LinkedList<String>();
        List<VariableSimpleIF> dataVariables = ncRadial.getDataVariables();
        for (VariableSimpleIF variableSimpleIF : dataVariables) {
            dataVariableNames.add(variableSimpleIF.getFullName());
        }
        NetcdfDataset ncDataset = cydep.getNetcdfDataset();
        Variable dataVariable = null;
        for (String dataVariableName : dataVariableNames) {
            Variable variable = ncDataset.findVariable(dataVariableName);
            if (variable != null) {
                dataVariable = variable;
                break;
            }
        }
        DefaultFeatureCollection collection = new DefaultFeatureCollection();
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
                    azimuthFrom -= 360.0;
                }
                double azimuthTo = azimuthBound2[iAzimuth];
                if (azimuthTo > 180.0) {
                    azimuthTo -= 360.0;
                }
                //                for (int iDistance = 0; iDistance < distanceBound1.length; iDistance++) {
                int iDistance = distanceBound1.length / 2;
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
                    err.format("%s %s\n", value, polygon);
                    simpleFeatureBuilder.set(azimuthName, azimuthAxis.getCoordName(iAzimuth));
                    simpleFeatureBuilder.set(distanceName, distanceAxis.getCoordName(iDistance));
                    simpleFeatureBuilder.set("value", value);
                    simpleFeatureBuilder.set("outline", polygon);
                    SimpleFeature feature = simpleFeatureBuilder.buildFeature(null);
                    collection.add(feature);
                }
//        }
            }
        }
        MapContent map = new MapContent();
        StyleBuilder styleBuilder = new StyleBuilder();
        PolygonSymbolizer symbolizer = styleBuilder.createPolygonSymbolizer(styleBuilder.createStroke(Color.BLACK), styleBuilder.createFill(Color.GRAY));
        map.addLayer(new FeatureLayer(collection, styleBuilder.createStyle(symbolizer)));
        GTRenderer render = new StreamingRenderer();
        render.setMapContent(map);
        int width = 500;
        int height = 500;
        int imageType = BufferedImage.TYPE_INT_RGB;
        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(Color.BLUE);
        final Rectangle imageBounds = new Rectangle(0, 0, width, height);
        graphics.fill(imageBounds);
        Rectangle paintArea = imageBounds;
        float lat_min = 0, lat_max = 0, lon_min = 0, lon_max = 0;
        List<Attribute> globalAttributes = ncDataset.getGlobalAttributes();
        for (Attribute attribute : globalAttributes) {
            if ("geospatial_lat_min".equals(attribute.getShortName())) {
                lat_min = attribute.getNumericValue().floatValue();
            } else if ("geospatial_lat_max".equals(attribute.getShortName())) {
                lat_max = attribute.getNumericValue().floatValue();
            } else if ("geospatial_lon_min".equals(attribute.getShortName())) {
                lon_min = attribute.getNumericValue().floatValue();
            } else if ("geospatial_lon_max".equals(attribute.getShortName())) {
                lon_max = attribute.getNumericValue().floatValue();
            }
        }
        Envelope mapArea = new ReferencedEnvelope(lon_min, lon_max, lat_min, lat_max, DefaultGeographicCRS.WGS84);
        show("map area", mapArea);
        render.paint(graphics, paintArea, mapArea);
        ImageIO.write(image, "png", out);
    }
    static Formatter err = new Formatter(System.err);

    public static void show(String label, Object object) {
        Util.dump(label, object, err, true);
    }
}
