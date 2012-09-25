package ch.unizh.geo.webgen.client.jump;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.dom4j.Document;
import org.dom4j.Element;

import ch.unizh.geo.webgen.registry.AlgorithmRegistry;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class LocalListServicesPlugIn extends AbstractPlugIn implements ThreadedPlugIn {

	private MultiInputDialog dialog;
	public String columnNames[] = {"name", "version", "owner", "description"};
	public String rowData[][];
	private String SDurls[];
	private String SDnames[];
	private JTable table;
	public String categoryfilter = "general";
	
	String[] algorithms;
	AlgorithmRegistry registry;

	public LocalListServicesPlugIn() {
	}

	public void initialize(PlugInContext context) throws Exception {
		context.getFeatureInstaller().addMainMenuItem(this, "WebGen06",
				"Local ListServices", null, null);
	}

	public boolean execute(PlugInContext context) throws Exception {
		try {
			initDialog(context);
			dialog.setSize(600, 300);
			dialog.setVisible(true);

			if (!dialog.wasOKPressed()) {
				return false;
			}
			return true;
		} catch (java.lang.IndexOutOfBoundsException e) {
			return false;
		}
		
	}

	private void initDialog(PlugInContext context) {
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();

			URL servicesurl = classLoader.getResource("ch/unizh/geo/webgen/service/");
			File servicesdir = new File(servicesurl.getFile());
			algorithms = servicesdir.list();
			
			for(int i=0; i<algorithms.length; i++) {
				//algorithms[i] = algorithms[i].replaceFirst("/.class","");
				algorithms[i] = algorithms[i].substring(0, algorithms[i].length()-6);
			}
			
			/*InputStream propIs = classLoader.getResourceAsStream("webgen.properties");
			Properties webgenProperties = new Properties();
			webgenProperties.load(propIs);
			String algorithmsPropVal = webgenProperties.getProperty("algorithms");
			algorithms = algorithmsPropVal.split(",");*/
		}
		catch(Exception e) {
			algorithms = new String[0];
		}
		
		registry = new AlgorithmRegistry(algorithms, true);
		
		dialog = new MultiInputDialog(context.getWorkbenchFrame(), "WebGen", true);
		//JFrame frame = new JFrame();
		
		JPanel spane = new JPanel();
		spane.setLayout(new BoxLayout(spane, BoxLayout.LINE_AXIS));
		JButton sbut = new JButton("View");
		sbut.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadTableData();
				createTable();
				table.doLayout();
				dialog.repaint();
			}
		});
		//spane.add(stxt);
		//spane.add(sbut);
		
		JPanel fpane = new JPanel();
		fpane.setLayout(new BoxLayout(fpane, BoxLayout.LINE_AXIS));
		String[] categories = {"general", "processing", "operator", "support", "mrdb" };
		JComboBox fchoice = new JComboBox(categories);
		fchoice.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				categoryfilter = (String)cb.getSelectedItem();
				table.clearSelection();
				loadTableData();
				table.doLayout();
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				dialog.repaint();
			}
		});
		JLabel flabel = new JLabel("category filter: ");
		fpane.add(flabel);
		fpane.add(fchoice);
		
		loadTableData();
		if(this.rowData != null) {
			createTable();
			table.doLayout();
		}
		else {
			table = new JTable();
			table.setBackground(Color.white);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		JScrollPane tscroll = new JScrollPane(table);
		
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		pane.add(spane);
		pane.add(fpane);
		pane.add(tscroll);
		//dialog.getContentPane().add(tscroll);
		dialog.getContentPane().add(pane);
		dialog.setSize(600, 300);
		GUIUtil.centreOnWindow(dialog);
	}

	public void run(TaskMonitor monitor, PlugInContext context)
			throws Exception {
		int mpos = table.getSelectedRow();
		System.out.println("selected method position: "+mpos);
		if(mpos<0) return;
		System.out.println("selected method name: "+SDnames[mpos]);
		
		Blackboard bb = (Blackboard) context.getWorkbenchContext().getBlackboard().getProperties().get("com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn - BLACKBOARD");
		bb.put("LAST WEBGEN SERVICE LOCAL", SDnames[mpos]);

		LocalExecuteServicePlugIn wgcall = new LocalExecuteServicePlugIn();
		//wgcall.initFactories();
		if(wgcall.execute(context) == true) {
			wgcall.run(monitor, context);
		}
	}
	
	private Element getXMLRoot() throws Exception {
		Document document = registry.getDocument();
		Element root = document.getRootElement();

		// check root element
		if (!root.getNamespacePrefix().equals("webgen"))
			throw new Exception("Only webgen namespace is supported!");
		if (!root.getName().equals("Registry"))
			throw new Exception("Only WebGenRequest elements are supported!");

		return root;
	}
	
	// ++++++++++++++++++++++++++++++++
	private void loadTableData() {
		try {
			Element root = getXMLRoot();
			
	        //Vector webgenlist = new Vector();
	        
	        //{"name", "institute", "autor-email", "description", "version", "date"}
	        int scount = root.elements("Service").size();
	        rowData = new String[scount][6];
	        SDurls = new String[scount];
	        SDnames = new String[scount];

	        int ic = 0;
	        for (Iterator i = root.elementIterator("Service"); i.hasNext();) {
	            Element tmpel = (Element) i.next();
	            String ctext = tmpel.elementText("name");
	            String ccat = tmpel.elementText("category");
	            if(categoryfilter.equals("general") || categoryfilter.equals(ccat)) {
	            	rowData[ic][0] = ctext;
		            rowData[ic][1] = tmpel.elementText("version");
		            rowData[ic][2] = tmpel.elementText("owner");
		            rowData[ic][3] = tmpel.elementText("description");
		            SDurls[ic] = tmpel.elementText("url");
		            SDnames[ic] = ctext;
		            ic++;
		            System.out.println("algo: "+ctext);
	            }
	        }
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			e.printStackTrace();
		}
	}
	
//	++++++++++++++++++++++++++++++++
	private void createTable() {
		//Create TableColumnModel
		DefaultTableColumnModel cm = new DefaultTableColumnModel();
		for (int i = 0; i < columnNames.length; ++i) {
			TableColumn col = new TableColumn(i);
			col.setHeaderValue(columnNames[i]);
			cm.addColumn(col);
		}
		
		//create Tablemodel
		TableModel tm = new AbstractTableModel() {
			static final long serialVersionUID = 12345;
			public int getRowCount() {
				return rowData.length;
			}

			public int getColumnCount() {
				return columnNames.length;
			}
			public Object getValueAt(int row, int column) {
				try {
					return rowData[row][column];
				} catch (ArrayIndexOutOfBoundsException e) {
					return null;
				}
			}
			public boolean isCellEditable(int row, int col) {
				return false;
			    /*switch (col) {
			     case 7: //weight
			      return true;
			     default:
			      return false;
			    }*/
			   }
		};
		
		//create table of selected files for SouthPanel
		table = new JTable(tm, cm);
		table.setBackground(Color.white);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

}
