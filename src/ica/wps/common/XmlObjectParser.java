package ica.wps.common;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import javax.swing.table.DefaultTableModel;

import org.apache.xmlbeans.XmlObject;
import org.icaci.genmr.wps.FeatureCollectionDocument;
import org.icaci.genmr.wps.FeatureDocument;
import org.icaci.genmr.wps.TableDocument;
import org.icaci.genmr.wps.ListDocument;
import org.icaci.genmr.wps.LineStringDocument;
import org.icaci.genmr.wps.MultiLineStringDocument;
import org.icaci.genmr.wps.MultiPointDocument;
import org.icaci.genmr.wps.MultiPolygonDocument;
import org.icaci.genmr.wps.PointDocument;
import org.icaci.genmr.wps.PolygonDocument;
import org.icaci.genmr.wps.GeometryCollectionDocument;
import org.icaci.genmr.wps.FeatureType;
import org.icaci.genmr.wps.TableType;
import org.icaci.genmr.wps.ListType;
import org.icaci.genmr.wps.FeatureCollectionType;
import org.icaci.genmr.wps.AttributeTypeString;
import org.icaci.genmr.wps.AttributeTypeInteger;
import org.icaci.genmr.wps.AttributeTypeLong;
import org.icaci.genmr.wps.AttributeTypeDouble;
import org.icaci.genmr.wps.AttributeTypeBoolean;
import org.icaci.genmr.wps.AttributeTypeDate;
import org.icaci.genmr.wps.AttributeTypeComplex;
import org.icaci.genmr.wps.AttributeTypeGeometryPoint;
import org.icaci.genmr.wps.AttributeTypeGeometryLineString;
import org.icaci.genmr.wps.AttributeTypeGeometryPolygon;
import org.icaci.genmr.wps.AttributeTypeGeometryMultiPoint;
import org.icaci.genmr.wps.AttributeTypeGeometryMultiLineString;
import org.icaci.genmr.wps.AttributeTypeGeometryMultiPolygon;
import org.icaci.genmr.wps.AttributeTypeGeometryMultiGeometry;
import org.icaci.genmr.wps.AttributeTypeGeometryGeometry;
import org.icaci.genmr.wps.ListTypeFeature;
import org.icaci.genmr.wps.ListTypeFeatureCollection;
import org.icaci.genmr.wps.TableType.Tuple;
import org.w3c.dom.Node;

import net.opengis.gml.*;

import ica.wps.data.WPSDatatypes;
import ica.wps.data.WPSDatatypes.*;

/**
 * Classes parses an xml object.
 * @author	M. Wittensoeldner
 * @date	Created on 08.02.2007
 */
public class XmlObjectParser {
	protected IWPSXmlObjectParser 		_wpsXmlParser;
	
	/**
	 * Constructor.
	 * @param wpsXmlParser				The parser parses wps objects.
	 */
	public XmlObjectParser(IWPSXmlObjectParser wpsXmlParser) {
		_wpsXmlParser = wpsXmlParser;
	}

