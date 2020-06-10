/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

// Maksym Pyatnytskyy (PMAK (AAVSO)) mpyat2@gmail.com
 
package org.aavso.tools.vstar.external.plugin;

import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//import java.awt.Cursor;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.SwingWorker;

import java.util.concurrent.ExecutionException;

import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
//import org.aavso.tools.vstar.ui.dialog.MessageBox;

import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.PhaseParameterDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;

/**
 * VSX query by name
 */
public class VSXquery extends GeneralToolPluginBase
{

	private static String sVSXname = "";

	protected static final String sVSX_URL = "https://www.aavso.org/vsx/index.php?view=api.object&ident=";

	@Override
	public void invoke()
	{
		new QueryVSXdialog();
	}

	@Override
	public String getDescription()
	{
		return "VSX Query";
	}

	@Override
	public String getDisplayName()
	{
		return "VSX Query";
	}
	
	@SuppressWarnings("serial")
	class QueryVSXdialog extends JDialog
	{
		protected static final String sTITLE = "Query VSX";
		
		protected TextField fieldVSXname;
		protected DoubleField fieldVSXperiod;
		protected DoubleField fieldVSXepoch;
		protected TextField fieldVSXvarType;
		protected TextField fieldVSXspectralType;
		protected JTextArea textArea;
		
		protected boolean closed = false;
		
		protected JButton queryButton;
		protected JButton phaseDialogButton;

