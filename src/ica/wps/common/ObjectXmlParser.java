package ica.wps.common;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.math.BigDecimal;
import java.util.Date;
import javax.swing.table.DefaultTableModel;

import org.apache.xmlbeans.XmlObject;
import org.icaci.genmr.wps.AttributeTypeGeometryPoint;
import org.icaci.genmr.wps.AttributeTypeGeometryLineString;
import org.icaci.genmr.wps.AttributeTypeGeometryPolygon;
import org.icaci.genmr.wps.AttributeTypeGeometryMultiPoint;
import org.icaci.genmr.wps.AttributeTypeGeometryMultiLineString;
import org.icaci.genmr.wps.AttributeTypeGeometryMultiPolygon;
import org.icaci.genmr.wps.AttributeTypeGeometryMultiGeometry;
import org.icaci.genmr.wps.AttributeTypeComplex;
import org.icaci.genmr.wps.ListTypeFeature;
import org.icaci.genmr.wps.ListTypeFeatureCollection;
import org.icaci.genmr.wps.FeatureType;
import org.icaci.genmr.wps.TableType;
import org.icaci.genmr.wps.TableType.Tuple;
import org.icaci.genmr.wps.ListType;
import org.icaci.genmr.wps.FeatureCollectionType;
import org.icaci.genmr.wps.AttributeTypeString;
import org.icaci.genmr.wps.AttributeTypeInteger;
import org.icaci.genmr.wps.AttributeTypeLong;
import org.icaci.genmr.wps.AttributeTypeDouble;
import org.icaci.genmr.wps.AttributeTypeBoolean;
import org.icaci.genmr.wps.AttributeTypeDate;
import org.icaci.genmr.wps.AttTypeFeature;
import org.icaci.genmr.wps.AttTypeList;
import org.icaci.genmr.wps.FeatureCollectionDocument;
import org.icaci.genmr.wps.FeatureDocument;
import org.icaci.genmr.wps.TableDocument;
import org.icaci.genmr.wps.ListDocument;
import org.icaci.genmr.wps.PointDocument;
import org.icaci.genmr.wps.LineStringDocument;
import org.icaci.genmr.wps.PolygonDocument;
import org.icaci.genmr.wps.MultiPointDocument;
import org.icaci.genmr.wps.MultiLineStringDocument;
import org.icaci.genmr.wps.MultiPolygonDocument;
import org.icaci.genmr.wps.GeometryCollectionDocument;
import org.w3c.dom.Node;

import net.opengis.gml.*;

import ica.wps.data.WPSDatatypes;
import ica.wps.data.WPSDatatypes.*;
import ica.wps.WPSConstants;

/**
 * Classes parses a wps object to a xml object.
 * @author	M. Wittensoeldner
 * @date	Created on 08.02.2007
 */
public class ObjectXmlParser {
	protected IWPSXmlObjectParser 		_wpsXmlParser;
	
	/**
	 * Constructor.
	 * @param wpsXmlParser				The parser parses wps objects.
	 */
	public ObjectXmlParser(IWPSXmlObjectParser wpsXmlParser) {
		_wpsXmlParser = wpsXmlParser;
	}

	/**
	 * Parses the object as ica document.
	 * @return XmlObject				The parsed object.
	 * @param obj						The object to parse.
	 * @throws Exception				When an error occurs.
	 */
	public XmlObject parseObjectAsDocument(Object obj, IWPSXmlObjectParser parser) throws Exception {
		try {
			return this.parseWpsObject(parser.parseObject(obj), true, false);
		} catch (Exception ex) {
			return this.parseObject(obj, true);
		}
	}

	/**
	 * Parses the wps geometry as gml document.
	 * @return XmlObject				The parsed object.
	 * @param geom						The geometry to parse.
	 * @throws Exception				When an error occurs.
	 */
	public XmlObject parseWpsObjectAsGmlDocument(WPSGeometry geom) throws Exception {
		return this.parseWpsObject(geom, true, true);
	}

	/**
	 * Parses the wps object.
	 * @return XmlObject				The parsed object.
	 * @param obj						The object to parse.
	 * @throws Exception				When an error occurs.
	 */
	public XmlObject parseWpsObject(WPSDatatype obj) throws Exception {
		return this.parseWpsObject(obj, false, false);
	}

