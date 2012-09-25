package ch.uzh.geo.webgen.wps.server;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Envelope;

import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.AttributeType;

import ica.wps.common.IWPSXmlObjectParser;
import ica.wps.data.WPSDatatypes;
import ica.wps.data.WPSDatatypes.*;

/**
 * Implementation of the xml parser to define jump features.
 * @author	M. Wittensoeldner
 * @date	Created on 08.02.2007
 */
public class WPSXmlObjectParser implements IWPSXmlObjectParser {

	protected GeometryFactory		_factory = new GeometryFactory();
	
	/**
	 * Parses the content of the whole xml tree. This method is called when the xml content is unknown. e.g. complex feature attribute.
	 * When no unknown data is available, this method has not been to implement.
	 * @return Object							The parsed object.
	 * @param rootNode							The root node of the xml tree.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseNode(Node rootNode) throws Exception {
		throw new Exception(this.getClass().getSimpleName()+".parseNode(): method not supported.");
	}
	
	/**
	 * Parses a point geometry.
	 * @return Object							The parsed object.
	 * @param point								The point geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parsePoint(WPSPoint point) throws Exception {
		return _factory.createPoint(this.parseCoordinate(point._coord));
	}

	/**
	 * Parses a line string geometry.
	 * @return Object							The parsed object.
	 * @param lineString						The line string geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseLineString(WPSLineString lineString) throws Exception {
		return _factory.createLineString(this.parseCoordinates(lineString._lstCoord));
	}

	/**
	 * Parses a polygon geometry.
	 * @return Object							The parsed object.
	 * @param polygon							The polygon geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parsePolygon(WPSPolygon polygon) throws Exception {
		Coordinate[] arrCoordOut = this.parseCoordinates(polygon._lstCoordOuterBoundary);
		LinearRing[] arrHoles = null;
		if (polygon._lstCoordInnerBoundaries != null) {
			arrHoles = new LinearRing[polygon._lstCoordInnerBoundaries.size()];
			int i = 0;
			Iterator<List<WPSCoordinate>> iterCoord = polygon._lstCoordInnerBoundaries.iterator();
			while (iterCoord.hasNext())
				arrHoles[i++] = _factory.createLinearRing(this.parseCoordinates(iterCoord.next()));
		}
		return _factory.createPolygon(_factory.createLinearRing(arrCoordOut), arrHoles);
	}

	/**
	 * Parses a multi point geometry.
	 * @return Object							The parsed object.
	 * @param multiPoint						The multi point geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseMultiPoint(WPSMultiPoint multiPoint) throws Exception {
		Point[] arrPoint = new Point[multiPoint._lstPoint.size()];
		int i = 0;
		Iterator<WPSPoint> iterPoint = multiPoint._lstPoint.iterator();
		while (iterPoint.hasNext())
			arrPoint[i++] = (Point)this.parsePoint(iterPoint.next());
		return _factory.createMultiPoint(arrPoint);
	}

	/**
	 * Parses a multi line string geometry.
	 * @return Object							The parsed object.
	 * @param multiLineString					The multi line string geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseMultiLineString(WPSMultiLineString multiLineString) throws Exception {
		LineString[] arrLineString = new LineString[multiLineString._lstLineString.size()];
		int i = 0;
		Iterator<WPSLineString> iterLineString = multiLineString._lstLineString.iterator();
		while (iterLineString.hasNext())
			arrLineString[i++] = (LineString)this.parseLineString(iterLineString.next());
		return _factory.createMultiLineString(arrLineString);
	}

	/**
	 * Parses a multi polygon geometry.
	 * @return Object							The parsed object.
	 * @param multiPolygon						The multi polygon geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseMultiPolygon(WPSMultiPolygon multiPolygon) throws Exception {
		Polygon[] arrPolygon = new Polygon[multiPolygon._lstPolygon.size()];
		int i = 0;
		Iterator<WPSPolygon> iterPolygon = multiPolygon._lstPolygon.iterator();
		while (iterPolygon.hasNext())
			arrPolygon[i++] = (Polygon)this.parsePolygon(iterPolygon.next());
		return _factory.createMultiPolygon(arrPolygon);
	}

	/**
	 * Parses a geometry collection.
	 * @return Object							The parsed object.
	 * @param geometryCollection				The geometry collection.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseGeometryCollection(WPSGeometryCollection geometryCollection) throws Exception {
		Geometry[] arrGeom = new Geometry[geometryCollection._lstGeometry.size()];
		int i = 0;
		WPSGeometry geom;
		Iterator<WPSGeometry> iterGeom = geometryCollection._lstGeometry.iterator();
		while (iterGeom.hasNext()) {
			geom = iterGeom.next();
			if (geom instanceof WPSPoint)
				arrGeom[i++] = (Geometry)this.parsePoint((WPSPoint)geom);
			else if (geom instanceof WPSLineString)
				arrGeom[i++] = (Geometry)this.parseLineString((WPSLineString)geom);
			else if (geom instanceof WPSPolygon)
				arrGeom[i++] = (Geometry)this.parsePolygon((WPSPolygon)geom);
			else if (geom instanceof WPSMultiPoint)
				arrGeom[i++] = (Geometry)this.parseMultiPoint((WPSMultiPoint)geom);
			else if (geom instanceof WPSMultiLineString)
				arrGeom[i++] = (Geometry)this.parseMultiLineString((WPSMultiLineString)geom);
			else if (geom instanceof WPSMultiPolygon)
				arrGeom[i++] = (Geometry)this.parseMultiPolygon((WPSMultiPolygon)geom);
			else if (geom instanceof WPSGeometryCollection)
				arrGeom[i++] = (Geometry)this.parseGeometryCollection((WPSGeometryCollection)geom);
		}
		return _factory.createGeometryCollection(arrGeom);
	}

	/**
	 * Parses a geometry.
	 * @return Object							The parsed object.
	 * @param geometry							The geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseGeometry(WPSGeometry geometry) throws Exception {
		if (geometry instanceof WPSPoint)
			return this.parsePoint((WPSPoint)geometry);
		else if (geometry instanceof WPSLineString)
			return this.parseLineString((WPSLineString)geometry);
		else if (geometry instanceof WPSPolygon)
			return this.parsePolygon((WPSPolygon)geometry);
		else if (geometry instanceof WPSMultiPoint)
			return this.parseMultiPoint((WPSMultiPoint)geometry);
		else if (geometry instanceof WPSMultiLineString)
			return this.parseMultiLineString((WPSMultiLineString)geometry);
		else if (geometry instanceof WPSMultiPolygon)
			return this.parseMultiPolygon((WPSMultiPolygon)geometry);
		else if (geometry instanceof WPSGeometryCollection)
			return this.parseGeometryCollection((WPSGeometryCollection)geometry);
		else
			throw new Exception(this.getClass().getSimpleName()+".parseGeometry(): unsupported geometry type.");
	}

	/**
	 * Parses a feature.
	 * @return Object							The parsed object.
	 * @param feature							The feature to parse. The attribute values of type Object
	 * 											are Java types or Object types the parser returns in the other methods.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseFeature(WPSFeature feature) throws Exception {
		FeatureSchema schema = new FeatureSchema();
		WPSAttribute att;
		Iterator<WPSAttribute> iterEntry = feature._lstAttributes.iterator();
		while (iterEntry.hasNext()) {
			att = iterEntry.next();
			AttributeType type = AttributeType.toAttributeType(att._objValue.getClass());
			if (type == null) {
				if (att._objValue instanceof Geometry)
					type = AttributeType.GEOMETRY;
				else
					type = AttributeType.OBJECT;
			}
			schema.addAttribute(att._sName, type);
		}
		Feature newFeature = new BasicFeature(schema);
		iterEntry = feature._lstAttributes.iterator();
		while (iterEntry.hasNext()) {
			att = iterEntry.next();
			newFeature.setAttribute(att._sName, att._objValue);
		}
		return newFeature;
	}

	/**
	 * Parses a feature collection.
	 * @return Object							The parsed object.
	 * @param featureCollection					The feature collection to parse. The features of type Object
	 * 											are Object types the parser returns the parseFeature() method.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseFeatureCollection(WPSFeatureCollection featureCollection) throws Exception {
		FeatureCollection newFeatureCollection = null;
		Feature feature;
		Iterator<WPSFeature> iterFeature = featureCollection._lstFeature.iterator();
		while (iterFeature.hasNext()) {
			feature = (Feature)this.parseFeature(iterFeature.next());
			if (newFeatureCollection == null)
				newFeatureCollection = new FeatureDataset(feature.getSchema());
			newFeatureCollection.add(feature);
		}
		return newFeatureCollection;
	}

	/**
	 * Parses a single coordinate.
	 * @return Coordinate						The parsed coordinate.
	 * @param coord								The coordinate to parse.
	 * @throws Exception						When an error occurs.
	 */
	protected Coordinate parseCoordinate(WPSCoordinate coord) throws Exception {
		if (coord._dZ != null)
			return new Coordinate(coord._dX.doubleValue(), coord._dY.doubleValue(), coord._dZ.doubleValue());
		else
			return new Coordinate(coord._dX.doubleValue(), coord._dY.doubleValue());
	}