	/**
	 * Parses the xml object.
	 * @return Object					The parsed object.
	 * @param obj						The object to parse.
	 * @throws Exception				When an error occurs.
	 */
	public Object parseXmlObject(XmlObject obj) throws Exception {
// main complex types
		if (obj instanceof FeatureCollectionDocument) {
			FeatureCollectionDocument doc = (FeatureCollectionDocument)obj;
			return _wpsXmlParser.parseFeatureCollection(this.parseFeatureCollection(doc.getFeatureCollection()));
		} else if (obj instanceof FeatureDocument) {
			FeatureDocument doc = (FeatureDocument)obj;
			return _wpsXmlParser.parseFeature(this.parseFeature(doc.getFeature()));
		} else if (obj instanceof TableDocument) {
			TableDocument doc = (TableDocument)obj;
			return this.parseTable(doc.getTable());
		} else if (obj instanceof ListDocument) {
			ListDocument doc = (ListDocument)obj;
			return this.parseList(doc.getList());
		} else if (obj instanceof PointDocument)
			return _wpsXmlParser.parsePoint(this.parsePoint(((PointDocument)obj).getPoint()));
		else if (obj instanceof LineStringDocument)
			return _wpsXmlParser.parseLineString(this.parseLineString(((LineStringDocument)obj).getLineString()));
		else if (obj instanceof PolygonDocument)
			return _wpsXmlParser.parsePolygon(this.parsePolygon(((PolygonDocument)obj).getPolygon()));
		else if (obj instanceof MultiPointDocument)
			return _wpsXmlParser.parseMultiPoint(this.parseMultiPoint(((MultiPointDocument)obj).getMultiPoint()));
		else if (obj instanceof MultiLineStringDocument)
			return _wpsXmlParser.parseMultiLineString(this.parseMultiLineString(((MultiLineStringDocument)obj).getMultiLineString()));
		else if (obj instanceof MultiPolygonDocument)
			return _wpsXmlParser.parseMultiPolygon(this.parseMultiPolygon(((MultiPolygonDocument)obj).getMultiPolygon()));
		else if (obj instanceof GeometryCollectionDocument)
			return _wpsXmlParser.parseGeometryCollection(this.parseGeometryCollection(((GeometryCollectionDocument)obj).getGeometryCollection()));
		else if (obj instanceof GeometryDocument)
			return _wpsXmlParser.parseGeometry(this.parseGeometry(((GeometryDocument)obj).getGeometry()));
// attribute types
		else if (obj instanceof AttributeTypeGeometryPoint) {
			AttributeTypeGeometryPoint doc = (AttributeTypeGeometryPoint)obj;
			return _wpsXmlParser.parsePoint(this.parsePoint(doc.getValue()));
		} else if (obj instanceof AttributeTypeGeometryLineString) {
			AttributeTypeGeometryLineString doc = (AttributeTypeGeometryLineString)obj;
			return _wpsXmlParser.parseLineString(this.parseLineString(doc.getValue()));
		} else if (obj instanceof AttributeTypeGeometryPolygon) {
			AttributeTypeGeometryPolygon doc = (AttributeTypeGeometryPolygon)obj;
			return _wpsXmlParser.parsePolygon(this.parsePolygon(doc.getValue()));
		} else if (obj instanceof AttributeTypeGeometryMultiPoint) {
			AttributeTypeGeometryMultiPoint doc = (AttributeTypeGeometryMultiPoint)obj;
			return _wpsXmlParser.parseMultiPoint(this.parseMultiPoint(doc.getValue()));
		} else if (obj instanceof AttributeTypeGeometryMultiLineString) {
			AttributeTypeGeometryMultiLineString doc = (AttributeTypeGeometryMultiLineString)obj;
			return _wpsXmlParser.parseMultiLineString(this.parseMultiLineString(doc.getValue()));
		} else if (obj instanceof AttributeTypeGeometryMultiPolygon) {
			AttributeTypeGeometryMultiPolygon doc = (AttributeTypeGeometryMultiPolygon)obj;
			return _wpsXmlParser.parseMultiPolygon(this.parseMultiPolygon(doc.getValue()));
		} else if (obj instanceof AttributeTypeGeometryMultiGeometry) {
			AttributeTypeGeometryMultiGeometry doc = (AttributeTypeGeometryMultiGeometry)obj;
			return _wpsXmlParser.parseGeometryCollection(this.parseGeometryCollection(doc.getValue()));
		} else if (obj instanceof AttributeTypeGeometryGeometry) {
			AttributeTypeGeometryGeometry doc = (AttributeTypeGeometryGeometry)obj;
			return _wpsXmlParser.parseGeometry(this.parseGeometry(doc.getValue()));
		} else if (obj instanceof ListTypeFeature) {
			ListTypeFeature doc = (ListTypeFeature)obj;
			return _wpsXmlParser.parseFeature(this.parseFeature(doc.getValue()));
		} else if (obj instanceof ListTypeFeatureCollection) {
			ListTypeFeatureCollection doc = (ListTypeFeatureCollection)obj;
			return _wpsXmlParser.parseFeatureCollection(this.parseFeatureCollection(doc.getValue()));
		} else {
			// unsupported datatype
			return _wpsXmlParser.parseNode(obj.getDomNode());
		}
	}