	/**
	 * Parses the wps object.
	 * @return XmlObject				The parsed object.
	 * @param obj						The object to parse.
	 * @param bReturnDocument			Returns the document object.
	 * @param bGml						Returns the gml document object.
	 * @throws Exception				When an error occurs.
	 */
	protected XmlObject parseWpsObject(WPSDatatype obj, boolean bReturnDocument, boolean bGml) throws Exception {
		String sAttPrefix = "Attribute";
		if (obj instanceof WPSFeatureCollection) {
			WPSFeatureCollection data = (WPSFeatureCollection)obj;
			FeatureCollectionType type = FeatureCollectionType.Factory.newInstance();
			if (data._sName != null)
				type.setName(data._sName);
			if (data._sFid != null)
				type.setFid(data._sFid);
			if (data._sDescription != null)
				type.setDescription(data._sDescription);
			if (data._lstBoundingBox != null)
				type.setBoundedBy(this.createBoundingShape(data._lstBoundingBox));
			FeatureType[] arrFeature = new FeatureType[data._lstFeature.size()];
			Iterator<WPSFeature> iterFeature = data._lstFeature.iterator();
			int i = 0;
			while ((i < arrFeature.length) && iterFeature.hasNext()) {
				arrFeature[i] = (FeatureType)this.parseWpsObject(iterFeature.next());
				i++;
			}
			type.setFeatureArray(arrFeature);
			this.parseAttriubutes(type.getDomNode(), data._lstAttributes, sAttPrefix);
			if (bReturnDocument) {
				FeatureCollectionDocument doc = FeatureCollectionDocument.Factory.newInstance();
				doc.setFeatureCollection(type);
				return doc;
			}
			else
				return type;
		} else if (obj instanceof WPSFeature) {
			WPSFeature data = (WPSFeature)obj;
			FeatureType type = FeatureType.Factory.newInstance();
			if (data._sName != null)
				type.setName(data._sName);
			if (data._sFid != null)
				type.setFid(data._sFid);
			if (data._sDescription != null)
				type.setDescription(data._sDescription);
			if (data._lstBoundingBox != null)
				type.setBoundedBy(this.createBoundingShape(data._lstBoundingBox));
			this.parseAttriubutes(type.getDomNode(), data._lstAttributes, sAttPrefix);
			if (bReturnDocument) {
				FeatureDocument doc = FeatureDocument.Factory.newInstance();
				doc.setFeature(type);
				return doc;
			}
			else
				return type;
		} else if (obj instanceof WPSPoint) {
			WPSPoint data = (WPSPoint)obj;
			PointType type = PointType.Factory.newInstance();
			if (data._sGid != null)
				type.setGid(data._sGid);
			if (data._sSrsName != null)
				type.setSrsName(data._sSrsName);
			type.setCoord(this.createCoord(data._coord));
			if (bReturnDocument) {
				if (bGml) {
					net.opengis.gml.PointDocument doc = net.opengis.gml.PointDocument.Factory.newInstance();
					doc.setPoint(type);
					return doc;
				} else {
					PointDocument doc = PointDocument.Factory.newInstance();
					doc.setPoint(type);
					return doc;
				}
			}
			else
				return type;
		} else if (obj instanceof WPSLineString) {
			WPSLineString data = (WPSLineString)obj;
			LineStringType type = LineStringType.Factory.newInstance();
			if (data._sGid != null)
				type.setGid(data._sGid);
			if (data._sSrsName != null)
				type.setSrsName(data._sSrsName);
			type.setCoordArray(this.createCoords(data._lstCoord));
			if (bReturnDocument) {
				if (bGml) {
					net.opengis.gml.LineStringDocument doc = net.opengis.gml.LineStringDocument.Factory.newInstance();
					doc.setLineString(type);
					return doc;
				} else {
					LineStringDocument doc = LineStringDocument.Factory.newInstance();
					doc.setLineString(type);
					return doc;
				}
			}
			else
				return type;
		} else if (obj instanceof WPSPolygon) {
			WPSPolygon data = (WPSPolygon)obj;
			PolygonType type = PolygonType.Factory.newInstance();
			if (data._sGid != null)
				type.setGid(data._sGid);
			if (data._sSrsName != null)
				type.setSrsName(data._sSrsName);
			type.addNewOuterBoundaryIs().addNewLinearRing().setCoordArray(this.createCoords(data._lstCoordOuterBoundary));
			if (data._lstCoordInnerBoundaries != null) {
				Iterator<List<WPSCoordinate>> iter = data._lstCoordInnerBoundaries.iterator();
				while (iter.hasNext()) {
					type.addNewInnerBoundaryIs().addNewLinearRing().setCoordArray(this.createCoords(iter.next()));
				}
			}
			if (bReturnDocument) {
				if (bGml) {
					net.opengis.gml.PolygonDocument doc = net.opengis.gml.PolygonDocument.Factory.newInstance();
					doc.setPolygon(type);
					return doc;
				} else {
					PolygonDocument doc = PolygonDocument.Factory.newInstance();
					doc.setPolygon(type);
					return doc;
				}
			}
			else
				return type;
		} else if (obj instanceof WPSMultiPoint) {
			WPSMultiPoint data = (WPSMultiPoint)obj;
			MultiPointType type = MultiPointType.Factory.newInstance();
			if (data._sGid != null)
				type.setGid(data._sGid);
			if (data._sSrsName != null)
				type.setSrsName(data._sSrsName);
			Iterator<WPSPoint> iter = data._lstPoint.iterator();
			while (iter.hasNext())
				type.addNewPointMember().setPoint((PointType)this.parseWpsObject(iter.next()));
			if (bReturnDocument) {
				if (bGml) {
					net.opengis.gml.MultiPointDocument doc = net.opengis.gml.MultiPointDocument.Factory.newInstance();
					doc.setMultiPoint(type);
					return doc;
				} else {
					MultiPointDocument doc = MultiPointDocument.Factory.newInstance();
					doc.setMultiPoint(type);
					return doc;
				}
			}
			else
				return type;
		} else if (obj instanceof WPSMultiLineString) {
			WPSMultiLineString data = (WPSMultiLineString)obj;
			MultiLineStringType type = MultiLineStringType.Factory.newInstance();
			if (data._sGid != null)
				type.setGid(data._sGid);
			if (data._sSrsName != null)
				type.setSrsName(data._sSrsName);
			Iterator<WPSLineString> iter = data._lstLineString.iterator();
			while (iter.hasNext())
				type.addNewLineStringMember().setLineString((LineStringType)this.parseWpsObject(iter.next()));
			if (bReturnDocument) {
				if (bGml) {
					net.opengis.gml.MultiLineStringDocument doc = net.opengis.gml.MultiLineStringDocument.Factory.newInstance();
					doc.setMultiLineString(type);
					return doc;
				} else {
					MultiLineStringDocument doc = MultiLineStringDocument.Factory.newInstance();
					doc.setMultiLineString(type);
					return doc;
				}
			}
			else
				return type;
		} else if (obj instanceof WPSMultiPolygon) {
			WPSMultiPolygon data = (WPSMultiPolygon)obj;
			MultiPolygonType type = MultiPolygonType.Factory.newInstance();
			if (data._sGid != null)
				type.setGid(data._sGid);
			if (data._sSrsName != null)
				type.setSrsName(data._sSrsName);
			Iterator<WPSPolygon> iter = data._lstPolygon.iterator();
			while (iter.hasNext())
				type.addNewPolygonMember().setPolygon((PolygonType)this.parseWpsObject(iter.next()));
			if (bReturnDocument) {
				if (bGml) {
					net.opengis.gml.MultiPolygonDocument doc = net.opengis.gml.MultiPolygonDocument.Factory.newInstance();
					doc.setMultiPolygon(type);
					return doc;
				} else {
					MultiPolygonDocument doc = MultiPolygonDocument.Factory.newInstance();
					doc.setMultiPolygon(type);
					return doc;
				}
			}
			else
				return type;
		} else if (obj instanceof WPSGeometryCollection) {
			WPSGeometryCollection data = (WPSGeometryCollection)obj;
			GeometryCollectionType type = GeometryCollectionType.Factory.newInstance();
			if (data._sGid != null)
				type.setGid(data._sGid);
			if (data._sSrsName != null)
				type.setSrsName(data._sSrsName);
			Iterator<WPSGeometry> iter = data._lstGeometry.iterator();
			while (iter.hasNext())
				type.addNewGeometryMember().set(this.parseWpsObjectAsGmlDocument(iter.next()));
			if (bReturnDocument) {
				if (bGml) {
					net.opengis.gml.GeometryCollectionDocument doc = net.opengis.gml.GeometryCollectionDocument.Factory.newInstance();
					doc.setGeometryCollection(type);
					return doc;
				} else {
					GeometryCollectionDocument doc = GeometryCollectionDocument.Factory.newInstance();
					doc.setGeometryCollection(type);
					return doc;
				}
			}
			else
				return type;
		} else {
			// unsupported datatype
			throw new Exception(this.getClass().getSimpleName()+".parseWpsObject(): unknown datatype '"+obj.getClass().getName()+"'.");
		}
	}