	/**
	 * Parses a coordinate list.
	 * @return Coordinate[]						The parsed coordinates.
	 * @param lstCoord							The coordinates to parse.
	 * @throws Exception						When an error occurs.
	 */
	protected Coordinate[] parseCoordinates(List<WPSCoordinate> lstCoord) throws Exception {
		Coordinate[] arrCoord = new Coordinate[lstCoord.size()];
		int i = 0;
		Iterator<WPSCoordinate> iterCoord = lstCoord.iterator();
		while (iterCoord.hasNext())
			arrCoord[i++] = this.parseCoordinate(iterCoord.next());
		return arrCoord;
	}

	/**
	 * Parses a platform object to a WPSDatatype object.
	 * @return WPSDatatype						The parsed object. Feature attributes of type Complex must be set as DOMNode.
	 * @param obj								The object to pars.
	 * @throws Exception						When an error occurs.
	 */
	public WPSDatatype parseObject(Object obj) throws Exception {
		if (obj instanceof FeatureCollection) {
			FeatureCollection fc = (FeatureCollection)obj;
			WPSFeatureCollection coll = WPSDatatypes.createWPSFeatureCollection();
			coll._lstBoundingBox = this.convertEnvelope(fc.getEnvelope());
			List<WPSFeature> lstFeatures = new LinkedList<WPSFeature>();
			Iterator<?> iterFeature = fc.getFeatures().iterator();
			while (iterFeature.hasNext()) {
				lstFeatures.add((WPSFeature)this.parseObject(iterFeature.next()));
			}
			coll._lstFeature = lstFeatures;
			return coll;
		} else if (obj instanceof Feature) {
			Feature f = (Feature)obj;
			List<WPSAttribute> lstAttributes = new LinkedList<WPSAttribute>();
			WPSFeature feature = WPSDatatypes.createWPSFeature();
			int i = 0;
			while (i < f.getSchema().getAttributeCount()) {
				WPSAttribute att = WPSDatatypes.createWPSAttribute();
				att._sName = f.getSchema().getAttributeName(i);
				if (f.getSchema().getAttributeType(i).toString().equals("OBJECT")) {
					if ((f.getAttribute(i) instanceof Long) || (f.getAttribute(i) instanceof Boolean)) {
						att._objValue = f.getAttribute(i); 
					} else {
						// put here the DOM node
						throw new Exception(this.getClass().getSimpleName()+".parseObject(): OBJECT datatype not yet supported.");
					}
				} else if (f.getSchema().getAttributeType(i).toString().equals("GEOMETRY")) {
					// geometry
					att._objValue = f.getAttribute(i);//this.parseObject(f.getAttribute(i));
				} else {
					// native java type
					att._objValue = f.getAttribute(i);
				}
				lstAttributes.add(att);
				i++;
			}
			feature._lstAttributes = lstAttributes;
			return feature;
		} else if (obj instanceof Point) {
			Point p = (Point)obj;
			WPSPoint point = WPSDatatypes.createWPSPoint();
			point._sSrsName = ""+p.getSRID();
			point._coord = this.convertCoordinates(p.getCoordinates()).iterator().next();
			return point;
		} else if (obj instanceof LineString) {
			LineString l = (LineString)obj;
			WPSLineString lineString = WPSDatatypes.createWPSLineString();
			lineString._sSrsName = ""+l.getSRID();
			lineString._lstCoord = this.convertCoordinates(l.getCoordinates());
			return lineString;
		} else if (obj instanceof Polygon) {
			Polygon p = (Polygon)obj;
			WPSPolygon polygon = WPSDatatypes.createWPSPolygon();
			polygon._sSrsName = ""+p.getSRID();
			polygon._lstCoordOuterBoundary = this.convertCoordinates(p.getExteriorRing().getCoordinates());
			List<List<WPSCoordinate>> lstHoles = new LinkedList<List<WPSCoordinate>>();
			int i = 0;
			while (i < p.getNumInteriorRing()) {
				lstHoles.add(this.convertCoordinates(p.getInteriorRingN(i).getCoordinates()));
				i++;
			}
			polygon._lstCoordInnerBoundaries = lstHoles;
			return polygon;
		} else if (obj instanceof MultiPoint) {
			MultiPoint mp = (MultiPoint)obj;
			WPSMultiPoint multiPoint = WPSDatatypes.createWPSMultiPoint();
			multiPoint._sSrsName = ""+mp.getSRID();
			multiPoint._lstPoint = new LinkedList<WPSPoint>();
			int i = 0;
			while (i < mp.getNumGeometries()) {
				multiPoint._lstPoint.add((WPSPoint)this.parseObject(mp.getGeometryN(i)));
				i++;
			}
			return multiPoint;
		} else if (obj instanceof MultiLineString) {
			MultiLineString ml = (MultiLineString)obj;
			WPSMultiLineString multiLineString = WPSDatatypes.createWPSMultiLineString();
			multiLineString._sSrsName = ""+ml.getSRID();
			multiLineString._lstLineString = new LinkedList<WPSLineString>();
			int i = 0;
			while (i < ml.getNumGeometries()) {
				multiLineString._lstLineString.add((WPSLineString)this.parseObject(ml.getGeometryN(i)));
				i++;
			}
			return multiLineString;
		} else if (obj instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon)obj;
			WPSMultiPolygon multiPolygon = WPSDatatypes.createWPSMultiPolygon();
			multiPolygon._sSrsName = ""+mp.getSRID();
			multiPolygon._lstPolygon = new LinkedList<WPSPolygon>();
			int i = 0;
			while (i < mp.getNumGeometries()) {
				multiPolygon._lstPolygon.add((WPSPolygon)this.parseObject(mp.getGeometryN(i)));
				i++;
			}
			return multiPolygon;
		} else if (obj instanceof GeometryCollection) {
			GeometryCollection gc = (GeometryCollection)obj;
			WPSGeometryCollection geometryCollection = WPSDatatypes.createWPSGeometryCollection();
			geometryCollection._sSrsName = ""+gc.getSRID();
			geometryCollection._lstGeometry = new LinkedList<WPSGeometry>();
			int i = 0;
			while (i < gc.getNumGeometries()) {
				geometryCollection._lstGeometry.add((WPSGeometry)this.parseObject(gc.getGeometryN(i)));
				i++;
			}
			return geometryCollection;
		}
		throw new Exception(this.getClass().getSimpleName()+".parseObject(): unsupported datatype '"+obj.getClass().getName()+"'.");
	}

	/**
	 * Converts an envelope to a coordinate list.
	 * @return List<WPSCoordinate>				The coordinate list.
	 * @param envelope							The envelope.
	 */
	private List<WPSCoordinate> convertEnvelope(Envelope envelope) {
		List<WPSCoordinate> lstCoord = new LinkedList<WPSCoordinate>();
		WPSCoordinate coord = WPSDatatypes.createWPSCoordinate();
		coord._dX = envelope.getMinX();
		coord._dY = envelope.getMinY();
		lstCoord.add(coord);
		coord = WPSDatatypes.createWPSCoordinate();
		coord._dX = envelope.getMaxX();
		coord._dY = envelope.getMaxY();
		lstCoord.add(coord);
		return lstCoord;
	}

	/**
	 * Converts a coordinate array to a coordinate list.
	 * @return List<WPSCoordinate>				The coordinate list.
	 * @param arrCoords							The coordinates.
	 */
	private List<WPSCoordinate> convertCoordinates(Coordinate[] arrCoords) {
		List<WPSCoordinate> lstCoord = new LinkedList<WPSCoordinate>();
		int i = 0;
		while (i < arrCoords.length) {
			WPSCoordinate coord = WPSDatatypes.createWPSCoordinate();
			coord._dX = arrCoords[i].x;
			coord._dY = arrCoords[i].y;
			coord._dZ = arrCoords[i].z;
			lstCoord.add(coord);
			i++;
		}
		return lstCoord;
	}
}
