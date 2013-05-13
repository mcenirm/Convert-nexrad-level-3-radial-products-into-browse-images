package cydep;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
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
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.unidata.geoloc.EarthLocation;

public class Cydep1 {

    private String pathname;
    private RadialDatasetSweep radialDatasetSweep;
    private NetcdfDataset netcdfDataset;
    private Variable dataVariable;
    private CoordinateAxis1D azimuthAxis;
    private CoordinateAxis1D distanceAxis;

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

    public Cydep1(String pathname, Formatter errlog) throws IOException {
        this.pathname = pathname;
        this.radialDatasetSweep = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(FeatureType.RADIAL, this.pathname, null, errlog);
        this.radialDatasetSweep.calcBounds();
        this.netcdfDataset = NetcdfDataset.wrap(this.radialDatasetSweep.getNetcdfFile(), NetcdfDataset.getEnhanceAll());
        List<String> dataVariableNames = new LinkedList<String>();
        List<VariableSimpleIF> dataVariables = getRadialDatasetSweep().getDataVariables();
        for (VariableSimpleIF variableSimpleIF : dataVariables) {
            dataVariableNames.add(variableSimpleIF.getFullName());
        }
        for (String dataVariableName : dataVariableNames) {
            Variable variable = getNetcdfDataset().findVariable(dataVariableName);
            if (variable != null) {
                this.dataVariable = variable;
                break;
            }
        }
        azimuthAxis = (CoordinateAxis1D) getNetcdfDataset().findCoordinateAxis(AxisType.RadialAzimuth);
        distanceAxis = (CoordinateAxis1D) getNetcdfDataset().findCoordinateAxis(AxisType.RadialDistance);
    }

    public Variable getDataVariable() {
        return dataVariable;
    }

    public CoordinateAxis1D getAzimuthAxis() {
        return azimuthAxis;
    }

    public CoordinateAxis1D getDistanceAxis() {
        return distanceAxis;
    }

