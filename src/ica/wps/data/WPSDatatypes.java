package ica.wps.data;

import java.util.List;

/**
 * Simple geometry container for parsing geometry data. The member variables started with an underline (_) and are accessible directly.
 * Membervariables can have the value 'null'.
 * @author	M. Wittensoeldner
 * @date	Created on 08.02.2007
 */
public class WPSDatatypes {
	public static WPSCoordinate createWPSCoordinate()					{return new WPSDatatypes().new WPSCoordinate();}
	public static WPSPoint createWPSPoint()								{return new WPSDatatypes().new WPSPoint();}
	public static WPSLineString createWPSLineString()					{return new WPSDatatypes().new WPSLineString();}
	public static WPSPolygon createWPSPolygon()							{return new WPSDatatypes().new WPSPolygon();}
	public static WPSMultiPoint createWPSMultiPoint()					{return new WPSDatatypes().new WPSMultiPoint();}
	public static WPSMultiLineString createWPSMultiLineString()			{return new WPSDatatypes().new WPSMultiLineString();}
	public static WPSMultiPolygon createWPSMultiPolygon()				{return new WPSDatatypes().new WPSMultiPolygon();}
	public static WPSGeometryCollection createWPSGeometryCollection()	{return new WPSDatatypes().new WPSGeometryCollection();}
	public static WPSAttribute createWPSAttribute()						{return new WPSDatatypes().new WPSAttribute();}
	public static WPSFeature createWPSFeature()							{return new WPSDatatypes().new WPSFeature();}
	public static WPSFeatureCollection createWPSFeatureCollection()		{return new WPSDatatypes().new WPSFeatureCollection();}
	
	public abstract class WPSDatatype {
	}
	
	public class WPSCoordinate {
		public Double						_dX = null;
		public Double						_dY = null;
		public Double						_dZ = null;
	}

	public abstract class WPSGeometry extends WPSDatatype {
		public String						_sGid = null;
		public String						_sSrsName = null;
	}

	public class WPSPoint extends WPSGeometry {
		public WPSCoordinate				_coord = null;
	}

	public class WPSLineString extends WPSGeometry {
		public List<WPSCoordinate>			_lstCoord = null;
	}

	public class WPSPolygon extends WPSGeometry {
		public List<WPSCoordinate>			_lstCoordOuterBoundary = null;
		public List<List<WPSCoordinate>>	_lstCoordInnerBoundaries = null;
	}

	public class WPSMultiPoint extends WPSGeometry {
		public List<WPSPoint>				_lstPoint = null;
	}

	public class WPSMultiLineString extends WPSGeometry {
		public List<WPSLineString>			_lstLineString = null;
	}

	public class WPSMultiPolygon extends WPSGeometry {
		public List<WPSPolygon>				_lstPolygon = null;
	}

	public class WPSGeometryCollection extends WPSGeometry {
		public List<WPSGeometry>			_lstGeometry = null;
	}

	public class WPSAttribute {
		public String						_sName = null;
		public Object						_objValue = null;
	}

	public abstract class WPSFeatureBase extends WPSDatatype {
		public String						_sFid = null;
		public String						_sName = null;
		public String						_sDescription = null;
		public List<WPSCoordinate>			_lstBoundingBox = null;
	}

	public class WPSFeature extends WPSFeatureBase {
		public List<WPSAttribute>			_lstAttributes = null;
	}

	public class WPSFeatureCollection extends WPSFeatureBase {
		public List<WPSAttribute>			_lstAttributes = null;
		public List<WPSFeature>				_lstFeature = null;
	}
}