	/**
	 * Parses a geometry object.
	 * @return WPSGeometry				The parsed geometry.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSGeometry parseGeometry(XmlObject obj) throws Exception {
		if (obj instanceof PointType)
			return this.parsePoint((PointType)obj);
		else if (obj instanceof LineStringType)
			return this.parseLineString((LineStringType)obj);
		else if (obj instanceof PolygonType)
			return this.parsePolygon((PolygonType)obj);
		else if (obj instanceof MultiPointType)
			return this.parseMultiPoint((MultiPointType)obj);
		else if (obj instanceof MultiLineStringType)
			return this.parseMultiLineString((MultiLineStringType)obj);
		else if (obj instanceof MultiPolygonType)
			return this.parseMultiPolygon((MultiPolygonType)obj);
		else if (obj instanceof GeometryCollectionType)
			return this.parseGeometryCollection((GeometryCollectionType)obj);
		else
			throw new Exception(this.getClass().getSimpleName()+".parseGeometry(): invalid geometry type.");
	}

	/**
	 * Parses a coordinate object.
	 * @return WPSCoordinate			The parsed coordinate.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSCoordinate parseCoordinate(CoordType obj) throws Exception {
		WPSCoordinate coord = new WPSDatatypes().new WPSCoordinate();
		coord._dX = obj.getX().doubleValue();
		coord._dY = obj.isSetY() ? obj.getY().doubleValue() : null;
		coord._dZ = obj.isSetZ() ? obj.getZ().doubleValue() : null;
		return coord;
	}

	/**
	 * Parses a coordinate object.
	 * @return List<WPSCoordinate>		The parsed coordinates.
	 * @param lstCoordIn				The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected List<WPSCoordinate> parseCoordinates(List<CoordType> lstCoordIn) throws Exception {
		List<WPSCoordinate> lstCoords = new LinkedList<WPSCoordinate>();
		Iterator<CoordType> iterCoord = lstCoordIn.iterator();
		while (iterCoord.hasNext())
			lstCoords.add(this.parseCoordinate(iterCoord.next()));
		return lstCoords;
	}

	/**
	 * Parses a coordinate object.
	 * @return List<WPSCoordinate>		The parsed coordinates.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected List<WPSCoordinate> parseCoordinates(CoordinatesType obj) throws Exception {
		List<WPSCoordinate> lstCoords = new LinkedList<WPSCoordinate>();
		String sTs = "\\s*\\s";
		String sCs = ",";
		String sDecimal = ".";
		if (obj.isSetTs())
			sTs = obj.getTs();
		if (obj.isSetCs())
			sCs = obj.getCs();
		if (obj.isSetDecimal())
			sDecimal = obj.getDecimal();
		String[] arrCoordTuple = obj.getStringValue().split(sTs, 0);
		String[] arrCoord;
		int i = 0;
		int k;
		while (i < arrCoordTuple.length) {
			WPSCoordinate coord = new WPSDatatypes().new WPSCoordinate();
			arrCoord = arrCoordTuple[i].trim().split(sCs);
			k = 0;
			while (k < 3) {
				Double dVal = null;
				if (k < arrCoord.length) {
					arrCoord[k] = arrCoord[k].trim();
					if (arrCoord[k].indexOf(sDecimal) >= 0)
						arrCoord[k] = arrCoord[k].replace(sDecimal, ".");
					dVal = new Double(arrCoord[k]);
				}
				if (k == 0)
					coord._dX = dVal;
				else if (k == 1)
					coord._dY = dVal;
				else
					coord._dZ = dVal;
				k++;
			}
			lstCoords.add(coord);
			i++;
		}
		return lstCoords;
	}

	/**
	 * Parses a point object.
	 * @return WPSPoint					The parsed point.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSPoint parsePoint(PointType obj) throws Exception {
		WPSPoint point = new WPSDatatypes().new WPSPoint();
		point._sSrsName = obj.getSrsName();
		point._sGid = obj.getGid();
		if (obj.isSetCoord())
			point._coord = this.parseCoordinate(obj.getCoord());
		else
			point._coord = this.parseCoordinates(obj.getCoordinates()).get(0);
		return point;
	}

	/**
	 * Parses a multi point object.
	 * @return WPSMultiPoint			The parsed multi point.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSMultiPoint parseMultiPoint(MultiPointType obj) throws Exception {
		List<WPSPoint> lstPoints = new LinkedList<WPSPoint>();
		WPSMultiPoint multiPoint = new WPSDatatypes().new WPSMultiPoint();
		multiPoint._sSrsName = obj.getSrsName();
		multiPoint._sGid = obj.getGid();
		PointMemberType member;
		Iterator<PointMemberType> iterMember = obj.getPointMemberList().iterator();
		while (iterMember.hasNext()) {
			member = iterMember.next();
			if (member.isSetPoint()) {
				lstPoints.add(this.parsePoint(member.getPoint()));
			} else {
				throw new Exception(this.getClass().getSimpleName()+".parseMultiPoint(): point is not set in point member.");
			}
		}
		multiPoint._lstPoint = lstPoints;
		return multiPoint;
	}

	/**
	 * Parses a line string object.
	 * @return WPSLineString			The parsed line string.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSLineString parseLineString(LineStringType obj) throws Exception {
		WPSLineString lineString = new WPSDatatypes().new WPSLineString();
		lineString._sSrsName = obj.getSrsName();
		lineString._sGid = obj.getGid();
		if (obj.isSetCoordinates())
			lineString._lstCoord = this.parseCoordinates(obj.getCoordinates());
		else
			lineString._lstCoord = this.parseCoordinates(obj.getCoordList());
		return lineString;
	}

	/**
	 * Parses a multi line string object.
	 * @return WPSMultiLineString		The parsed multi line string.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSMultiLineString parseMultiLineString(MultiLineStringType obj) throws Exception {
		List<WPSLineString> lstLineStrings = new LinkedList<WPSLineString>();
		WPSMultiLineString multiLineString = new WPSDatatypes().new WPSMultiLineString();
		multiLineString._sSrsName = obj.getSrsName();
		multiLineString._sGid = obj.getGid();
		LineStringMemberType member;
		Iterator<LineStringMemberType> iterMember = obj.getLineStringMemberList().iterator();
		while (iterMember.hasNext()) {
			member = iterMember.next();
			if (member.isSetLineString()) {
				lstLineStrings.add(this.parseLineString(member.getLineString()));
			} else {
				throw new Exception(this.getClass().getSimpleName()+".parseMultiLineString(): line string is not set in line string member.");
			}
		}
		multiLineString._lstLineString = lstLineStrings;
		return multiLineString;
	}

	/**
	 * Parses a polygon object.
	 * @return WPSPolygon				The parsed polygon.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSPolygon parsePolygon(PolygonType obj) throws Exception {
		WPSPolygon polygon = new WPSDatatypes().new WPSPolygon();
		polygon._sGid = obj.getGid();
		polygon._sSrsName = obj.getSrsName();
		
		if (obj.getOuterBoundaryIs().isSetLinearRing()) {
			if (obj.getOuterBoundaryIs().getLinearRing().isSetCoordinates())
				polygon._lstCoordOuterBoundary = this.parseCoordinates(obj.getOuterBoundaryIs().getLinearRing().getCoordinates());
			else
				polygon._lstCoordOuterBoundary = this.parseCoordinates(obj.getOuterBoundaryIs().getLinearRing().getCoordList());
		} else {
			throw new Exception(this.getClass().getSimpleName()+".parsePolygon(): linear ring is not set in outer boundary.");
		}
		LinkedList<List<WPSCoordinate>> lstInnerBoundaries = new LinkedList<List<WPSCoordinate>>();
		Iterator<LinearRingMemberType> iterInner = obj.getInnerBoundaryIsList().iterator();
		LinearRingMemberType member;
		while (iterInner.hasNext()) {
			member = iterInner.next();
			if (member.isSetLinearRing()) {
				if (member.getLinearRing().isSetCoordinates())
					lstInnerBoundaries.add(this.parseCoordinates(member.getLinearRing().getCoordinates()));
				else
					lstInnerBoundaries.add(this.parseCoordinates(member.getLinearRing().getCoordList()));
			} else {
				throw new Exception(this.getClass().getSimpleName()+".parsePolygon(): linear ring is not set in inner boundary.");
			}
		}
		polygon._lstCoordInnerBoundaries = lstInnerBoundaries;
		return polygon;
	}

	/**
	 * Parses a multi polygon object.
	 * @return WPSMultiPolygon			The parsed multi polygon.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSMultiPolygon parseMultiPolygon(MultiPolygonType obj) throws Exception {
		List<WPSPolygon> lstPolygons = new LinkedList<WPSPolygon>();
		WPSMultiPolygon multiPolygon = new WPSDatatypes().new WPSMultiPolygon();
		multiPolygon._sSrsName = obj.getSrsName();
		multiPolygon._sGid = obj.getGid();
		PolygonMemberType member;
		Iterator<PolygonMemberType> iterMember = obj.getPolygonMemberList().iterator();
		while (iterMember.hasNext()) {
			member = iterMember.next();
			if (member.isSetPolygon()) {
				lstPolygons.add(this.parsePolygon(member.getPolygon()));
			} else {
				throw new Exception(this.getClass().getSimpleName()+".parseMultiPolygon(): polygon is not set in polygon member.");
			}
		}
		multiPolygon._lstPolygon = lstPolygons;
		return multiPolygon;
	}

	/**
	 * Parses a geometry collection object.
	 * @return WPSGeometryCollection	The parsed geometry collection.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSGeometryCollection parseGeometryCollection(GeometryCollectionType obj) throws Exception {
		List<WPSGeometry> lstGeom = new LinkedList<WPSGeometry>();
		WPSGeometryCollection geometryCollection = new WPSDatatypes().new WPSGeometryCollection();
		geometryCollection._sSrsName = obj.getSrsName();
		geometryCollection._sGid = obj.getGid();
		Iterator<GeometryAssociationType> iterMember = obj.getGeometryMemberList().iterator();
		while (iterMember.hasNext()) {
			lstGeom.add(this.parseGeometry(iterMember.next().getGeometry()));
		}
		geometryCollection._lstGeometry = lstGeom;
		return geometryCollection;
	}

	/**
	 * Parses a feature.
	 * @return WPSFeature				The parsed feature.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSFeature parseFeature(FeatureType featureType) throws Exception {
		WPSFeature feature = new WPSDatatypes().new WPSFeature();
		feature._sFid = featureType.getFid();
		feature._sName = featureType.getName();
		feature._sDescription = featureType.getDescription();
		if (featureType.isSetBoundedBy() && featureType.getBoundedBy().isSetBox()) {
			if (featureType.getBoundedBy().getBox().isSetCoordinates())
				feature._lstBoundingBox = this.parseCoordinates(featureType.getBoundedBy().getBox().getCoordinates());
			else
				feature._lstBoundingBox = this.parseCoordinates(featureType.getBoundedBy().getBox().getCoordList());
		}
		feature._lstAttributes = this.parseAttributes(featureType.getDomNode());
		return feature;
	}

	/**
	 * Parses a table.
	 * @return DefaultTableModel		The parsed table.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected DefaultTableModel parseTable(TableType tableType) throws Exception {
		List<String> lstColumnNames = new LinkedList<String>();
		List<Object[]> lstTuples = new LinkedList<Object[]>();
		WPSAttribute attribute;
		boolean bFirst = true;
		Iterator<Tuple> iterTuple = tableType.getTupleList().iterator();
		Tuple tuple;
		while (iterTuple.hasNext()) {
			tuple = iterTuple.next();
			List<Object> lstValues = new LinkedList<Object>();
			Iterator<WPSAttribute> iterAttributes = this.parseAttributes(tuple.getDomNode()).iterator();
			while (iterAttributes.hasNext()) {
				attribute = iterAttributes.next();
				if (bFirst)
					lstColumnNames.add(attribute._sName);
				lstValues.add(attribute._objValue);
			}
			lstTuples.add(lstValues.toArray());
			bFirst = false;
		}
		Object[][] arrData = new Object[lstTuples.size()][];
		int i = 0;
		Iterator<Object[]> iterTuples = lstTuples.iterator();
		while (iterTuples.hasNext())
			arrData[i++] = iterTuples.next();
		return new DefaultTableModel(arrData, lstColumnNames.toArray());
	}

	/**
	 * Parses a list.
	 * @return List<Object>				The parsed list.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected List<Object> parseList(ListType listType) throws Exception {
		return this.parseListItems(listType.getDomNode());
	}

	/**
	 * Parses feature or feature collection attributes.
	 * @return List<WPSAttribute>		The parsed attributes.
	 * @param rootNode					The root node containing the attributes.
	 * @throws Exception				When an exception occurs.
	 */
	protected List<WPSAttribute> parseAttributes(Node rootNode) throws Exception {
		List<WPSAttribute> lstAttributes = new LinkedList<WPSAttribute>();
		Node node;
		Node node2;
		String sType;
		String sName;
		final String sPrefix = "AttributeType";
		final String sPrefixAtt = "Attribute";
		int k;
		int i = 0;
		while (i < rootNode.getChildNodes().getLength()) {
			node = rootNode.getChildNodes().item(i);
			if ((node.getNodeType() == Node.ELEMENT_NODE) && node.getLocalName().startsWith(sPrefixAtt)) {
				sName = node.getLocalName().substring(sPrefixAtt.length());
				k = 0;
				while (k < node.getChildNodes().getLength()) {
					node2 = node.getChildNodes().item(k);
					if ((node2.getNodeType() == Node.ELEMENT_NODE) && node2.getLocalName().equals("Value")) {
						if ((node2.getAttributes() != null) && (node2.getAttributes().getNamedItem("wpstype") != null)) {
							sType = node2.getAttributes().getNamedItem("wpstype").getNodeValue();
							sType = sType.substring(sPrefix.length());
							Object objAttribute = this.parseNodeContent(node2, sType);
							if (objAttribute != null) {
								WPSAttribute att = new WPSDatatypes().new WPSAttribute();
								att._sName = sName;
								att._objValue = objAttribute;
								lstAttributes.add(att);
							} else {
								throw new Exception(this.getClass().getSimpleName()+".parseFeature(): No attribute content found for attribute '"+sName+"'.");
							}
							break;
						}
					}
					k++;
				}
			}
			i++;
		}
		return lstAttributes;
	}