    public static void drawToPng(String inputPathname, OutputStream out) throws IOException {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        GeodeticCalculator calc = new GeodeticCalculator(DefaultGeographicCRS.WGS84);
        Cydep1 cydep = new Cydep1(inputPathname, err);
        RadialDatasetSweep ncRadial = cydep.getRadialDatasetSweep();
        EarthLocation commonOrigin = ncRadial.getCommonOrigin();
        calc.setStartingGeographicPoint(commonOrigin.getLongitude(), commonOrigin.getLatitude());
        NetcdfDataset ncDataset = cydep.getNetcdfDataset();
        Variable dataVariable = cydep.getDataVariable();
        DefaultFeatureCollection collection = new DefaultFeatureCollection();
        if (dataVariable != null) {
            CoordinateAxis1D azimuthAxis = cydep.getAzimuthAxis();
            String azimuthName = azimuthAxis.getShortName();
            double[] azimuthBound1 = azimuthAxis.getBound1();
            double[] azimuthBound2 = azimuthAxis.getBound2();
            CoordinateAxis1D distanceAxis = cydep.getDistanceAxis();
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

    void drawToMeteredImage(File imageFile, String imageFormat) throws IOException {
        float lat_min = findFloatGlobalAttribute("geospatial_lat_min");
        float lat_max = findFloatGlobalAttribute("geospatial_lat_max");
        float lon_min = findFloatGlobalAttribute("geospatial_lon_min");
        float lon_max = findFloatGlobalAttribute("geospatial_lon_max");
        float lat_height = lat_max - lat_min;
        float lon_width = lon_max - lon_min;
        double[] azimuthFroms = azimuthAxis.getBound1();
        double[] azimuthTos = azimuthAxis.getBound2();
        double[] distanceFroms = distanceAxis.getBound1();
        double[] distanceTos = distanceAxis.getBound2();
        int width = 2500;
        int height = width;
        double distanceScale = width / distanceTos[distanceTos.length - 1] / 2;
        int imageType = BufferedImage.TYPE_4BYTE_ABGR;
        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D g = image.createGraphics();
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, width, height);
        g.setPaint(Color.GREEN);
        g.drawOval(0, 0, width, height);
        g.translate(width / 2, height / 2);
        float data_min = findFloatGlobalAttribute("data_min");
        float data_max = findFloatGlobalAttribute("data_max");
        err.format("# data min: %s\n", data_min);
        err.format("# data max: %s\n", data_max);
        Color[] colors = new Color[10];
        for (int i = 0; i < colors.length; i++) {
            Color color = Color.getHSBColor(0.9f * i * colors.length, 0.5f, 1.0f);
            colors[i] = color;
        }
        Array data = dataVariable.read();
        float[] sortme = (float[]) data.copyTo1DJavaArray();
        Arrays.sort(sortme);
        int lastGoodValueIndex = sortme.length;
        while (lastGoodValueIndex > 1) {
            lastGoodValueIndex--;
            if (!Float.isNaN(sortme[lastGoodValueIndex])) {
                break;
            }
        }
        float value_max = sortme[lastGoodValueIndex];
        Float zero = Float.valueOf(0.0f);
        int firstGoodValueIndex = 0;
        while (firstGoodValueIndex < sortme.length) {
            if (zero.compareTo(sortme[firstGoodValueIndex]) < 0) {
                break;
            }
            firstGoodValueIndex++;
        }
        float value_min = sortme[(int) (firstGoodValueIndex + 0.1 * (lastGoodValueIndex - firstGoodValueIndex))];
        err.format("# value min: %s\n", value_min);
        err.format("# value max: %s\n", value_max);
        Index index = data.getIndex();
        for (int iAzimuth = 0; iAzimuth < azimuthFroms.length; iAzimuth++) {
            double azimuthFrom = azimuthFroms[iAzimuth];
            double azimuthTo = azimuthTos[iAzimuth];
            double radiansFrom = degreesToRadians(azimuthFrom);
            double radiansTo = degreesToRadians(azimuthTo);
            double sinFrom = Math.sin(radiansFrom);
            double sinTo = Math.sin(radiansTo);
            double cosFrom = Math.cos(radiansFrom);
            double cosTo = Math.cos(radiansTo);
            for (int iDistance = 0; iDistance < distanceFroms.length; iDistance++) {
                float value = data.getFloat(index.set(iAzimuth, iDistance));
                if (!Float.isNaN(value) && value > value_min) {
                    double distanceFrom = distanceFroms[iDistance] * distanceScale;
                    double distanceTo = distanceTos[iDistance] * distanceScale;
                    GeneralPath path = new GeneralPath();
                    double x1 = distanceFrom * sinFrom;
                    double y1 = distanceFrom * cosFrom;
                    double x2 = distanceTo * sinFrom;
                    double y2 = distanceTo * cosFrom;
                    double x3 = distanceTo * sinTo;
                    double y3 = distanceTo * cosTo;
                    double x4 = distanceFrom * sinTo;
                    double y4 = distanceFrom * cosTo;
                    path.moveTo(x1, y1);
                    path.lineTo(x2, y2);
                    path.lineTo(x3, y3);
                    path.lineTo(x4, y4);
                    path.lineTo(x1, y1);
                    final int colorIndex = (int) ((colors.length - 1) * (value - value_min) / (value_max - value_min));
                    g.setPaint(colors[colorIndex]);
                    g.fill(path);
                }
            }
        }
        ImageIO.write(image, imageFormat, imageFile);
    }

    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    private float findFloatGlobalAttribute(final String attributeName) {
        return getNetcdfDataset().findGlobalAttribute(attributeName).getNumericValue().floatValue();
    }

    public void drawToGrid(NetcdfFileWriter writer) throws IOException {
        double[] distanceValues = distanceAxis.getCoordValues();
        int length = 2 * distanceValues.length;
        Dimension geoX = writer.addDimension(null, AxisType.GeoX.getCFAxisName(), length);
        Dimension geoY = writer.addDimension(null, AxisType.GeoY.getCFAxisName(), length);
        List<Dimension> dimensions = new LinkedList<Dimension>();
        dimensions.add(geoX);
        dimensions.add(geoY);
        Variable geoXVar = writer.addVariable(null, geoX.getShortName(), distanceAxis.getDataType(), geoX.getShortName());

        Variable gridVariable = writer.addVariable(null, dataVariable.getShortName(), dataVariable.getDataType(), dimensions);
        writer.create();
    }

    BufferedImage drawToGrid(String imageFormat) {
        double[] distanceValues = distanceAxis.getCoordValues();
        int length = 2 * distanceValues.length;
        return null;
    }

    BufferedImage drawUnmapped() throws IOException {
        int bits = 8;
        int size = 16;
        byte[] r = new byte[size];
        byte[] g = new byte[size];
        byte[] b = new byte[size];
        float[] cutoffs = new float[10];
        int trans = 0;
        r[0] = (byte) 0;
        g[0] = (byte) 0;
        b[0] = (byte) 0;
        cutoffs[0] = 0.001f;
        r[1] = (byte) 191;
        g[1] = (byte) 255;
        b[1] = (byte) 233;
        cutoffs[1] = 0.01f;
        r[2] = (byte) 80;
        g[2] = (byte) 210;
        b[2] = (byte) 250;
        cutoffs[2] = 0.1f;
        r[3] = (byte) 221;
        g[3] = (byte) 255;
        b[3] = (byte) 153;
        cutoffs[3] = 1.0f;
        r[4] = (byte) 170;
        g[4] = (byte) 255;
        b[4] = (byte) 0;
        cutoffs[4] = 2.0f;
        r[5] = (byte) 255;
        g[5] = (byte) 255;
        b[5] = (byte) 112;
        cutoffs[5] = 3.0f;
        r[6] = (byte) 247;
        g[6] = (byte) 227;
        b[6] = (byte) 0;
        cutoffs[6] = 4.0f;
        r[7] = (byte) 230;
        g[7] = (byte) 153;
        b[7] = (byte) 0;
        cutoffs[7] = 5.0f;
        r[8] = (byte) 240;
        g[8] = (byte) 47;
        b[8] = (byte) 34;
        cutoffs[8] = 6.0f;
        r[9] = (byte) 171;
        g[9] = (byte) 0;
        b[9] = (byte) 0;
        cutoffs[9] = 7.0f;
        r[10] = (byte) 54;
        g[10] = (byte) 37;
        b[10] = (byte) 0;
//        r[11] = (byte) 0;
//        g[11] = (byte) 0;
//        b[11] = (byte) 0;
//        r[12] = (byte) 0;
//        g[12] = (byte) 0;
//        b[12] = (byte) 0;
//        r[13] = (byte) 0;
//        g[13] = (byte) 0;
//        b[13] = (byte) 0;
//        r[14] = (byte) 0;
//        g[14] = (byte) 0;
//        b[14] = (byte) 0;
//        r[15] = (byte) 0;
//        g[15] = (byte) 0;
//        b[15] = (byte) 0;
        int[] rgb = new int[size];
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = b[i] + (g[i] + (r[i]) << bits) << bits;
        }
        IndexColorModel cm = new IndexColorModel(bits, size, r, g, b, trans);
        int height = distanceAxis.getShape(0);
        int width = azimuthAxis.getShape(0);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, cm);
        Array data = dataVariable.read();
        Index index = data.getIndex();
        float minV = Float.MAX_VALUE;
        float maxV = Float.MIN_VALUE;
        int minI = Integer.MAX_VALUE;
        int maxI = Integer.MIN_VALUE;
        for (int iAzimuth = 0; iAzimuth < width; iAzimuth++) {
            for (int iDistance = 0; iDistance < height; iDistance++) {
                float value = data.getFloat(index.set(iAzimuth, iDistance));
                if (!Float.isNaN(value)) {
                    int i = 0;
                    while (i < cutoffs.length && value > cutoffs[i]) {
                        i++;
                    }
                    err.format("value %s i %s\n", value, i);
                    image.setRGB(iAzimuth, iDistance, rgb[i]);
                    if (value > 0.0f) {
                        if (value > maxV) {
                            maxV = value;
                            maxI = i;
                        }
                        if (value < minV) {
                            minV = value;
                            minI = i;
                        }
                    }
                }
            }
        }
        err.format("min value %s i %s\n", minV, minI);
        err.format("max value %s i %s\n", maxV, maxI);
        return image;
    }
}
