package ch.unizh.geo.webgen.client.jump;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import ch.unizh.geo.webgen.model.ConstraintSpace;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.SelectionManager;

public class KnowledgeBaseVisualisationPlugIn extends AbstractPlugIn implements ThreadedPlugIn {

    private MultiInputDialog dialog;
    KnowledgeBaseVisualisationPanel vispanel;
    List fclist;
    PlugInContext context;
    Layer activelayer;

    public KnowledgeBaseVisualisationPlugIn() {
    }

    public void initialize(PlugInContext context) throws Exception {
    	context.getFeatureInstaller().addMainMenuItem(
                this, "WebGen06", "KnowledgeBase Visualisaion",
    			null, null);
    }

    public boolean execute(PlugInContext context) throws Exception {
    	try {
//    		initDialog(context);
//        	dialog.setVisible(true);

//        	if (!dialog.wasOKPressed()) {
//            	return false;
//        	}
        	return true;
    	}
    	catch (java.lang.IndexOutOfBoundsException e) {return false;}
    	//return true;
    }

    /*private void initDialog(PlugInContext context) {
        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "WebGen Algorithm", true);
        dialog.setSideBarDescription("Select Constrained Layer");
        Layer deflay = null;
        try {
        	deflay = context.getCandidateLayer(0);
        }
        catch (Exception e) {}
        dialog.addLayerComboBox("buildings", deflay, null, context.getLayerManager());
        GUIUtil.centreOnWindow(dialog);
    }*/