	/**
	 * Parses list items.
	 * @return List<Object>				The parsed list items.
	 * @param rootNode					The root node containing the attributes.
	 * @throws Exception				When an exception occurs.
	 */
	protected List<Object> parseListItems(Node rootNode) throws Exception {
		List<Object> lstItems = new LinkedList<Object>();
		Node node;
		Node node2;
		String sType;
		final String sPrefixAtt = "AttributeType";
		final String sPrefixList = "ListType";
		final String sItemName = "ListItem";
		int k;
		int i = 0;
		while (i < rootNode.getChildNodes().getLength()) {
			node = rootNode.getChildNodes().item(i);
			if ((node.getNodeType() == Node.ELEMENT_NODE) && node.getLocalName().equals(sItemName)) {
				k = 0;
				while (k < node.getChildNodes().getLength()) {
					node2 = node.getChildNodes().item(k);
					if ((node2.getNodeType() == Node.ELEMENT_NODE) && node2.getLocalName().equals("Value")) {
						if ((node2.getAttributes() != null) && (node2.getAttributes().getNamedItem("wpstype") != null)) {
							sType = node2.getAttributes().getNamedItem("wpstype").getNodeValue();
							Object objAttribute = null;
							if (sType.startsWith(sPrefixAtt))
								objAttribute = this.parseNodeContent(node2, sType.substring(sPrefixAtt.length()));
							else
								objAttribute = this.parseNodeContent(node2, sType.substring(sPrefixList.length()));
							if (objAttribute != null) {
								lstItems.add(objAttribute);
							} else {
								throw new Exception(this.getClass().getSimpleName()+".parseFeature(): No list item content found for item type '"+sType+"'.");
							}
							break;
						}
					}
					k++;
				}
			}
			i++;
		}
		return lstItems;
	}

