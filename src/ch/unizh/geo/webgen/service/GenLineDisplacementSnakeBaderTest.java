package ch.unizh.geo.webgen.service;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DecimalFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Iterator;

import java.io.*;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.axpand.jaxpand.genoperator.common.IVisualisationUtility;
import com.axpand.jaxpand.genoperator.common.SignatureWidth;


/**
 * Operator Implementation for the line displacement algorithm.
 * @author	M. Wittensoeldner
 * @date	Created on 26.12.2006
 *
 */
public class GenLineDisplacementSnakeBaderTest
{
	protected LinkedList	_lstLines = new LinkedList();
	protected LinkedList	_lstLinesShifted = new LinkedList();
	protected LinkedList	_lstAreas = new LinkedList();
	protected int			_nMaxIterations;
	protected double		_dEnergyThreshold;
	
	protected boolean		_bStraightness;
	protected double		_dA;
	protected double		_dI;
	protected double		_dE;
	protected double		_dGamma;
	protected double		_dForceFactor;
	
	double[][] stiffMat;
	double[] forceMat;
	String _root;
	Boolean _outSet;
	
	public Coordinate[][]	_coordVects;
	
	IVisualisationUtility	_visualisationUtility;
	
	
	/**
	 * Constructor
	 */
	public GenLineDisplacementSnakeBaderTest(IVisualisationUtility visualisationUtility)
	{
		_visualisationUtility = visualisationUtility;
	}
	
	public LinkedList getResult(){
		return _lstLines;		
	}
	
	public void writeOutput(String root, String output){
		FileWriter writer;
		File file;		
		file = new File(root);
		try {
			if (_outSet == false){
				 writer = new FileWriter(file);
				 _outSet = true;
			}
			else{				
				writer = new FileWriter(file ,true);
			}			
			writer.write(output);
			writer.write(System.getProperty("line.separator"));
			writer.flush();		       
		    writer.close();
		} catch (IOException e) {
		      e.printStackTrace();
		}
	}
	
