package ica.wps.common;

import org.w3c.dom.Node;

import ica.wps.data.WPSDatatypes.*;

/**
 * Interface defines a parser parses xml data to Objects.
 * @author	M. Wittensoeldner
 * @date	Created on 08.02.2007
 */
public interface IWPSXmlObjectParser {

	/**
	 * Parses the content of the whole xml tree. This method is called when the xml content is unknown. e.g. complex feature attribute.
	 * When no unknown data is available, this method has not been to implement.
	 * @return Object							The parsed object.
	 * @param rootNode							The root node of the xml tree.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseNode(Node rootNode) throws Exception;
	
	/**
	 * Parses a point geometry.
	 * @return Object							The parsed object.
	 * @param point								The point geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parsePoint(WPSPoint point) throws Exception;

	/**
	 * Parses a line string geometry.
	 * @return Object							The parsed object.
	 * @param lineString						The line string geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseLineString(WPSLineString lineString) throws Exception;

	/**
	 * Parses a polygon geometry.
	 * @return Object							The parsed object.
	 * @param polygon							The polygon geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parsePolygon(WPSPolygon polygon) throws Exception;

	/**
	 * Parses a multi point geometry.
	 * @return Object							The parsed object.
	 * @param multiPoint						The multi point geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseMultiPoint(WPSMultiPoint multiPoint) throws Exception;

	/**
	 * Parses a multi line string geometry.
	 * @return Object							The parsed object.
	 * @param multiLineString					The multi line string geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseMultiLineString(WPSMultiLineString multiLineString) throws Exception;

	/**
	 * Parses a multi polygon geometry.
	 * @return Object							The parsed object.
	 * @param multiPolygon						The multi polygon geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseMultiPolygon(WPSMultiPolygon multiPolygon) throws Exception;

	/**
	 * Parses a geometry collection.
	 * @return Object							The parsed object.
	 * @param geometryCollection				The geometry collection.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseGeometryCollection(WPSGeometryCollection geometryCollection) throws Exception;

	/**
	 * Parses a geometry.
	 * @return Object							The parsed object.
	 * @param geometry							The geometry.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseGeometry(WPSGeometry geometry) throws Exception;

	/**
	 * Parses a feature.
	 * @return Object							The parsed object.
	 * @param feature							The feature to parse. The attribute values of type Object
	 * 											are Java types or Object types the parser returns in the other methods.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseFeature(WPSFeature feature) throws Exception;

	/**
	 * Parses a feature collection.
	 * @return Object							The parsed object.
	 * @param featureCollection					The feature collection to parse. The features of type Object
	 * 											are Object types the parser returns the parseFeature() method.
	 * @throws Exception						When an error occurs.
	 */
	public Object parseFeatureCollection(WPSFeatureCollection featureCollection) throws Exception;

	/**
	 * Parses a platform object to a WPSDatatype object.
	 * @return WPSDatatype						The parsed object. Feature attributes of type Complex must be set as DOMNode.
	 * @param obj								The object to pars.
	 * @throws Exception						When an error occurs.
	 */
	public WPSDatatype parseObject(Object obj) throws Exception;
}