	/**
	 * Parses the node content.
	 * @return Object					The parsed object.
	 * @param rootNode					The root node containing the content.
	 * @param sType						The datatype string.
	 * @throws Exception				When an exception occurs.
	 */
	protected Object parseNodeContent(Node rootNode, String sType) throws Exception {
		Object objAttribute = null;
		if (sType.startsWith("Geometry")) {
			if (sType.endsWith("MultiGeometry"))
				objAttribute = this.parseXmlObject(AttributeTypeGeometryMultiGeometry.Factory.parse(rootNode));
			else if (sType.endsWith("MultiPolygon"))
				objAttribute = this.parseXmlObject(AttributeTypeGeometryMultiPolygon.Factory.parse(rootNode));
			else if (sType.endsWith("MultiLineString"))
				objAttribute = this.parseXmlObject(AttributeTypeGeometryMultiLineString.Factory.parse(rootNode));
			else if (sType.endsWith("MultiPoint"))
				objAttribute = this.parseXmlObject(AttributeTypeGeometryMultiPoint.Factory.parse(rootNode));
			else if (sType.endsWith("Polygon"))
				objAttribute = this.parseXmlObject(AttributeTypeGeometryPolygon.Factory.parse(rootNode));
			else if (sType.endsWith("LineString"))
				objAttribute = this.parseXmlObject(AttributeTypeGeometryLineString.Factory.parse(rootNode));
			else if (sType.endsWith("Point"))
				objAttribute = this.parseXmlObject(AttributeTypeGeometryPoint.Factory.parse(rootNode));
			else
				throw new Exception(this.getClass().getSimpleName()+".parseFeature(): unsupported data type '"+sType+"'.");
		} else if (sType.equals("String")) {
			objAttribute = AttributeTypeString.Factory.parse(rootNode).getValue().getStringValue();
		} else if (sType.equals("Integer")) {
			objAttribute = new Integer(AttributeTypeInteger.Factory.parse(rootNode).getValue().getBigIntegerValue().intValue());
		} else if (sType.equals("Long")) {
			objAttribute = new Long(AttributeTypeLong.Factory.parse(rootNode).getValue().getLongValue());										
		} else if (sType.equals("Double")) {
			objAttribute = new Double(AttributeTypeDouble.Factory.parse(rootNode).getValue().getDoubleValue());										
		} else if (sType.equals("Boolean")) {
			objAttribute = new Boolean(AttributeTypeBoolean.Factory.parse(rootNode).getValue().getBooleanValue());										
		} else if (sType.equals("Date")) {
			objAttribute = AttributeTypeDate.Factory.parse(rootNode).getValue().getDateValue();																				
		} else if (sType.equals("Complex")) {
			objAttribute = _wpsXmlParser.parseNode(AttributeTypeComplex.Factory.parse(this.getFirstElementNode(rootNode)).getDomNode());
		} else if (sType.equals("Feature")) {
			objAttribute = this.parseXmlObject(ListTypeFeature.Factory.parse(rootNode));
		} else if (sType.equals("FeatureCollection")) {
			objAttribute = this.parseXmlObject(ListTypeFeatureCollection.Factory.parse(rootNode));
		} else {
			throw new Exception(this.getClass().getSimpleName()+".parseFeature(): unsupported data type '"+sType+"'.");
		}
		return objAttribute;
	}

