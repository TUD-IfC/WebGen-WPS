package ch.unizh.geo.webgen.xml;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import com.vividsolutions.jts.geom.*;


/**
 * Writes or creates a xml containing the GML
 * representation of a JTS Geometry.
 * Supports a user-defined line prefix and a user-defined maximum number of coordinates per line.
 * Indents components of Geometries to provide a nicely-formatted representation.
 */
public class GMLGeometryGenerator
{

  // these could be make settable
  private static final String coordinateSeparator = ",";
  private static final String tupleSeparator = " ";

  private String srsName = null;
  private String gid = null;
  
  private Namespace gmlns;
  private QName coordQName;
  private QName namepoint;
  private QName namelinestring;
  private QName namelinearring;
  private QName namepolygon;
  private QName nameouterboundary;
  private QName nameinnerboundary;
  private QName namemultipoint;
  private QName namepointmember;
  private QName namemultilinestring;
  private QName namelinestringmember;
  private QName namemultipolygon;
  private QName namepolygonmember;
  private QName namemultigeometry;
  private QName namegeometrymember;

  
  public GMLGeometryGenerator() {
	//initialise names
	gmlns = new Namespace("gml","http://www.opengis.net/gml");
	coordQName = QName.get("coordinates", gmlns);
	namepoint = QName.get("Point", gmlns);
	namelinestring = QName.get("LineString", gmlns);
	namelinearring = QName.get("LinearRing", gmlns);
	namepolygon = QName.get("Polygon", gmlns);
	nameouterboundary = QName.get("outerBoundaryIs", gmlns);
	nameinnerboundary = QName.get("innerBoundaryIs", gmlns);
	namemultipoint = QName.get("MultiPoint", gmlns);
	namepointmember = QName.get("pointMember", gmlns);
	namemultilinestring = QName.get("MultiLineString", gmlns);
	namelinestringmember = QName.get("lineStringMember", gmlns);
  	namemultipolygon = QName.get("MultiPolygon", gmlns);
  	namepolygonmember = QName.get("polygonMember", gmlns);
  	namemultigeometry = QName.get("MultiGeometry", gmlns);
  	namegeometrymember = QName.get("geometryMember", gmlns);
	}


  /**
   * Sets the <code>srsName</code> attribute to be output on the Geometry element.
   * If <code>null</code> no attribute will be output.
   * @param srsName
   */
  public void setSRSName(String srsName)
  {
    this.srsName = srsName;
  }

  /**
   * Sets the <code>gid</code> attribute to be output on the Geometry element.
   * If <code>null</code> no attribute will be output.
   * @param srsName
   */
  public void setGID(String gid)
  {
    this.gid = gid;
  }


  public void write(Element propertyel, Geometry geom) {
	  //generate geometriy tags
	  writeGeometry(propertyel, geom, true);
	  //System.out.println(geom.getClass().getName());
  }

  /**
   * Generates the GML representation of a JTS Geometry.
   * @param soap Node to append geometry
   * @param g Geometry to output
   */
  private void writeGeometry(Element propertyel, Geometry g, boolean isnotmult) {
    /*
     * order is important in this if-else list.
     * E.g. homogeneous collections need to come before GeometryCollection
    */
      if (g instanceof Point) {
          writePoint(propertyel, (Point) g, isnotmult);
      } else if (g instanceof LinearRing) {
          writeLinearRing(propertyel, (LinearRing) g, isnotmult);
      } else if (g instanceof LineString) {
          writeLineString(propertyel, (LineString) g, isnotmult);
      } else if (g instanceof Polygon) {
          writePolygon(propertyel, (Polygon) g, isnotmult);
      } else if (g instanceof MultiPoint) {
          writeMultiPoint(propertyel, (MultiPoint) g);
      } else if (g instanceof MultiLineString) {
          writeMultiLineString(propertyel, (MultiLineString) g);
      } else if (g instanceof MultiPolygon) {
          writeMultiPolygon(propertyel, (MultiPolygon) g);
      } else if (g instanceof GeometryCollection) {
        writeGeometryCollection(propertyel, (GeometryCollection) g);
      }
      // throw an error for an unknown type?
  }

  private void addAttributes(Element geo) {
    if (gid != null) {
    	try {
    		geo.addAttribute("gid", gid);
    	}
    	catch(Exception e) {}
    }
    if (srsName != null) {
    	try {
    		geo.addAttribute("srsName", srsName);
    		}
    	catch(Exception e) {}
    }
  }

  //<gml:Point><gml:coordinates>1195156.78946687,382069.533723461</gml:coordinates></gml:Point>
  private void writePoint(Element propertyel, Point p, boolean isnotmult) {
      try {
      	Element pointel = propertyel.addElement(namepoint);
      	addAttributes(pointel);
      	Element coordsel = pointel.addElement(coordQName);
      	coordsel.addText(writeCoords(new Coordinate[] {p.getCoordinate()}));
      }
      catch(Exception e) {}
  }