	/**
	 * Parses the object.
	 * @return XmlObject				The parsed object.
	 * @param obj						The object to parse.
	 * @param bReturnDocument			Returns the document object.
	 * @throws Exception				When an error occurs.
	 */
	protected XmlObject parseObject(Object obj, boolean bReturnDocument) throws Exception {
		String sAttPrefix = "Attribute";
		if (obj instanceof DefaultTableModel) {
			DefaultTableModel data = (DefaultTableModel)obj;
			TableType type = TableType.Factory.newInstance();
			Tuple tuple;
			int i = 0;
			int k;
			while (i < data.getRowCount()) {
				tuple = type.addNewTuple();
				List<WPSAttribute> lstAttributes = new LinkedList<WPSAttribute>();
				k = 0;
				while (k < data.getColumnCount()) {
					WPSAttribute attribute = WPSDatatypes.createWPSAttribute();
					attribute._sName = data.getColumnName(k);
					attribute._objValue = data.getValueAt(i, k);
					lstAttributes.add(attribute);
					k++;
				}
				this.parseAttriubutes(tuple.getDomNode(), lstAttributes, sAttPrefix);
				i++;
			}
			if (bReturnDocument) {
				TableDocument doc = TableDocument.Factory.newInstance();
				doc.setTable(type);
				return doc;
			}
			else
				return type;
		} else if (obj instanceof List) {
			List<?> data = (List<?>)obj;
			ListType type = ListType.Factory.newInstance();
			List<WPSAttribute> lstAttributes = new LinkedList<WPSAttribute>();
			Iterator<?> iterValues = data.iterator();
			while (iterValues.hasNext()) {
				WPSAttribute attribute = WPSDatatypes.createWPSAttribute();
				attribute._sName = "ListItem";
				attribute._objValue = iterValues.next();
				lstAttributes.add(attribute);
			}
			this.parseAttriubutes(type.getDomNode(), lstAttributes, "");
			if (bReturnDocument) {
				ListDocument doc = ListDocument.Factory.newInstance();
				doc.setList(type);
				return doc;
			}
			else
				return type;
		} else {
			// unsupported datatype
			throw new Exception(this.getClass().getSimpleName()+".parseObject(): unknown datatype '"+obj.getClass().getName()+"'.");
		}
	}