	/**
	 * Print the stiffness matrix into a *.txt-file
	 */		
	public void writeStiffMat(){
		//Origin of this adapted code: http://blog.mynotiz.de/programmieren/java-text-in-eine-datei-schreiben-450/
		FileWriter writer;
		File file;
		
		file = new File("/media/Speicher/Programierung/WebGen/Verdraengung/Ausgaben/StiffnessMatrix.txt");
		
		try {						
		       new FileWriter(file); //- overwrite, if the file already exists
		       writer = new FileWriter(file ,true);
		       writer.write("Dies ist die Stiffness Matrix!");
		       writer.write(System.getProperty("line.separator"));
		       writer.write("!Achtung! Die Werte sind auf 4 Nachkommastellen gerundet");
		       writer.write(System.getProperty("line.separator"));
		       writer.write(System.getProperty("line.separator"));
		       writer.write("| ");
		       DecimalFormat df = new DecimalFormat( "0.0000" );		       
		       
		       for (int k = 0; k < stiffMat.length; ++k) {
				     for (int l = 0; l < stiffMat[k].length; ++l) {
				        String s = df.format( stiffMat[k][l] );
				        writer.write(s + " ");
				     }
				     writer.write("| ");
				     writer.write(System.getProperty("line.separator"));
				     writer.write("|");
				}		       
		       // writes the stream in the output
		       writer.flush();		       
		       // closes the stream
		       writer.close();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
	}
	/*public void writeCorrectionVector(){
		int g=1;
        FileWriter writer;
		File file;		    		
		file = new File("/home/klammer/workspace/CorrctionVector.txt");
		new FileWriter(file); //- overwrite, if the file already exists
		writer = new FileWriter(file ,true);
		writer.write("Correction Vector");
		try {	
    		writer.write(System.getProperty("line.separator"));		 		      
		       writer.write(System.getProperty("line.separator"));
		       writer.write("Line number "+g);
		       for (int i=0; i<arrCorrVectors.length; i++){		 		    	 
		    	  writer.write(System.getProperty("line.separator"));
		    	  writer.write(arrCorrVectors[i].x +", "+arrCorrVectors[i].y);
		       }
				 writer.write(System.getProperty("line.separator"));		 				     
						       
		       // writes the stream in the output
		       		       
		       // closes the stream
		       
		    } catch (IOException e) {
		      e.printStackTrace();
		    } 
		writer.flush();
        writer.close();
    	g++;
	}*/
	
	public void writeStiffMatXML(String root){
		//Origin of this adapted code: http://www.ibiblio.org/xml/books/xmljava/chapters/ch03s03.html		
		try { 
			OutputStream fout= new FileOutputStream(root);//"/home/klammer/workspace/StiffMat.xml");
	        OutputStream bout= new BufferedOutputStream(fout);
	        OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");
	        DecimalFormat df = new DecimalFormat( "0.0000" );
	      
	        out.write("<?xml version=\"1.0\" ");
	        out.write("encoding=\"ISO-8859-1\"?>\r\n");  
	        out.write("<StiffnessMatrix>\r\n"); 
	        for (int k = 0; k < stiffMat.length; ++k) {	 
	        	out.write("  <Line"+k+">");	        	
	        	for (int l = 0; l < stiffMat[k].length; ++l) {
	        		out.write("<E"+l+">");
			        String s = df.format( stiffMat[k][l] );
			        out.write(s);	
			        out.write("</E"+l+">");
			     }
	        	 out.write("  </Line"+k+">\r\n");
	        }
	        out.write("</StiffnessMatrix>\r\n"); 
	        
	        out.flush();  // Don't forget to flush!
	        out.close();
	      }
	      catch (UnsupportedEncodingException e) {
	        System.out.println(
	         "This VM does not support the Latin-1 character set."
	        );
	      }
	      catch (IOException e) {
	        System.out.println(e.getMessage());        
	      }  
		
	}	
		
	public void writeForceMat(){
		FileWriter writer;
		File file;
		file = new File("/media/Speicher/Programierung/WebGen/Verdraengung/Ausgaben/ForceMatrix.txt");
		
		try {						
		       new FileWriter(file); //- falls die Datei bereits existiert wird diese überschrieben
		       writer = new FileWriter(file ,true);
		       writer.write("Dies ist die Force Matrix!");
		       writer.write(System.getProperty("line.separator"));
		       writer.write(System.getProperty("line.separator"));
		       writer.write("| ");
		        
		       for (int k = 0; k < forceMat.length; ++k) {
				     
				       writer.write(forceMat[k] + " ");		     
				     
				}
		       writer.write("|");		       
		       // Schreibt den Stream in die Datei
		       writer.flush();		       
		       // Schließt den Stream
		       writer.close();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
	}

	/**
	 * Initializes the operator.
	 * @return void
	 * @param nMaxIterations					The maximal number of iterations.
	 * @param dEnergyThreshold					The aspired energy difference between two iterations to break the iteration. -1 when nMaxIterations is used.
	 */
	public void init(int nMaxIterations,
						double dEnergyThreshold)
	{
		_nMaxIterations = nMaxIterations;
		_dEnergyThreshold = dEnergyThreshold;
		_lstLines.clear();
		_lstLinesShifted.clear();
		_lstAreas.clear();
		
		// set default values
		_dA = 1.0;
		_dI = 1000.0;
		_dE = 1.0;
		_dGamma = 1.0;
		_dForceFactor = 1.0;
		_bStraightness = true;
		
		_root = "/media/Speicher/Programierung/WebGen/Verdraengung/Ausgaben/Output.txt";
		_outSet = false;
	}

	/**
	 * Adds a line to the set of lines to displace.
	 * @return void
	 * @param lineString				The line to be added to the set of lines to displace.
	 * @param dSigWidth					The signature width.
	 * @param dMinDist					The minimal distance to an other object.
	 * @param bDeformable				The line is deformable.
	 * @param nPriority					The lines priority.
	 * @param bCopyCoordinates			Copies the original coordinates.
	 */
	public void addDataLine(LineString lineString, SignatureWidth dSigWidth, Double dMinDist, Boolean bDeformable, Integer nPriority, Boolean bCopyCoordinates)
	{
		writeOutput(_root, "addDataLine");
		Boolean[] bSuccess = {new Boolean(false)};
		LineString lineStringShifted = lineString;
		if (dSigWidth != null)
		{
			if (bCopyCoordinates != null)
				lineStringShifted = dSigWidth.shiftLine(lineString, bCopyCoordinates.booleanValue(), true, bSuccess);
			else
				lineStringShifted = dSigWidth.shiftLine(lineString, true, true, bSuccess);
		}
		_lstLines.addLast(new BeamObject(lineStringShifted, dSigWidth, dMinDist, bDeformable, nPriority, bCopyCoordinates));
		_lstLinesShifted.add(bSuccess[0]);
	}

	/**
	 * Adds an area to the set of fixed areas.
	 * @return void
	 * @param polygon					The polygon to be added to the set of fixed areas.
	 * @param dSigWidth					The signature width.
	 * @param dMinDist					The minimal distance to an other object.
	 */
	public void addDataPolygon(Polygon polygon, Double dSigWidth, Double dMinDist)
	{
		_lstAreas.addLast(new BeamObject(polygon, dSigWidth, dMinDist, null, null, null));
	}

	/**
	 * Executes the operator.
	 * @return boolean					True when successful.
	 */
	public boolean execute()
	{
		System.out.println("nun sind wir im 'Execute' der Grundlagenklasse der Snakeverschiebung");
		writeOutput(_root, "nun sind wir im 'Execute' der Grundlagenklasse der Snakeverschiebung");
		try
		{
			boolean bResult = false;
			// 1. detect partner points of lines
			writeOutput(_root, "***1. detect partner points of lines");
			this.detectPartnerPoints(true);
			// 2. detect partner points of areas
			writeOutput(_root, "***2. detect partner points of areas");
			this.detectPartnerPoints(false);
			// 3. detect junctions
			writeOutput(_root, "***3. detect junctions");
			int nNofJunctions = this.detectJunctions();
			// 4. create stiffness matrix
			writeOutput(_root, "***4. create stiffness matrix");
			double[][] matK = this.createStiffnessMatrix(nNofJunctions);
				// 4.2. save stiffness matrix global
				stiffMat = matK;				
			// 5. create force matrix
			writeOutput(_root, "***5. create force matrix");
			double[] matF = this.createForceMatrix(matK);
				// 5.2. save force matrix global
				forceMat = matF;				
			// 6. manipulate matrices to handle junctions
			writeOutput(_root, "***6. manipulate matrices to handle junctions");
			this.reorganizeJunctions(matK, matF, nNofJunctions, _dGamma);
			// 7. create cholesky decomposition
			writeOutput(_root, "***7. create cholesky decomposition");
			if (this.calcCholeskyDecomposition(matK))
			{
				// 8. calculates the displaced coordinates by iteration
				writeOutput(_root, "***8. calculates the displaced coordinates by iteration");
				this.calcIteration(matK, matF, nNofJunctions);
				bResult = true;
			}
			else
			{
				System.out.println("GenLineDisplacementBeam.execute(): Matrix is not positive definite.");
			}
			
			// 9. shift lines back (asymmetric signature width)
			writeOutput(_root, "9. shift lines back (asymmetric signature width)");
			Iterator iterLines = _lstLines.iterator();
			Iterator iterShift = _lstLinesShifted.iterator();
			Boolean bShifted;
			BeamObject line;
			while (iterLines.hasNext() && iterShift.hasNext())
			{
				line = (BeamObject)iterLines.next();
				bShifted = (Boolean)iterShift.next();
				if (bShifted.booleanValue())
				{
					line._dSigWidth.shiftLineBack((LineString)line._geom, false, true, null);
				}
			}
			return bResult;
		}
		catch (Exception ex)
		{
			System.out.println("Line displacement failed. " + ex.getMessage());
			ex.printStackTrace();
		}
		catch (Error er)
		{
			er.printStackTrace();
		}
		return false;
	}

	/**
	 * Gets the displaced line.
	 * @return LineString				The displaced line.
	 * @param nIndex					The line index.
	 */
	public LineString getDataLine(int nIndex)
	{
		writeOutput(_root, "getDataLine");
		BeamObject line;
		if (nIndex < _lstLines.size())
		{
			line = (BeamObject)_lstLines.get(nIndex);
			if (line._geom instanceof LineString)
			{
				LineString lineString = (LineString)line._geom;
				return lineString;
			}
		}
		return null;
	}
	
	
    /**
	 * Calculates the result iterative.
	 * @return void
	 * @param matK						The stiffness matrix.
	 * @param matF						The force matrix.
	 * @param nNofJunctions				The number of junctions.
	 */
	protected void calcIteration(double[][] matK, double[] matF, int nNofJunctions)
	{
		writeOutput(_root, "calcIteration");
		Iterator iterLines;
        BeamObject line;
        Coordinate[] arrCorrVectors;
        int nOffset;
		int nIterationCount = 0;
        double[] f = new double[matK.length];
        double[] mLastIteration = new double[matK.length];
        Arrays.fill(mLastIteration, 0.0);
		boolean bRun = true;
		double dForceOld;
		double dForce = Double.MAX_VALUE;
		while (bRun)
		{
			try
			{
				dForceOld = dForce;
				dForce = 0.0;
				nOffset = 0;
		        Arrays.fill(f, 0.0);
		        iterLines = _lstLines.iterator();
		        
	    		while (iterLines.hasNext())		        
		        {
		        	line = (BeamObject)iterLines.next();
		        	arrCorrVectors = this.computeCorrectionVectors(line);
		        	
			        nOffset = this.fillForceVector(line, arrCorrVectors, f, _dForceFactor, nOffset);
		        	
		        }
	    		
		        // calc force
		        int j = 0;
		        while (j < f.length)
		        	dForce += Math.abs(f[j++]);

		        if (_visualisationUtility != null)
		        {
		        	GeometryFactory factory = new GeometryFactory();
		        	int nSize = 0;
		        	double dFactor = 10;
		        	iterLines = _lstLines.iterator();
		        	while (iterLines.hasNext())
		        	{
		        		nSize += ((BeamObject)iterLines.next())._arrCoords.size();
		        	}
		        	LineString[] lineStrings = new LineString[nSize];
		        	int i = 0;
		        	iterLines = _lstLines.iterator();
		        	while (iterLines.hasNext())
		        	{
		        		line = (BeamObject)iterLines.next();
		        		factory = line._geom.getFactory();
		        		int k = 0;
		        		while (k < line._arrCoords.size())
		        		{
			        		Coordinate[] coords = new Coordinate[2];
		        			coords[0] = (Coordinate)((BeamPoint)line._arrCoords.get(k))._coordCur.clone();
		        			coords[1] = new Coordinate(coords[0].x+dFactor*f[i*2], coords[0].y+dFactor*f[i*2+1]);
			        		lineStrings[i] = factory.createLineString(coords);
			        		i++;
		        			k++;
		        		}
		        	}
		        	_visualisationUtility.addToDebugBagGeometry(factory.createMultiLineString(lineStrings), "Vectors "+nIterationCount);
		        }

		        this.reorganizeForceJunctions(f, nNofJunctions);
		        this.constructEquationVector(f, matF, mLastIteration);
		        // solve equation system
		        this.solve(matK, f, mLastIteration);
		        this.applyChanges(mLastIteration, nNofJunctions);
		        
		        nIterationCount++;

		        if (_visualisationUtility != null)
		        {
		        	GeometryFactory factory = new GeometryFactory();
		        	LineString[] lineStrings = new LineString[_lstLines.size()];
		        	int i = 0;
		        	iterLines = _lstLines.iterator();
		        	while (iterLines.hasNext())
		        	{
		        		line = (BeamObject)iterLines.next();
		        		factory = line._geom.getFactory();
		        		Coordinate[] coords = new Coordinate[line._arrCoords.size()];
		        		int k = 0;
		        		while (k < coords.length)
		        		{
		        			coords[k] = (Coordinate)((BeamPoint)line._arrCoords.get(k))._coordCur.clone();
		        			k++;
		        		}
		        		lineStrings[i] = factory.createLineString(coords);
		        		i++;
		        	}
		        	_visualisationUtility.addToDebugBagGeometry(factory.createMultiLineString(lineStrings), "Iteraion "+nIterationCount);
		        }

		        if (nIterationCount >= _nMaxIterations)
		        	bRun = false;
		        if (Math.abs(dForce-dForceOld) < _dEnergyThreshold)
		        	bRun = false;
		        if (dForceOld-dForce < 0.0)
		        {
		        	// set old coordinates
		        	this.applyOldCoordinates();
		        	bRun = false;
		        }
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				bRun = false;
			}
		}
		writeOutput(_root, "Iterations: " + nIterationCount);
		this.createResultGeometry();
	}

	/**
	 * Applies the coordinate changes to the input geometry.
	 * @return void
	 */
	protected void createResultGeometry()
	{
		writeOutput(_root, "createResultGeometry");
		int i;
		BeamObject line;
		Coordinate[] coords;
		Iterator iterLines = _lstLines.iterator();
		while (iterLines.hasNext())
		{
			line = (BeamObject)iterLines.next();
			coords = line._geom.getCoordinates();
			i = 0;
			while (i < line._arrCoords.size())
			{
				coords[i].setCoordinate(((BeamPoint)line._arrCoords.get(i))._coordCur);
				i++;
			}
			line._geom.geometryChanged();
		}
	}
	
	/**
	 * Calculates the cholesky decomposition of a matrix.
	 * @return boolean					True wheter the matrix is symmetric positive definite.
	 * @param mK						The matrix to decomposite.
	 */
	protected boolean calcCholeskyDecomposition(double[][] mK)
	{
		writeOutput(_root, "calcCholeskyDecomposition");
		boolean bResult = true;
		int i;
		int k;
		int j;
        if (mK[0][0] < 0.0)
        	bResult = false;
        else
        	mK[0][0] = Math.sqrt(mK[0][0]);
        
        i = 1;
        while ((i < mK.length) && bResult)
        {
        	k = 0;
        	while (k <= i-1)
        	{
                mK[i][k] = mK[i][k];
                j = 0;
                while (j <= k-1)
                {
                    mK[i][k] -= mK[i][j] * mK[k][j];
                	j++;
                }
                mK[i][k] /= mK[k][k];
        		k++;
        	}
        	j = 0;
        	while (j <= i-1)
        	{
                mK[i][i] -= mK[i][j] * mK[i][j];
        		j++;
        	}

            if (mK[i][i] < 0.0)
            	bResult = false;
            else
            	mK[i][i] = Math.sqrt(mK[i][i]);
        	i++;
        }
		return bResult;
	}
	   
	/**
     * Solves the equation Ax + b = 0, where A = L L'
	 * @return void						True wheter the matrix is single positive definite.
	 * @param mL						The lower triangle matrix.
	 * @param mB						The vector b.
	 * @param mX						The result vector.
	 */
    protected void solve(double[][] mL, double[] mB, double[] mX)
    {
    	writeOutput(_root, "solve");
    	int i;
    	int k;
    	double dValue;
        double[] mC = new double[mL.length];

        // solving Lc + b = 0
        i = 0;
        while (i < mX.length)
        {
        	dValue = 0.0;
        	k = 0;
        	while (k < i)
        	{
        		dValue += mL[i][k] * mC[k];
        		k++;
        	}
            mC[i] = (mB[i] - dValue) / mL[i][i];
        	i++;
        }

        //  solving L'x + c = 0
        i = mX.length-1;
        while (i >= 0)
        {
        	dValue = 0.0;
        	k = i + 1;
        	while (k < mX.length)
        	{
                dValue += mL[k][i] * mX[k];
        		k++;
        	}
            mX[i] = (-mC[i] - dValue) / mL[i][i];
        	i--;
        }
    }

    /**
	 * Applies the coordinate changes.
	 * @return void
	 * @param matIteration				The result of the iteration.
	 * @param nNofJunctions				The number of junctions.
	 */
	protected void applyChanges(double[] matIteration, int nNofJunctions)
	{
		writeOutput(_root, "applyChanges");
		int i;
		BeamObject line;
		BeamPoint point;
		int nOffset = 0;
		int nInternalOffset;
		Iterator iterLines = _lstLines.iterator();
		while (iterLines.hasNext())
		{
			line = (BeamObject)iterLines.next();
			i = 0;
			while (i < line._arrCoords.size())
			{
				point = (BeamPoint)line._arrCoords.get(i);
				nInternalOffset = nOffset * 2;
				if (point._nJunctionIndex != -1)
				{
					nInternalOffset = matIteration.length-nNofJunctions*2+point._nJunctionIndex*2;
				}
				point._coordOld.setCoordinate(point._coordCur);
// todo: add junction corrections
				point._coordCur.x = point._coordNull.x + matIteration[nInternalOffset];
				point._coordCur.y = point._coordNull.y + matIteration[nInternalOffset+1];
				nOffset++;
				i++;
			}
		}
	}    
    
    /**
	 * Applies the last (old) coordinates.
	 * @return void
	 */
	protected void applyOldCoordinates()
	{
		writeOutput(_root, "applyOldCoordinates");
		int i;
		BeamObject line;
		BeamPoint point;
		Iterator iterLines = _lstLines.iterator();
		while (iterLines.hasNext())
		{
			line = (BeamObject)iterLines.next();
			i = 0;
			while (i < line._arrCoords.size())
			{
				point = (BeamPoint)line._arrCoords.get(i);
				point._coordCur.setCoordinate(point._coordOld);
				i++;
			}
		}
	}    

	/**
	 * Constructs the right handed side of the equation (b = last_iteration + gamma * f). b is stored in f.
	 * @return void
	 * @param f							The iterative force matrix.
	 * @param matF						The global force matrix.
	 * @param matLastIteration			The result of the last iteration.
	 */
	protected void constructEquationVector(double[] f, double[] matF, double[] matLastIteration)
	{
		writeOutput(_root, "constructEquationVector");
		int i = 0;
		while (i < f.length)
		{
			f[i] -= matF[i];
			f[i] = -(matLastIteration[i] + _dGamma * f[i]);
			i++;
		}
	}
	
    /**
	 * Reorganizes the the force matrix. Replace junction dependant values.
	 * @return void
	 * @param f							The force matrix.
	 * @param nJunctionCount			The number of junctions.
	 */
	protected void reorganizeForceJunctions(double[] f, int nJunctionCount)
	{
		writeOutput(_root, "reorganizeForceJunctions");
		int i;
		int k;
		int iStart;
		int nOffset = f.length - nJunctionCount*2;
		int nPointCount = 0;
		BeamPoint point;
		BeamObject line;
		Iterator iterLines = _lstLines.iterator();
		while (iterLines.hasNext())
		{
			line = (BeamObject)iterLines.next();
			i = 0;
			while (i < line._arrCoords.size())
			{
				point = (BeamPoint)line._arrCoords.get(i);
				if (point._nJunctionIndex != -1)
				{
			        // move junction related forces down
					iStart = nPointCount*2;
					k = iStart;
					while (k < (iStart+2))
					{
						f[nOffset+point._nJunctionIndex*2+k-iStart] += f[k];
						f[k] = 0.0;
						k++;
					}
				}
				nPointCount++;
				i++;
			}
		}
	}
	
    /**
	 * Fills the force vector with correction related data.
	 * @return int						The new point offset.
	 * @param line						The line to fill the values for.
	 * @param arrCorrVectors			The lines correction vectors.
	 * @param f							The force vector to fill.
	 * @param dFactor					The force factor.
	 * @param nOffset					The actual point offset.
	 */
    protected int fillForceVector(BeamObject line, Coordinate[] arrCorrVectors, double[] f, double dFactor, int nOffset)
    {
    	writeOutput(_root, "fillForceVector");
    	double dDistance;
    	int i = 0;
    	while (i < line._arrCoords.size()-1)
    	{
    		dDistance = ((BeamPoint)line._arrCoords.get(i))._coordCur.distance(((BeamPoint)line._arrCoords.get(i+1))._coordCur);
    		f[nOffset*2] += dDistance / 2.0 * arrCorrVectors[i].x * dFactor;
    		f[nOffset*2+1] += dDistance / 2.0 * arrCorrVectors[i].y * dFactor;
    		f[nOffset*2+2] += dDistance / 2.0 * arrCorrVectors[i+1].x * dFactor;
    		f[nOffset*2+3] += dDistance / 2.0 * arrCorrVectors[i+1].y * dFactor;
    		nOffset++;
    		i++;
    	}
		nOffset++;
    	return nOffset;
    }
    
    
    /**
	 * Computes the shortest distance from a point to the junction of both lines.
	 * @return double					The calculated distance.
	 * @param line1						The first line.
	 * @param nPointIndex1				The point index on the first line.
	 * @param line2						The second line.
	 * @param nPointIndex2				The point index on the second line.
	 */
    protected double getShortestDistanceToJunction(BeamObject line1, int nPointIndex1, BeamObject line2, int nPointIndex2)
    {
    	writeOutput(_root, "getShortestDistanceToJunction");
    	double dDistance = Double.MAX_VALUE;
    	double dHardCoreDistance = this.calcHardCoreDistance(line1, line2);
    	if (((BeamPoint)line1._arrCoords.get(0))._coordCur.distance(((BeamPoint)line2._arrCoords.get(0))._coordCur) < dHardCoreDistance)
    	{
    		// intersection
    		dDistance = Math.min(dDistance, this.getSubLineDistance(line1, 0, nPointIndex1));
    		dDistance = Math.min(dDistance, this.getSubLineDistance(line2, 0, nPointIndex2));
    	}
    	if (((BeamPoint)line1._arrCoords.get(0))._coordCur.distance(((BeamPoint)line2._arrCoords.get(line2._arrCoords.size()-1))._coordCur) < dHardCoreDistance)
    	{
    		// intersection
    		dDistance = Math.min(dDistance, this.getSubLineDistance(line1, 0, nPointIndex1));
    		dDistance = Math.min(dDistance, this.getSubLineDistance(line2, nPointIndex2, line2._arrCoords.size()-1));
    	}
    	if (((BeamPoint)line1._arrCoords.get(line1._arrCoords.size()-1))._coordCur.distance(((BeamPoint)line2._arrCoords.get(0))._coordCur) < dHardCoreDistance)
    	{
    		// intersection
    		dDistance = Math.min(dDistance, this.getSubLineDistance(line1, nPointIndex1, line1._arrCoords.size()-1));
    		dDistance = Math.min(dDistance, this.getSubLineDistance(line2, 0, nPointIndex2));
    	}
    	return dDistance;
    }

    /**
	 * Computes the distance on a line.
	 * @return double					The calculated distance.
	 * @param line						The line to calculate the distance on.
	 * @param nIndexStart				The start index to measure.
	 * @param nIndexEnd					The end index to measure.
	 */
    protected double getSubLineDistance(BeamObject line, int nIndexStart, int nIndexEnd)
    {
    	writeOutput(_root, "getSubLineDistance");
    	double dDistance = 0.0;
    	int i = nIndexStart+1;
    	while (i <= nIndexEnd)
    	{
    		dDistance += ((BeamPoint)line._arrCoords.get(i))._coordCur.distance(((BeamPoint)line._arrCoords.get(i-1))._coordCur);
    		i++;
    	}
    	return dDistance;
    }


    /**
	 * Computes the correction vectors for each point on a line.
	 * @return Coordinate[]				The correction vectors for each point.
	 * @param line						The line.
	 */
    protected Coordinate[] computeCorrectionVectors(BeamObject line)
    {
    	writeOutput(_root, "computeCorrectionVectors");
    	LineSegment segment = new LineSegment();
    	BeamRef ref;
    	BeamPoint point;
        double dHardCoreDistance;
    	double dDistance;
    	double dSeverity;
        double dXAmount;
    	boolean bAdd;
    	Double dDist;
    	Map.Entry entry;
    	Coordinate coordTemp = new Coordinate();
    	HashMap mapClosePoints = new HashMap();
    	HashMap mapCloseDistances = new HashMap();
        Coordinate[] arrCorrVectors = new Coordinate[line._arrCoords.size()];
        int i = 0;
        while (i < line._arrCoords.size())
        {
        	arrCorrVectors[i] = new Coordinate(0.0, 0.0);
        	point = (BeamPoint)line._arrCoords.get(i);
        	// 1. consider the fixed lines
        	if (point._lstAreaReferences.size() > 0)
        	{
        		// search for closest distance to each area
        		mapClosePoints.clear();
        		mapCloseDistances.clear();
        		Iterator iterRefs = point._lstAreaReferences.iterator();
        		while (iterRefs.hasNext())
        		{
        			ref = (BeamRef)iterRefs.next();
            		dHardCoreDistance = this.calcHardCoreDistance(line, ref._obj);
            		segment.p0 = ((BeamPoint)ref._obj._arrCoords.get(ref._nSegmentIndex))._coordCur;
            		segment.p1 = ((BeamPoint)ref._obj._arrCoords.get(ref._nSegmentIndex+1))._coordCur;
            		dDistance = segment.distance(point._coordCur);
            		if (dDistance <= dHardCoreDistance)
            		{
            			bAdd = true;
            			if ((dDist = (Double)mapCloseDistances.get(ref._obj)) != null)
            			{
                			bAdd = dDistance < dDist.doubleValue();
            			}
            			if (bAdd)
            			{
	            			mapClosePoints.put(ref._obj, segment.closestPoint(point._coordCur));
	            			mapCloseDistances.put(ref._obj, new Double(dDistance));
            			}
            		}
        		}
        	}
        	// 2. consider the moveable lines
        	if (point._lstLineReferences.size() > 0)
        	{
        		// search for closest distance to each line
        		mapClosePoints.clear();
        		mapCloseDistances.clear();
        		Iterator iterRefs = point._lstLineReferences.iterator();
        		while (iterRefs.hasNext())
        		{
        			ref = (BeamRef)iterRefs.next();
        			if (ref._obj != line)
        			{
	            		dHardCoreDistance = this.calcHardCoreDistance(line, ref._obj);
	            		segment.p0 = ((BeamPoint)ref._obj._arrCoords.get(ref._nSegmentIndex))._coordCur;
	            		segment.p1 = ((BeamPoint)ref._obj._arrCoords.get(ref._nSegmentIndex+1))._coordCur;
	            		dDistance = segment.distance(point._coordCur);
	            		if (dDistance <= dHardCoreDistance)
	            		{
	            			bAdd = true;
	            			if ((dDist = (Double)mapCloseDistances.get(ref._obj)) != null)
	            			{
	                			bAdd = dDistance < dDist.doubleValue();
	            			}
	            			if (bAdd)
	            			{
	            				if (this.getShortestDistanceToJunction(line, i, ref._obj, ref._nSegmentIndex) > this.calcHardCoreSpace(line, ref._obj))
	            				{
			            			mapClosePoints.put(ref._obj, segment.closestPoint(point._coordCur));
			            			mapCloseDistances.put(ref._obj, new Double(dDistance));
	            				}
	            			}
	            		}
        			}
        			else
        			{
        				// self displacement
        				// not yet supported
        			}
        		}
        	}
    		// calculate vector
    		Iterator iterClosePoints = mapClosePoints.entrySet().iterator();
    		while (iterClosePoints.hasNext())
    		{
    			entry = (Map.Entry)iterClosePoints.next();
        		dHardCoreDistance = this.calcHardCoreDistance(line, (BeamObject)entry.getKey());
    			coordTemp.setCoordinate((Coordinate)entry.getValue());
    			dDistance = coordTemp.distance(((BeamPoint)point)._coordCur);
    			coordTemp.x = ((BeamPoint)point)._coordCur.x - coordTemp.x;
    			coordTemp.y = ((BeamPoint)point)._coordCur.y - coordTemp.y;
                if (dDistance <= dHardCoreDistance)
                {
                	dSeverity = (dHardCoreDistance - dDistance) / dHardCoreDistance;
                    dXAmount = Math.abs(coordTemp.x) / (Math.abs(coordTemp.x) + Math.abs(coordTemp.y));
                    if (coordTemp.x != 0.0)
                    	arrCorrVectors[i].x += dSeverity * dXAmount * (coordTemp.x / Math.abs(coordTemp.x));
                    if (coordTemp.y != 0.0)
                    	arrCorrVectors[i].y += dSeverity * (1.0 - dXAmount) * (coordTemp.y / Math.abs(coordTemp.y));
                }
    		}
        	i++;
        }
        return arrCorrVectors;
    }

	/**
	 * Reorganizes the stiffness and the force matrix. Replace junction dependant values.
	 * @return void
	 * @param matK						The stiffness matrix.
	 * @param matF						The force matrix.
	 * @param nJunctionCount			The number of junctions.
	 * @param dGamma					The gamma value.
	 */
	protected void reorganizeJunctions(double[][] matK, double[] matF, int nJunctionCount, double dGamma)
	{
		writeOutput(_root, "reorganizeJunctions");
		int i;
		int k;
		int j;
		int iStart;
		int nPointCount = 0;
		int nOffset = matK.length - nJunctionCount*2;
		BeamPoint point;
		BeamObject line;
		Iterator iter = _lstLines.iterator();
		while (iter.hasNext())
		{
			line = (BeamObject)iter.next();
			i = 0;
			while (i < line._arrCoords.size())
			{
				point = (BeamPoint)line._arrCoords.get(i);
				if (point._nJunctionIndex != -1)
				{
					iStart = nPointCount*2;
					// 1. move junction related values to right
					k = 0;
					while (k < matK.length)
					{
						j = iStart;
						while (j < (iStart+2))
						{
							matK[k][nOffset+point._nJunctionIndex*2+j-iStart] += matK[k][j];
							matK[k][j] = 0.0;
							j++;
						}
						k++;
					}
			        // 2. move junction related forces down
					iStart = nPointCount*2;
					k = iStart;
					while (k < (iStart+2))
					{
						matF[nOffset+point._nJunctionIndex*2+k-iStart] = matF[k];
						matF[k] = 0.0;
						k++;
					}
				}
				nPointCount++;
				i++;
			}
		}
		nPointCount = 0;
		iter = _lstLines.iterator();
		while (iter.hasNext())
		{
			line = (BeamObject)iter.next();
			i = 0;
			while (i < line._arrCoords.size())
			{
				point = (BeamPoint)line._arrCoords.get(i);
				if (point._nJunctionIndex != -1)
				{
					iStart = nPointCount*2;
					// 3. move junction related values down
					k = 0;
					while (k < matK.length)
					{
						j = iStart;
						while (j < (iStart+2))
						{
							matK[nOffset+point._nJunctionIndex*2+j-iStart][k] += matK[j][k];
							matK[j][k] = 0.0;
							if (k == j)
								matK[k][j] = 1.0;
							j++;
						}
						k++;
					}
				}
				nPointCount++;
				i++;
			}
		}

		// add gamma correction
		i = 0;
		while (i < matK.length)
		{
			k = 0;
			while (k < matK.length)
			{
				matK[i][k] *= dGamma;
				if (i == k)
					matK[i][k] += 1.0;	// Attraction therm
				k++;
			}
			i++;
		}
	}
	
	/**
	 * Detects the junction points of each line. Start and end point of a line can be a junction point.
	 * @return int						The total number of junctions.
	 */
	protected int detectJunctions()
	{
		writeOutput(_root, "detectJunctions");
		int nJunctionCount = 0;
		BeamRef ref;
		BeamPoint ptBorder;
		BeamObject line;
		int i;
		int k;
		int nIndex;
		double dDistance;
		Iterator iter = _lstLines.iterator();
		while (iter.hasNext())
		{
			line = (BeamObject)iter.next();
			i = 0;
			// 1. check start point
			ptBorder = (BeamPoint)line._arrCoords.get(0);
			while (i < 2)
			{
				Iterator iterRefs = ptBorder._lstLineReferences.iterator();
				while (iterRefs.hasNext())
				{
					ref = (BeamRef)iterRefs.next();
					if (ref._obj != line)
					{
						dDistance = this.calcMaxSigWidth(line, ref._obj) / 2.0;
						k = 0;
						nIndex = ref._nSegmentIndex;
						while (k < 2)
						{
							if (ptBorder._coordCur.distance(((BeamPoint)ref._obj._arrCoords.get(nIndex))._coordCur) < dDistance)
							{
								// junction found
								if ((ptBorder._nJunctionIndex == -1) && (((BeamPoint)ref._obj._arrCoords.get(nIndex))._nJunctionIndex == -1))
									ptBorder._nJunctionIndex = nJunctionCount++;
								else
								{
									if ((ptBorder._nJunctionIndex != -1) && (((BeamPoint)ref._obj._arrCoords.get(nIndex))._nJunctionIndex != -1))
									{
										if (ptBorder._nJunctionIndex != ((BeamPoint)ref._obj._arrCoords.get(nIndex))._nJunctionIndex)
											System.out.println("GenLineDisplacementBeam.detectJunctions(): Junction is registered twice!");
									}
									ptBorder._nJunctionIndex = Math.max(ptBorder._nJunctionIndex, ((BeamPoint)ref._obj._arrCoords.get(nIndex))._nJunctionIndex);
								}
								((BeamPoint)ref._obj._arrCoords.get(nIndex))._nJunctionIndex = ptBorder._nJunctionIndex; 
							}
							nIndex++;
							k++;
						}
					}
				}
				if (ptBorder._nJunctionIndex == -1)
				{
					ptBorder._nJunctionIndex = nJunctionCount++;
				}
				// 2. check end point
				ptBorder = (BeamPoint)line._arrCoords.get(line._arrCoords.size()-1);
				i++;
			}
		}
		return nJunctionCount;
	}
	
    /**
     * Creates the force matrix. Forces will enable the realignment of the junctions.
	 * @return double[][]				The force matrix.
	 * @return matK						The stiffness matrix.
	 */
	protected double[] createForceMatrix(double[][] matK)
	{
		writeOutput(_root, "createForceMatrix");
		double[] mF = new double[matK.length];
        Arrays.fill(mF, 0.0);
        return mF;
		
/*		double[] mD = new double[matK.length];
        Arrays.fill(mD, 0.0);
        BeamObject line;
        int nOffset = 0;
        Coordinate coordFirst;
        Coordinate coordLast;
        Iterator iter = _lstLines.iterator();
        while (iter.hasNext())
        {
        	line = (BeamObject)iter.next();
//        	if (((BeamPoint)line._arrCoords.get(0))._nJunctionIndex == -1)
        		coordFirst = new Coordinate(0.0, 0.0);
//        	else
// correction
//        	if (((BeamPoint)line._arrCoords.get(line._arrCoords.size()-1))._nJunctionIndex == -1)
        		coordLast = new Coordinate(0.0, 0.0);
//        	else
//        		 correction
        	
        	mD[nOffset] = coordFirst.x;
        	mD[nOffset+1] = coordFirst.y;
        	mD[nOffset+(line._arrCoords.size()-1)*3] = coordLast.x;
        	mD[nOffset+(line._arrCoords.size()-1)*3+1] = coordLast.y;
        	nOffset += line._arrCoords.size() * 3;
        }

//        Matrix matD = new Matrix(mD, mD.length);
//        Matrix matF = new Matrix(matK).times(matD);
		return this.multiply(matK, mD);*/
	}

    /**
     * Creates and filles the stiffness matrix that holds segment and junction data.
	 * @return double[][]				The stiffness matrix.
	 * @return nJunctionCount			The number of junctions.
	 */
	protected double[][] createStiffnessMatrix(int nJunctionCount)
	{
		writeOutput(_root, "createStiffnessMatrix");
		double[][] mat = null;
		int i;
		int k;
		int j;
		int nSize = 0;
		BeamObject line;
		LineSegment seg = new LineSegment();
        double[][] matSeg = new double[4][4];
        double dH;
        double dAlpha = 1.0;
        double dBeta = 1.0;
        double dA;
        double dB10;
        double dB60;
        
        
        int nOffset = 0;
		Iterator iter = _lstLines.iterator();
		while (iter.hasNext())
		{
			nSize += ((BeamObject)iter.next())._arrCoords.size();
		}
		nSize += nJunctionCount;
		// each point needs 2 cols/rows in the matrix
		nSize *= 2;
		mat = new double[nSize][nSize];

		// initializes the matrix with null values
		i = 0;
		while (i < nSize)
		{
			Arrays.fill(mat[i], 0.0);
			i++;
		}

		iter = _lstLines.iterator();
		while (iter.hasNext())
		{
			line = (BeamObject)iter.next();
            // straight line: - A, high I
            // sinuous road: - A, - I
            // big road: high A, high I
            // small road: small A, - I
            if (line._nDisplacementPriority != null)
            {
//                dFactorA *= Math.max(line._nDisplacementPriority.doubleValue(), 1.0) - 1.0 / 20.0 + 1.0;
//            	dFactorI *= Math.max(line._nDisplacementPriority.doubleValue(), 1.0) - 1.0 / 10.0 + 1.0;
            }
			i = 0;
			while (i < line._arrCoords.size()-1)
			{
				// build matrix per segment
				seg.p0 = ((BeamPoint)line._arrCoords.get(i))._coordCur;
				seg.p1 = ((BeamPoint)line._arrCoords.get(i+1))._coordCur;
				dH = seg.getLength();
				dA = dAlpha*dH*dH;
				dB10 = 10.0*dBeta;
				dB60 = 60.0*dBeta;

		        matSeg[0][0] = 6.0 * (dA + dB10) / (5.0 * dH*dH*dH);
		        matSeg[0][1] = matSeg[1][0] = (dA + dB60) / (10.0 * dH*dH);
		        matSeg[0][2] = matSeg[2][0] = -6.0 * (dA + dB10) / (5.0 * dH*dH*dH);
//
		        matSeg[0][3] = matSeg[3][0] = +matSeg[0][1];
		        matSeg[1][1] = 2.0 * (dA + 30.0 * dBeta) / (15.0 * dH);
		        matSeg[1][2] = matSeg[2][1] = -matSeg[0][1];
		        matSeg[1][3] = matSeg[3][1] = -(dA -dB60) / (30.0 * dH);
		        matSeg[2][2] =  matSeg[0][0];
//		        
		        matSeg[2][3] = matSeg[3][2] = -matSeg[0][1];
		        matSeg[3][3] = matSeg[1][1];

		        // copy the matrix into the big matrix
		        k = 0;
		        while (k < matSeg.length)
		        {
		        	j = 0;
		        	while (j < matSeg[k].length)
		        	{
		        		mat[nOffset+k][nOffset+j] += matSeg[k][j];
		        		j++;
		        	}
		        	k++;
		        }
		        nOffset += 2;
                i++;
			}
	        nOffset += 2;
		}
		
		return mat;
	}
	
	protected double calcStraightness(BeamObject line, int nIndexPoint)
	{
		writeOutput(_root, "calcStraightness");
		double dStraightness = 0.0;
	    double dStraightnessMax = 200.0;
	    double dTolerance = 5.0;
		double dStraightnessForward = 0.0;
		double dStraightnessBackward = 0.0;
        double dFraction = 1.0;
        int i;
    	double dDistCur = 0.0;
    	double dDistLast = 0.0;
    	LineSegment seg = new LineSegment();
    	seg.p0 = ((BeamPoint)line._arrCoords.get(nIndexPoint))._coordCur;
    	seg.p1 = ((BeamPoint)line._arrCoords.get(nIndexPoint+1))._coordCur;
		

		// calc the forward straightness
    	i = nIndexPoint + 2;
    	while ((i < line._arrCoords.size()) && (dFraction == 1.0))
	    {
	    	dDistCur = seg.distancePerpendicular(((BeamPoint)line._arrCoords.get(i))._coordCur);
            if (dDistCur > dTolerance)
                dFraction = (dTolerance - dDistLast) / (dDistCur - dDistLast);
            dStraightnessForward += ((BeamPoint)line._arrCoords.get(i-1))._coordCur.distance(((BeamPoint)line._arrCoords.get(i))._coordCur) * dFraction;
            dDistLast = dDistCur;
            i++;
        }

        if (dFraction == 1.0)
        	dStraightnessForward = dStraightnessMax;
        dStraightnessForward = Math.min(dStraightnessForward, dStraightnessMax);

		// calc the backward straightness
        i = nIndexPoint - 2;
        dFraction = 1.0;
    	dDistCur = 0.0;
    	dDistLast = 0.0;
        while ((i >= 0) && (dFraction == 1.0))
        {
	    	dDistCur = seg.distancePerpendicular(((BeamPoint)line._arrCoords.get(i))._coordCur);
            if (dDistCur > dTolerance)
            	dFraction = (dTolerance - dDistLast) / (dDistCur - dDistLast);
            dStraightnessBackward += ((BeamPoint)line._arrCoords.get(i+1))._coordCur.distance(((BeamPoint)line._arrCoords.get(i))._coordCur) * dFraction;
            dDistLast = dDistCur;
            i--;
        }

        if (dFraction == 1.0)
        	dStraightnessBackward = dStraightnessMax;
        dStraightnessBackward = Math.min(dStraightnessBackward, dStraightnessMax);
        
        dStraightness = dStraightnessForward + dStraightnessBackward + seg.getLength();
	    return dStraightness;
	}

	/**
	 * Detects line or area partners for each point.
	 * @return void
	 * @param bLine						True to look for line partners, false to look for area partners.
	 */
	public void detectPartnerPoints(boolean bLine)
	{
		writeOutput(_root, "detectPartnerPoints");
		BeamObject obj1;
		BeamObject obj2;
		Coordinate coordP;
		Coordinate coordL1;
		Coordinate coordL2;
		boolean bInterior;
		double dDistance = 0;
		double dHardCoreDepth;
		int i;
		int k; 
		Output writer = new Output();
		writer.setRoot("/media/Speicher/Programierung/WebGen/Verdraengung/Ausgaben/Output_detectPartnerPoints.txt");

		Iterator iterLine = _lstLines.iterator();
		while (iterLine.hasNext())	//loop through all linear geometries
		{
			obj1 = (BeamObject)iterLine.next();
			i = 0;
			while (i < obj1._arrCoords.size())	//loop through all points of the current line
			{
				writer.writeOut("LineNr:"+i+" bLine= "+bLine);
				//clear the references of the current point
				if (bLine) //bLine is true for linear and false for polygonal corresponding geometries
					((BeamPoint)obj1._arrCoords.get(i))._lstLineReferences.clear();
				else
					((BeamPoint)obj1._arrCoords.get(i))._lstAreaReferences.clear();
				coordP = ((BeamPoint)obj1._arrCoords.get(i))._coordCur;

				//initiate the iterator for corresponding geometries
				Iterator iter2Line = null;
				if (bLine) 
					iter2Line = _lstLines.iterator();
				else
					iter2Line = _lstAreas.iterator();
				writer.writeOut("	loop through all corresponding geometries");
				writer.writeOut("	iter2Line.hasNext()= "+iter2Line.hasNext());
				while (iter2Line.hasNext())	//loop through all corresponding geometries
				{
					obj2 = (BeamObject)iter2Line.next();
//					if (line1 != line2)
//					{
						/* Der Faktor 3.0 ist willkuerlich gewaehlt, da	*/
						/* nur Vorauswahl.				*/		
						writer.writeOut("		the factor dHardCoreDepth is randomly set to 3.0 --> ca. Line1384");
						dHardCoreDepth = 3.0 * this.calcHardCoreDistance(obj1, obj2);
						writer.writeOut("		calculated dHardCoreDepth= "+dHardCoreDepth);
						k = 1;
						//loop through all points of the current corresponding geometry
						while (k < obj2._arrCoords.size())
						{
							coordL1 = ((BeamPoint)obj2._arrCoords.get(k-1))._coordCur;
							coordL2 = ((BeamPoint)obj2._arrCoords.get(k))._coordCur;
							bInterior = true;				
							if (coordL1.x < coordL2.x)
							{
								if(coordL1.x-dHardCoreDepth > coordP.x || coordL2.x+dHardCoreDepth < coordP.x) 
									bInterior = false;
							}
							else
							{
								if(coordL2.x-dHardCoreDepth > coordP.x || coordL1.x+dHardCoreDepth < coordP.x) 
									bInterior = false; 
							}
							if (bInterior)
							{
								if (coordL1.y < coordL2.y)
								{
									if (coordL1.y-dHardCoreDepth > coordP.y || coordL2.y+dHardCoreDepth < coordP.y) 
										bInterior = false;
								}
								else
								{
									if (coordL2.y-dHardCoreDepth > coordP.y || coordL1.y+dHardCoreDepth < coordP.y) 
										bInterior = false;
								}
							}
		
							if (bInterior)
							{
								LineSegment seg = new LineSegment(coordL1, coordL2);
								dDistance = seg.distance(coordP);
							}
							if (bInterior && dDistance <= dHardCoreDepth)
							{
								if (bLine)
									((BeamPoint)obj1._arrCoords.get(i))._lstLineReferences.add(new BeamRef(obj2, k-1));
								else
									((BeamPoint)obj1._arrCoords.get(i))._lstAreaReferences.add(new BeamRef(obj2, k-1));
							}
							k++;
						}
//					}
				}
				i++;
			}
		}
	}

	/**
	 * Determine the minimal distance between two objects.
	 * @return double					The minimal distance.
	 * @param obj1						The first object.
	 * @param obj2						The second object.
	 */
	protected double calcHardCoreDistance(BeamObject obj1, BeamObject obj2)
	{
		writeOutput(_root, "calcHardCoreDistance");
		double dHc = 0.0;
		if (obj1._dSigWidth != null)
			dHc += obj1._dSigWidth.getWidth()/2.0;
		if (obj2._dSigWidth != null)
			dHc += obj2._dSigWidth.getWidth()/2.0;
		dHc += Math.max(obj1._dMinDist == null ? 0 : obj1._dMinDist.doubleValue(), obj2._dMinDist == null ? 0 : obj2._dMinDist.doubleValue());
		return dHc/1.0;
	}

	/**
	 * Determine the maximal space between two objects.
	 * @return double					The maximal space.
	 * @param obj1						The first object.
	 * @param obj2						The second object.
	 */
	protected double calcHardCoreSpace(BeamObject obj1, BeamObject obj2)
	{
		writeOutput(_root, "calcHardCoreSpace");
		double dHc = 0.0;
		dHc += Math.max(obj1._dMinDist == null ? 0 : obj1._dMinDist.doubleValue(), obj2._dMinDist == null ? 0 : obj2._dMinDist.doubleValue());
		return dHc/1.0;
	}

	/**
	 * Determine the maximal signature width of two two objects.
	 * @return double					The maximal signature width.
	 * @param obj1						The first object.
	 * @param obj2						The second object.
	 */
	protected double calcMaxSigWidth(BeamObject obj1, BeamObject obj2)
	{
		writeOutput(_root, "calcMaxSigWidth");
		double dWidth1 = 0.0;
		double dWidth2 = 0.0;
		if (obj1._dSigWidth != null)
			dWidth1 = obj1._dSigWidth.getWidth();
		if (obj2._dSigWidth != null)
			dWidth2 = obj2._dSigWidth.getWidth();
		return Math.max(dWidth1, dWidth2);
	}
	
	/**
	 * Multiply a matrix with a vector.
	 * @return double[]						The result vector.
	 * @param matA							The matrix.
	 * @param matB							The vector.
     * @exception IllegalArgumentException	Whenm matrix inner dimensions are not agree.
	 */
    protected double[] multiply(double[][] matA, double[] matB)
    {
    	writeOutput(_root, "multiply");
        int i;
        int k;
        if (matA[0].length != matB.length)
        {
    	   throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        double[] matResult = new double[matA.length];

        i = 0;
        while (i < matA.length)
        {
        	k = 0;
        	matResult[i] = 0.0;
        	while (k < matA[0].length)
        	{
        		matResult[i] += matA[i][k] * matB[k];
        		k++;
        	}
        	i++;
        }
        return matResult;
    }

    private class BeamPoint
    {
    	public Coordinate	_coordNull = new Coordinate();
    	public Coordinate	_coordCur = new Coordinate();
    	public Coordinate	_coordOld = new Coordinate();
    	public double		_dEExtern = 0.0;
    	public int			_nJunctionIndex = -1;
    	public LinkedList	_lstLineReferences = new LinkedList();
    	public LinkedList	_lstAreaReferences = new LinkedList();
    	
    	public BeamPoint(Coordinate coord, boolean bCopy)
    	{
    		if (bCopy)
    			_coordCur.setCoordinate(coord);
    		else
    			_coordCur = coord;
    		_coordNull.setCoordinate(coord);
    	}
    }

    private class BeamRef
    {
    	public BeamObject		_obj;
    	public int				_nSegmentIndex;
    	
    	public BeamRef(BeamObject obj, int nSegmentIndex)
    	{
    		_obj = obj;
    		_nSegmentIndex = nSegmentIndex;
    	}

    }
    

    public class BeamObject
    {
    	public ArrayList		_arrCoords;
    	public SignatureWidth	_dSigWidth;				// Signature width
    	public Double			_dMinDist;				// Minimal distance to other objects
    	public Boolean			_bDeformable;			// Object is deformable
    	public Integer			_nDisplacementPriority;	// The displacement priority, movability (between 0 and 9)
    	public Geometry			_geom;
    	
    	public BeamObject(LineString lineString, SignatureWidth dSigWidth, Double dMinDist, Boolean bDeformable, Integer nPriority, Boolean bCopyCoordinates)
    	{
    		if ((bCopyCoordinates != null) && bCopyCoordinates.booleanValue())
    		{
    			_geom = (LineString)lineString.clone();
    		}
    		else
    			_geom = lineString;
    		_arrCoords = new ArrayList(lineString.getNumPoints());
    		int i = 0;
    		while (i < lineString.getNumPoints())
    		{
    			_arrCoords.add(new BeamPoint(((LineString)_geom).getCoordinateN(i), false));
    			i++;
    		}
    		_dSigWidth = dSigWidth;
    		_dMinDist = dMinDist;
    		_bDeformable = bDeformable;
    		_nDisplacementPriority = nPriority;
    	}

    	public BeamObject(Polygon polygon, Double dSigWidth, Double dMinDist, Boolean bDeformable, Integer nPriority, Boolean bCopyCoordinates)
    	{
    		if ((bCopyCoordinates != null) && bCopyCoordinates.booleanValue())
    		{
    			_geom = (Polygon)polygon.clone();
    		}
    		else
    			_geom = polygon;
    		_arrCoords = new ArrayList(polygon.getExteriorRing().getNumPoints());
    		int i = 0;
    		while (i < polygon.getExteriorRing().getNumPoints())
    		{
    			_arrCoords.add(new BeamPoint(((Polygon)_geom).getExteriorRing().getCoordinateN(i), false));
    			i++;
    		}
    		if (dSigWidth != null)
    			_dSigWidth = new SignatureWidth(dSigWidth.doubleValue(), 0.0);
    		else
    			_dSigWidth = null;
    		_dMinDist = dMinDist;
    		_bDeformable = bDeformable;
    		_nDisplacementPriority = nPriority;
    	}
    }
    public class Output{
    	public Boolean outSet =false;
    	public String root;
    	public void writeOut(String output){
    		FileWriter writer;
    		File file;		
    		file = new File(root);
    		try {
    			if (outSet == false){
    				 writer = new FileWriter(file);
    				 outSet = true;
    			}
    			else{				
    				writer = new FileWriter(file ,true);
    			}			
    			writer.write(output);
    			writer.write(System.getProperty("line.separator"));
    			writer.flush();		       
    		    writer.close();
    		} catch (IOException e) {
    		      e.printStackTrace();
    		}
    	}
    	public void setRoot(String directory){
    		root = directory;
    	}
    	
    }
    
    
}