		/**
		 * Constructor
		 */
		public QueryVSXdialog()
		{
			super(DocumentManager.findActiveWindow());
			setTitle(sTITLE);
			setModal(false);
			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			topPane.add(createInfoPane());
			topPane.add(createButtonPane());
			contentPane.add(topPane);

			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					closed = true;
				}
			});
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}
		
		private JPanel createInfoPane()
		{
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			fieldVSXname = new TextField("VSX Name", sVSXname);
			panel.add(fieldVSXname.getUIComponent());
			
			fieldVSXperiod = new DoubleField("VSX Period", null, null, null);
			fieldVSXperiod.setValue(0.0);
			((JTextComponent) (fieldVSXperiod.getUIComponent())).setEditable(false);
			panel.add(fieldVSXperiod.getUIComponent());
			
			fieldVSXepoch = new DoubleField("VSX Epoch", null, null, null);
			fieldVSXepoch.setValue(0.0);
			((JTextComponent) (fieldVSXepoch.getUIComponent())).setEditable(false);
			panel.add(fieldVSXepoch.getUIComponent());

			fieldVSXvarType = new TextField("VSX Variability Type", "");
			fieldVSXvarType.setEditable(false);			
			panel.add(fieldVSXvarType.getUIComponent());

			fieldVSXspectralType = new TextField("VSX Spectral Type", "");
			fieldVSXspectralType.setEditable(false);
			panel.add(fieldVSXspectralType.getUIComponent());
			
			textArea = new JTextArea(16, 0);
			textArea.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(textArea);
			panel.add(scrollPane);
 
			return panel;
		}

		private JPanel createButtonPane() {
			JPanel panel = new JPanel(new BorderLayout());

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(createCancelButtonListener());
			panel.add(cancelButton, BorderLayout.LINE_START);

			queryButton = new JButton("Query");
			queryButton.addActionListener(createQueryButtonListener());
			panel.add(queryButton, BorderLayout.CENTER);

			phaseDialogButton = new JButton("Populate Phase Dialog");
			phaseDialogButton.addActionListener(createPhaseDialogButtonListener());
			panel.add(phaseDialogButton, BorderLayout.LINE_END);
			phaseDialogButton.setEnabled(false);

			this.getRootPane().setDefaultButton(queryButton);

			return panel;
		}
		
		// Return a listener for the "Query" button.
		private ActionListener createQueryButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					queryVSX();
				}
			};
		}

		// Return a listener for the "Populate Phase Dialog" button.
		private ActionListener createPhaseDialogButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Mediator mediator = Mediator.getInstance();
					PhaseParameterDialog phaseParam = mediator.getPhaseParameterDialog();
					phaseParam.setEpochField(fieldVSXepoch.getValue());
					phaseParam.setPeriodField(fieldVSXperiod.getValue());
					MessageBox.showMessageDialog(QueryVSXdialog.this, sTITLE, "Phase dialog parameters have been set");
				}
			};
		}
		
		// Return a listener for the "Cancel" button.
		private ActionListener createCancelButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closed = true;
					setVisible(false);
					dispose();
				}
			};
		}
		
		private void queryVSX() {
			sVSXname = fieldVSXname.getValue().trim();
			if ("".equals(sVSXname))
				return;
			textArea.setText("Please wait...");
			fieldVSXperiod.setValue(0.0);
			fieldVSXepoch.setValue(0.0);
			fieldVSXvarType.setValue("");
			fieldVSXspectralType.setValue("");
			setTitle("Wait...");
			//pack();
			SwingWorker<VSQqueryResult, VSQqueryResult> worker = new VSXquerySwingWorker(this, sVSXname);
			queryButton.setEnabled(false);
			phaseDialogButton.setEnabled(false);
			worker.execute();
		}
		

	}
	
	class VSQqueryResult {
		protected String name;
		protected Double period;
		protected Double epoch;
		protected String varType;
		protected String spType;		
		protected String stringResult;
	}

	class VSXquerySwingWorker extends SwingWorker<VSQqueryResult, VSQqueryResult>	{

		private VSXquery.QueryVSXdialog dialog;
		private String vsxName; 

		public VSXquerySwingWorker(VSXquery.QueryVSXdialog dialog, String name)
		{
			this.dialog = dialog;
			this.vsxName = name;
		}

		@Override
		protected VSQqueryResult doInBackground() throws Exception {
			return queryVSX(vsxName, 1);
		}

		@Override
		protected void done()  
		{ 
			VSQqueryResult vsxResult = null;
			String error = "Unknown error";
			try 
			{ 
				vsxResult = get(); 
			}  
			catch (InterruptedException ex)  
			{ 
				error = "Interrupted";
			}  
			catch (ExecutionException ex)  
			{ 
				Throwable cause = ex.getCause();
				error = cause.getLocalizedMessage();
				if (error == null || "".equals(error))
					error = cause.toString();
				error = "Execution Exception: \n" + error;
			} finally {
				if (!dialog.closed)
				{
					dialog.setTitle(VSXquery.QueryVSXdialog.sTITLE);
					dialog.queryButton.setEnabled(true);
					dialog.fieldVSXname.setValue(vsxName);
					if (vsxResult != null) {
						dialog.textArea.setText(vsxResult.stringResult);
						dialog.fieldVSXperiod.setValue(vsxResult.period != null ? vsxResult.period : 0.0);
						dialog.fieldVSXepoch.setValue(vsxResult.epoch != null ? vsxResult.epoch : 0.0);
						dialog.fieldVSXvarType.setValue(vsxResult.varType);
						dialog.fieldVSXspectralType.setValue(vsxResult.spType);
						dialog.phaseDialogButton.setEnabled(true);
					} else {
						dialog.textArea.setText(error);
						dialog.fieldVSXperiod.setValue(0.0);
						dialog.fieldVSXepoch.setValue(0.0);
						dialog.fieldVSXvarType.setValue("");
						dialog.fieldVSXspectralType.setValue("");
					}
					dialog.pack();
				}
			}
		}
		
		private VSQqueryResult queryVSX(String sVSXname, int maxStars)
				throws UnsupportedEncodingException, ParserConfigurationException, IOException, SAXException {
			VSQqueryResult vsxResult = new VSQqueryResult();
			String result = "";

			// Example: https://www.aavso.org/vsx/index.php?view=api.object&ident=pmak+v41
			String sURL = VSXquery.sVSX_URL + URLEncoder.encode(sVSXname, StandardCharsets.UTF_8.name());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			URL url = new URL(sURL);
			InputStream stream = url.openStream();
			Document doc = db.parse(stream);
			doc.getDocumentElement().normalize();

			// Parse <VSXObject>
			Element root = doc.getDocumentElement();
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Element row = (Element) nodes.item(i);
				String tag = row.getTagName();
				String content = row.getTextContent();
				if ("Period".equalsIgnoreCase(tag))
					try {
						vsxResult.period = Double.parseDouble(content);
						if (vsxResult.period == 0)
							vsxResult.period = null;
					} catch (NumberFormatException ex) {
						vsxResult.period = null;
					}
				else if ("Epoch".equalsIgnoreCase(tag))
					try {
						vsxResult.epoch = Double.parseDouble(content);
						if (vsxResult.epoch == 0)  
							vsxResult.epoch = null;
					} catch (NumberFormatException ex) {
						vsxResult.epoch = null;
					}
				else if ("VariabilityType".equalsIgnoreCase(tag))
					vsxResult.varType = content;
				else if ("SpectralType".equalsIgnoreCase(tag))
					vsxResult.spType = content;
				else
					result += tag + ": " + content + "\n";
			}
			if ("".equals(result))
				result = "No valid data returned by the query.";
			vsxResult.stringResult = result;
			return vsxResult;
		}
	}

}