	/**
	 * Parses attributes.
	 * @return void
	 * @param rootNode					The root node holds the resulting attributes.
	 * @param lstAttributes				The attributes to parse.
	 * @param sAttPrefix				The attribute prefix.
	 * @throws Exception				When an error occurs.
	 */
	private void parseAttriubutes(Node rootNode, List<WPSAttribute> lstAttributes, String sAttPrefix) throws Exception {
		if (lstAttributes != null) {
			Iterator<WPSAttribute> iterAttribute = lstAttributes.iterator();
			while (iterAttribute.hasNext()) {
				this.createAttribute(rootNode, iterAttribute.next(), sAttPrefix);
			}
		}
	}

	/**
	 * Creates an attribute node an adds it to the parent node.
	 * @return void
	 * @param parentNode				The parent node.
	 * @param attribute					The attribute to parse.
	 * @param sAttPrefix				The attribute prefix.
	 * @throws Exception				When an error occurs.
	 */
	private void createAttribute(Node parentNode, WPSAttribute attribute, String sAttPrefix) throws Exception {
		String sName = attribute._sName;
		if (attribute._objValue == null) {
			attribute = new WPSDatatypes().new WPSAttribute();
			attribute._sName = sName;
			attribute._objValue = "";
		}
		XmlObject objAtt = null;
		if (attribute._objValue instanceof String) {
			objAtt = AttributeTypeString.Factory.newInstance();
			AttributeTypeString.Value val = ((AttributeTypeString)objAtt).addNewValue();
			val.setStringValue(attribute._objValue.toString());
			val.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_STRING);
		} else if (attribute._objValue instanceof Integer) {
			objAtt = AttributeTypeInteger.Factory.newInstance();
			AttributeTypeInteger.Value val = ((AttributeTypeInteger)objAtt).addNewValue();
			val.setBigDecimalValue(new BigDecimal((Integer)attribute._objValue));
			val.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_INTEGER);
		} else if (attribute._objValue instanceof Long) {
			objAtt = AttributeTypeLong.Factory.newInstance();
			AttributeTypeLong.Value val = ((AttributeTypeLong)objAtt).addNewValue();
			val.setBigDecimalValue(new BigDecimal((Long)attribute._objValue));
			val.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_LONG);
		} else if (attribute._objValue instanceof Double) {
			objAtt = AttributeTypeDouble.Factory.newInstance();
			AttributeTypeDouble.Value val = ((AttributeTypeDouble)objAtt).addNewValue();
			val.setDoubleValue(((Double)attribute._objValue).doubleValue());
			val.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_DOUBLE);
		} else if (attribute._objValue instanceof Boolean) {
			objAtt = AttributeTypeBoolean.Factory.newInstance();
			AttributeTypeBoolean.Value val = ((AttributeTypeBoolean)objAtt).addNewValue();
			val.setBooleanValue(((Boolean)attribute._objValue).booleanValue());
			val.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_BOOLEAN);
		} else if (attribute._objValue instanceof Date) {
			objAtt = AttributeTypeDate.Factory.newInstance();
			AttributeTypeDate.Value val = ((AttributeTypeDate)objAtt).addNewValue();
			val.setDateValue((Date)attribute._objValue);
			val.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_DATE);
		} else if (attribute._objValue instanceof Node) {
			objAtt = AttributeTypeComplex.Factory.newInstance();
			AttributeTypeComplex.Value value = ((AttributeTypeComplex)objAtt).addNewValue();
			value.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_COMPLEX);
			value.set(XmlObject.Factory.parse((Node)attribute._objValue));			
		} else {
			WPSDatatype objWps = _wpsXmlParser.parseObject(attribute._objValue);
			if (objWps instanceof WPSGeometry) {
				XmlObject objGeom = this.parseWpsObject((WPSGeometry)objWps);
				if (objGeom instanceof PointType) {
					objAtt = AttributeTypeGeometryPoint.Factory.newInstance();
					AttributeTypeGeometryPoint.Value value = ((AttributeTypeGeometryPoint)objAtt).addNewValue();
					value.set(objGeom);
					value.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_GEOMETRY_POINT);
				} else if (objGeom instanceof LineStringType) {
					objAtt = AttributeTypeGeometryLineString.Factory.newInstance();
					AttributeTypeGeometryLineString.Value value = ((AttributeTypeGeometryLineString)objAtt).addNewValue();
					value.set(objGeom);
					value.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_GEOMETRY_LINE_STRING);
				} else if (objGeom instanceof PolygonType) {
					objAtt = AttributeTypeGeometryPolygon.Factory.newInstance();
					AttributeTypeGeometryPolygon.Value value = ((AttributeTypeGeometryPolygon)objAtt).addNewValue();
					value.set(objGeom);
					value.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_GEOMETRY_POLYGON);
				} else if (objGeom instanceof MultiPointType) {
					objAtt = AttributeTypeGeometryMultiPoint.Factory.newInstance();
					AttributeTypeGeometryMultiPoint.Value value = ((AttributeTypeGeometryMultiPoint)objAtt).addNewValue();
					value.set(objGeom);
					value.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_GEOMETRY_MULTI_POINT);
				} else if (objGeom instanceof MultiLineStringType) {
					objAtt = AttributeTypeGeometryMultiLineString.Factory.newInstance();
					AttributeTypeGeometryMultiLineString.Value value = ((AttributeTypeGeometryMultiLineString)objAtt).addNewValue();
					value.set(objGeom);
					value.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_GEOMETRY_MULTI_LINE_STRING);
				} else if (objGeom instanceof MultiPolygonType) {
					objAtt = AttributeTypeGeometryMultiPolygon.Factory.newInstance();
					AttributeTypeGeometryMultiPolygon.Value value = ((AttributeTypeGeometryMultiPolygon)objAtt).addNewValue();
					value.set(objGeom);
					value.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_GEOMETRY_MULTI_POLYGON);
				} else if (objGeom instanceof GeometryCollectionType) {
					objAtt = AttributeTypeGeometryMultiGeometry.Factory.newInstance();
					AttributeTypeGeometryMultiGeometry.Value value = ((AttributeTypeGeometryMultiGeometry)objAtt).addNewValue();
					value.set(objGeom);
					value.setWpstype(AttTypeFeature.ATTRIBUTE_TYPE_GEOMETRY_MULTI_GEOMETRY);
				} else {
					throw new Exception(this.getClass().getSimpleName()+".createAttribute(): unknown geometry type '"+objGeom.getClass().getName()+"'.");
				}
			} else if (objWps instanceof WPSFeature) {
				XmlObject objFeature = this.parseWpsObject(objWps);
				objAtt = ListTypeFeature.Factory.newInstance();
				ListTypeFeature.Value value = ((ListTypeFeature)objAtt).addNewValue();
				value.set(objFeature);
				value.setWpstype(AttTypeList.LIST_TYPE_FEATURE);
			} else if (objWps instanceof WPSFeatureCollection) {
				XmlObject objFeatureCollection = this.parseWpsObject(objWps);
				objAtt = ListTypeFeatureCollection.Factory.newInstance();
				ListTypeFeatureCollection.Value value = ((ListTypeFeatureCollection)objAtt).addNewValue();
				value.set(objFeatureCollection);
				value.setWpstype(AttTypeList.LIST_TYPE_FEATURE_COLLECTION);
			} else {
				throw new Exception(this.getClass().getSimpleName()+".createAttribute(): not a geometry type '"+objWps.getClass().getName()+"'.");
			}
		}
		Node nodeNew = parentNode.appendChild(parentNode.getOwnerDocument().createElementNS(WPSConstants.WPS_DEFAULT_NAMESPACE, sAttPrefix+sName));
		nodeNew.appendChild(nodeNew.getOwnerDocument().importNode(objAtt.getDomNode(), true));
	}

	/**
	 * Creates a bounding shape.
	 * @return BoundingShapeType		The bounding shape.
	 * @param lstCoord					The bounding coordinates.
	 * @throws Exception				When an error occurs.
	 */
	private BoundingShapeType createBoundingShape(List<WPSCoordinate> lstCoord) throws Exception {
		BoundingShapeType type = BoundingShapeType.Factory.newInstance();
		BoxType box = type.addNewBox();
		box.setCoordArray(this.createCoords(lstCoord));
		return type;
	}

	/**
	 * Creates a coordinate.
	 * @return CoordType				The coordinate.
	 * @param coord						The coordinate.
	 * @throws Exception				When an error occurs.
	 */
	private CoordType createCoord(WPSCoordinate coord) throws Exception {
		CoordType coordType = CoordType.Factory.newInstance();
		if ((coord._dX != null) && (!coord._dX.isNaN()) && (!coord._dX.isInfinite()))
			coordType.setX(new BigDecimal(coord._dX));
		if ((coord._dY!= null) && (!coord._dY.isNaN()) && (!coord._dY.isInfinite()))
			coordType.setY(new BigDecimal(coord._dY));
		if ((coord._dZ != null) && (!coord._dZ.isNaN()) && (!coord._dZ.isInfinite()))
			coordType.setZ(new BigDecimal(coord._dZ));
		return coordType;
	}
	
	/**
	 * Creates a coordinate array.
	 * @return CoordType[]				The coordinate array.
	 * @param lstCoord					The coordinates.
	 * @throws Exception				When an error occurs.
	 */
	private CoordType[] createCoords(List<WPSCoordinate> lstCoord) throws Exception {
		CoordType[] arrCoord = new CoordType[lstCoord.size()];
		int i = 0;
		Iterator<WPSCoordinate> iterCoord = lstCoord.iterator();
		while ((i < arrCoord.length) && (iterCoord.hasNext())) {
			arrCoord[i] = this.createCoord(iterCoord.next());
			i++;
		}
		return arrCoord;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}