	/**
	 * Parses a feature collection.
	 * @return WPSFeatureCollection		The parsed feature collection.
	 * @param obj						The object to parse.
	 * @throws Exception				When an exception occurs.
	 */
	protected WPSFeatureCollection parseFeatureCollection(FeatureCollectionType obj) throws Exception {
		List<WPSFeature> lstFeature = new LinkedList<WPSFeature>();
		WPSFeatureCollection featureCollection = new WPSDatatypes().new WPSFeatureCollection();
		featureCollection._sFid = obj.getFid();
		featureCollection._sName = obj.getName();
		featureCollection._sDescription = obj.getDescription();
		if (obj.isSetBoundedBy() && obj.getBoundedBy().isSetBox()) {
			if (obj.getBoundedBy().getBox().isSetCoordinates())
				featureCollection._lstBoundingBox = this.parseCoordinates(obj.getBoundedBy().getBox().getCoordinates());
			else
				featureCollection._lstBoundingBox = this.parseCoordinates(obj.getBoundedBy().getBox().getCoordList());
		}
		featureCollection._lstAttributes = this.parseAttributes(obj.getDomNode());
		Iterator<FeatureType> iterFeature = obj.getFeatureList().iterator();
		while (iterFeature.hasNext())
			lstFeature.add(this.parseFeature(iterFeature.next()));
		featureCollection._lstFeature = lstFeature;
		return featureCollection;
	}

	/**
	 * Gets the first child element.
	 * @return Node						The first child element node.
	 * @param node						The root node.
	 */
	private Node getFirstElementNode(Node node) {
		int i = 0;
		while (i < node.getChildNodes().getLength()) {
			if (node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE)
				return node.getChildNodes().item(i);
			i++;
		}
		return null;
	}
}