  //<gml:LineString><gml:coordinates>1195123.37289257,381985.763974674 1195120.22369473,381964.660533343 1195118.14929823,381942.597718511</gml:coordinates></gml:LineString>
  private void writeLineString(Element propertyel, LineString ls, boolean isnotmult) {
    try {
      	Element linestringel = propertyel.addElement(namelinestring);
      	if(isnotmult)addAttributes(linestringel);
      	Element coordsel = linestringel.addElement(coordQName);
      	coordsel.addText(writeCoords(ls.getCoordinates()));
      }
      catch(Exception e) {}
  }

  //<gml:LinearRing><gml:coordinates>1226890.26761027,1466433.47430292 1226880.59239079,1466427.03208053...></coordinates></gml:LinearRing>
  private void writeLinearRing(Element propertyel, LinearRing lr, boolean isnotmult) {
    try {
    	Element linearringel = propertyel.addElement(namelinearring);
      	if(isnotmult)addAttributes(linearringel);
      	Element coordsel = linearringel.addElement(coordQName);
      	coordsel.addText(writeCoords(lr.getCoordinates()));
      }
      catch(Exception e) {}
  }
  
  private void writePolygon(Element propertyel, Polygon p, boolean isnotmult) {
    try {
      	Element polygonel = propertyel.addElement(namepolygon);
      	if(isnotmult)addAttributes(polygonel);
      	
      	Element outerboundaryel = polygonel.addElement(nameouterboundary);
      	writeLinearRing(outerboundaryel, (LinearRing) p.getExteriorRing(), false);
      	
      	for (int t = 0; t < p.getNumInteriorRing(); t++) {
      		Element innerboundaryel = polygonel.addElement(nameinnerboundary);
          	writeLinearRing(innerboundaryel, (LinearRing) p.getInteriorRingN(t), false);
          }
      }
      catch(Exception e) {}
  }

  private void writeMultiPoint(Element propertyel, MultiPoint mp) {
  	try {
  		Element multipointel = propertyel.addElement(namemultipoint);
  		addAttributes(multipointel);
  		for (int t = 0; t < mp.getNumGeometries(); t++) {
  			Element pointmemberel = multipointel.addElement(namepointmember);
  			writePoint(pointmemberel, (Point) mp.getGeometryN(t), false);
  		}
  	}
  	catch(Exception e) {}
  }

  private void writeMultiLineString(Element propertyel, MultiLineString mls) {
	try {
  		Element multilinestringel = propertyel.addElement(namemultilinestring);
  		addAttributes(multilinestringel);
  		for (int t = 0; t < mls.getNumGeometries(); t++) {
  			Element linestringmemberel = multilinestringel.addElement(namelinestringmember);
  			writeLineString(linestringmemberel, (LineString) mls.getGeometryN(t), false);
  		}
  	}
  	catch(Exception e) {}
  }

  private void writeMultiPolygon(Element propertyel, MultiPolygon mp) {
    try {
  		Element multipolygonel = propertyel.addElement(namemultipolygon);
  		addAttributes(multipolygonel);
  		for (int t = 0; t < mp.getNumGeometries(); t++) {
  			Element polygonmemberel = multipolygonel.addElement(namepolygonmember);
  			writePolygon(polygonmemberel, (Polygon) mp.getGeometryN(t), false);
  		}
  	}
  	catch(Exception e) {}
  }

  private void writeGeometryCollection(Element propertyel, GeometryCollection gc) {
    try {
  		Element multigeometryel = propertyel.addElement(namemultigeometry);
  		addAttributes(multigeometryel);
  		for (int t = 0; t < gc.getNumGeometries(); t++) {
  			Element geometrymemberel = multigeometryel.addElement(namegeometrymember);
  			writeGeometry(geometrymemberel, gc.getGeometryN(t), false);
  		}
  	}
  	catch(Exception e) {}
  }

  /**
   * Takes a list of coordinates and converts it to GML.<br>
   * 2d and 3d aware.
   * Terminates the coordinate output with a newline.
   *@param cs array of coordinates
   */

  private String writeCoords(Coordinate[] coords) {
    StringBuffer buf = new StringBuffer();
    int dim = 2;

    if (coords.length > 0) {
      if (!(Double.isNaN(coords[0].z)))
         dim = 3;
    }

    for (int i = 0; i < coords.length; i++) {
      if (dim == 2) {
        buf.append(coords[i].x);
        buf.append(coordinateSeparator);
        buf.append(coords[i].y);
      } else if (dim == 3) {
        buf.append(coords[i].x);
        buf.append(coordinateSeparator);
        buf.append(coords[i].y);
        buf.append(coordinateSeparator);
        buf.append(coords[i].z);
      }
      buf.append(tupleSeparator);
    }
    return buf.toString();
  }
}
