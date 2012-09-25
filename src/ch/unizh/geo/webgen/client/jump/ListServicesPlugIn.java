package ch.unizh.geo.webgen.client.jump;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import ch.unizh.geo.webgen.registry.AlgorithmRegistry;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class ListServicesPlugIn extends AbstractPlugIn implements ThreadedPlugIn {

	private PlugInContext context;
	private MultiInputDialog dialog;
	public String columnNames[] = {"name", "version", "owner", "description"};
	public String rowData[][];
	private AbstractTableModel tm;
	private String SDurls[];
	private JTable table;
	JTextField stxt = new JTextField("http://www.geo.unizh.ch:8080/neun/servlet/webgen.forward.Registry");
	//JTextField stxt = new JTextField("http://localhost:8080/webgen/registry");
	public String categoryfilter = "general";

	public ListServicesPlugIn() {
	}

	public void initialize(PlugInContext context) throws Exception {
		context.getFeatureInstaller().addMainMenuItem(this, "WebGen06",
				"WebGen ListServices", null, null);
	}

	public boolean execute(PlugInContext context) throws Exception {
		try {
			this.context = context;
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
		dialog = new MultiInputDialog(context.getWorkbenchFrame(), "WebGen", true);
		//JFrame frame = new JFrame();
		
		JPanel spane = new JPanel();
		spane.setLayout(new BoxLayout(spane, BoxLayout.LINE_AXIS));
		JButton sbut = new JButton("View");
		sbut.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadTableData();
				//createTable();
				tm.fireTableDataChanged();
				table.doLayout();
				dialog.repaint();
			}
		});
		spane.add(stxt);
		spane.add(sbut);
		
		try {
			Blackboard bb = (Blackboard) context.getWorkbenchContext().getBlackboard().getProperties().get("com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn - BLACKBOARD");
			String registryurl = bb.get("LAST WEBGEN REGISTRY").toString();
			if(registryurl != null) stxt.setText(registryurl);
		}
		catch(Exception e) {}
		
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
				tm.fireTableDataChanged();
				table.doLayout();
				//table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				dialog.repaint();
			}
		});
		categoryfilter = (String)fchoice.getSelectedItem();
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
		System.out.println("selected method url: "+SDurls[mpos]);
		
		Blackboard bb = (Blackboard) context.getWorkbenchContext().getBlackboard().getProperties().get("com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn - BLACKBOARD");
		bb.put("LAST WEBGEN SERVICE", SDurls[mpos]);

		ExecuteServicePlugIn wgcall = new ExecuteServicePlugIn();
		wgcall.initFactories();
		if(wgcall.execute(context) == true) {
			wgcall.run(monitor, context);
		}
	}
	
	private Element getXMLRoot() throws Exception {
		String registryurl = stxt.getText();
		URL url = new URL(registryurl);
		Reader is = new InputStreamReader(url.openStream());
		SAXReader reader = new SAXReader();
		Document document = reader.read(is);
		Element root = document.getRootElement();

		// check root element
		if (!root.getNamespacePrefix().equals("webgen"))
			throw new Exception("Only webgen namespace is supported!");
		if (!root.getName().equals("Registry"))
			throw new Exception("Only WebGenRequest elements are supported!");
		if(!root.attribute("protocolVersion").getValue().equals(AlgorithmRegistry.protocolVersion))
        	throw new Exception("Only protocolVersion "+ AlgorithmRegistry.protocolVersion + " supported!");
		
		Blackboard bb = (Blackboard) context.getWorkbenchContext().getBlackboard().getProperties().get("com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn - BLACKBOARD");
		bb.put("LAST WEBGEN REGISTRY", registryurl);
		
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
		            ic++;
		            System.out.println("algo: "+ctext);
	            }
	        }
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			rowData = new String[0][6];
	        SDurls = new String[0];
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
		tm = new AbstractTableModel() {
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