    public void run(TaskMonitor monitor, PlugInContext context) {
    	this.context = context;
    	JInternalFrame visframe = new JInternalFrame("Knowledge Base Visualisation", false, true, false, true);

    	ConstraintSpace trainedConstraintSpace = openConstraintSpace();    	
    	if(trainedConstraintSpace == null)
    		return;
    	
    	int nbrCategories = 10;    		
    	int nbrConstraints = trainedConstraintSpace.getNbrConstraints();
    	int nbrOperations = trainedConstraintSpace.getNbrOperations();
    	double constraintOperatorPriority[][][][] = new double[nbrOperations][nbrConstraints][nbrOperations][nbrCategories];    	
    	
    	// loop over knowledge base
    	for(int id=0; id<trainedConstraintSpace.getNbrFeatures(); id++) {
    		Double[] featureCost = trainedConstraintSpace.getFeatureCostById(id);
    		Double[] featureOperation = trainedConstraintSpace.getFeatureOperationById(id);
    		
    		// loop over operations
    		for(int operationId=0; operationId<nbrOperations; operationId++) {
        		for(int constraintId=0; constraintId<nbrConstraints; constraintId++) {
        			int v1 = operationId;
        			int v2 = constraintId;
        			int v3 = getCategoryFeatureCost(featureCost[constraintId]);
        			int v4 = (int)Math.round(featureOperation[operationId]);    				
        			constraintOperatorPriority[v1][v2][v3][v4] += 1;        			
        		}
   
    		}
    	}
    	
    	//make file menu
    	JMenuItem saveImageItem = new JMenuItem();
    	saveImageItem.setText("Save Image");
    	saveImageItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
				        return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
				    }
				    public String getDescription() {
				        return ".png image";
				    }
				});
				chooser.setSelectedFile(new File("eval.png"));
				int returnVal = chooser.showSaveDialog(vispanel);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try{
						File file = chooser.getSelectedFile();
	                	if(!file.getName().endsWith(".png")) file = new File(file.getCanonicalPath()+".png");
	                	if(file.exists()) {
	                    	int ret = JOptionPane.showConfirmDialog (chooser,
	                    		"The file "+ file.getName() + " already exists. \nWould you like to replace it?",
	                        	"Replace?",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	                    	if (ret!=JOptionPane.OK_OPTION) file = null;
	                	}
	                	if(file != null) {
	                		RenderedImage imageData = vispanel.getImage();
				        	ImageIO.write(imageData, "png", file);
	                	}
					}
					catch(IOException ex){}
	            }
			}
		});
    	JMenu filemenu = new JMenu();
    	filemenu.setText("File");
    	filemenu.add(saveImageItem);
    	JMenuBar menubar = new JMenuBar();
    	menubar.add(filemenu);
    	visframe.setJMenuBar(menubar);
    	
    	Container viscontainer = visframe.getContentPane();
    	vispanel = new KnowledgeBaseVisualisationPanel(constraintOperatorPriority);
    	vispanel.addMouseListener(new VisContainterMouseListener());
    	viscontainer.add(vispanel);
    	
        visframe.setSize(new Dimension(820,550));
        visframe.setTitle(dialog.getLayer("buildings").getName());
    	context.getWorkbenchFrame().addInternalFrame(visframe, true, true);
    	visframe.show();
    }
    
    public static ConstraintSpace openConstraintSpace() {
    	try {
    		FileInputStream fis = new FileInputStream("C:\\java\\constrspace.ser");
    		//FileInputStream fis = new FileInputStream("C:\\Dokumente und Einstellungen\\burg\\Eigene Dateien\\withSave\\Experimente\\Steuerung\\constrspace.ser");
        	ObjectInputStream ois = new ObjectInputStream(fis);
        	ConstraintSpace tcs = (ConstraintSpace)ois.readObject();
        	ois.close();
        	return tcs;
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    }    

    /**
     * derives 5 categories from featureCost
     * 
     * @param featureCost
     * @return
     */
    public int getCategoryFeatureCost(double featureCost) {
    	int categoryFeatureCost;
    	if(featureCost <= 0.2)
    		categoryFeatureCost = 0;
    	else
        	if(featureCost <= 0.4)
        		categoryFeatureCost = 1;
        	else
            	if(featureCost <= 0.6)
            		categoryFeatureCost = 2;
            	else
                	if(featureCost <= 0.8)
                		categoryFeatureCost = 3;
                	else
                    	if(featureCost <= 1.0)
                    		categoryFeatureCost = 4;
                    	else
                    		categoryFeatureCost = -1;

    	return categoryFeatureCost;
    }    
    
    class VisContainterMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            // Check if a rectangle contains the point of
            // mouse click
        	//System.out.println("MouseClick: "+e.getX() + ";" + e.getY());
        	ArrayList<Feature> selectedfeatures = new ArrayList<Feature>();
        	//double ptdist;
        	//vispanel.selectedpos = -1;
        	vispanel.selectedposs.clear();
        	for(int i=0; i < vispanel.lines.length; i++) {
        		for(int j=0; j < 7; j++) {
        			//ptdist = vispanel.lines[i][j].ptLineDist(e.getX(), e.getY());
        			//System.out.println("Distance  ("+e.getX()+","+e.getY()+") ["+i+","+j+"] "+ptdist);
        			//if(ptdist < 2.0) {
        			if(vispanel.lines[i][j].intersects(e.getX(),e.getY(),2,2)){
        				System.out.println("Feat: "+fclist.get(i).toString());
        				//vispanel.selectedpos = i;
        				vispanel.selectedposs.add(new Integer(i));
        				//((Feature)activelayer.getFeatureCollectionWrapper().getFeatures().get(0)).
        				//context.getLayerViewPanel().getSelectionManager().
        				//((SelectionManagerProxy) context.getActiveInternalFrame()).getSelectionManager().
        				selectedfeatures.add((Feature)fclist.get(i));
        			}
        		}
        	}
        	vispanel.repaint();
        	LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        	SelectionManager selectionManager = layerViewPanel.getSelectionManager();
        	selectionManager.clear();
        	selectionManager.getFeatureSelection().selectItems(
                activelayer, selectedfeatures);
            
        	
        	//history navigation
        	if((370 < e.getX()) && (e.getX() < 382) &&
        	   (470 < e.getY()) && (e.getY() < 482)) {
        		//System.out.println("-");
        		if(vispanel.actstate > 0) vispanel.actstate--;
        		//vispanel.repaint();
        	}
        	if((420 < e.getX()) && (e.getX() < 432) &&
               (470 < e.getY()) && (e.getY() < 482)) {
             	//System.out.println("+");
             	if(vispanel.actstate < vispanel.laststate) vispanel.actstate++;
             	//vispanel.repaint();
            }
        }
    }
	
}
