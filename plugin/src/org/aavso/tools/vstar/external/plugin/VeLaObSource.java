/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed inputStream the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

// PMAK 2021-12-28

package org.aavso.tools.vstar.external.plugin;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.IntegerField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.NumberFieldBase;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.Type;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * The plugin generates series from VeLa expression  
 */
public class VeLaObSource extends ObservationSourcePluginBase {
	
	private static final String FUNC_NAME = "F";
	private static final String OBJ_NAME = "VeLa model";
	
	private final NameAndDescription DEFAULT_SERIES_NAME_AND_DESCRIPTION = new NameAndDescription(OBJ_NAME, OBJ_NAME); 
	
	private NameAndDescription seriesNameAndDescription = DEFAULT_SERIES_NAME_AND_DESCRIPTION;
	
	private double minJD = 0.0;
	private double maxJD = 0.0;
	private int points = 0;
	private JDflavour jDflavour = JDflavour.UNKNOWN;	
	private String veLaCode = null;
	
	private static ParameterDialog paramDialog;
	
	public VeLaObSource() {
		super();
	}
	
	@Override
	public String getCurrentStarName() {
		return getInputName();
	}

	@Override
	public InputType getInputType() {
		return InputType.NONE;
	}
	
	@Override
	public String getDescription() {
		return OBJ_NAME + " as an observation source";
	}

	@Override
	public String getDisplayName() {
		return "New Star from " + OBJ_NAME + "...";
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		if (paramDialog == null)
			paramDialog = new ParameterDialog();
		paramDialog.showDialog();
		if (paramDialog.isCancelled()) {
			return null;
		}
		
		minJD = paramDialog.getMinJD();
		maxJD = paramDialog.getMaxJD();;
		points = paramDialog.getPoints();
		jDflavour = paramDialog.getJDflavour();		
		veLaCode = paramDialog.getCode();
		
		isAdditive = paramDialog.isAdditive();
				
		inputName = OBJ_NAME;
		
		seriesNameAndDescription = getSeriesNameAndDescription();
		
		return new VeLaModelObsRetriever();
	}
	
	// Create unique series name and description
	private NameAndDescription getSeriesNameAndDescription() {
		
		if (!isAdditive)
			return DEFAULT_SERIES_NAME_AND_DESCRIPTION;
		
		Map<SeriesType, List<ValidObservation>> validObservationCategoryMap = Mediator.getInstance().getValidObservationCategoryMap();
		if (validObservationCategoryMap == null)
			return DEFAULT_SERIES_NAME_AND_DESCRIPTION;

		int n = 0;
		String tempDesc = getStringWithNumber(DEFAULT_SERIES_NAME_AND_DESCRIPTION.desc , n);
		while (true) {
			boolean found = false;
			for (Map.Entry<SeriesType, List<ValidObservation>> series : validObservationCategoryMap.entrySet()) {
				String desc = series.getKey().getDescription();
				if (desc != null) desc = desc.trim();
				if (tempDesc.equals(desc)) {
					found = true;
					n++;
					tempDesc = getStringWithNumber(DEFAULT_SERIES_NAME_AND_DESCRIPTION.desc, n);
					break;
				}
			}
			if (!found)
				break;
		}
		
		n = 0;
		String tempName = getStringWithNumber(tempDesc, n);
		while (true) {
			boolean found = false;
			for (Map.Entry<SeriesType, List<ValidObservation>> series : validObservationCategoryMap.entrySet()) {
				String name = series.getKey().getShortName();
				if (name != null) name = name.trim();
				if (tempName.equals(name)) {
					found = true;
					n++;
					tempName = getStringWithNumber(tempDesc, n);
					break;
				}
			}
			if (!found)
				break;
		}
		
		return new NameAndDescription(tempName, tempDesc);
	}
	
	private String getStringWithNumber(String s, int i) {
		if (i > 1)
			return s + " (" + i + ")";
		else
			return s;
	}
	
	class VeLaModelObsRetriever extends AbstractObservationRetriever {
		
		public VeLaModelObsRetriever() {
			//
		}
		
		@Override
		public void retrieveObservations() throws ObservationReadError {
			try {
				retrieveVeLaModelObservations();
			} catch (Exception e) {
				throw new ObservationReadError(e.getLocalizedMessage());
			}
		}			

		private void retrieveVeLaModelObservations() throws ObservationReadError {
			
			if (points < 2) {
				throw new ObservationReadError("Number of points must be >1");
			}
			
			double step = (maxJD - minJD) / (points - 1);
			
			if (step <= 0) {
				throw new ObservationReadError("Maximum JD must be greater than Minimum JD");
			}
			
			if (jDflavour == JDflavour.UNKNOWN) {
				throw new ObservationReadError("Undefined datetype");
			}
			
			if (veLaCode == null || "".equals(veLaCode.trim())) {
				throw new ObservationReadError("VeLa model is not defined");
			}
			
			setJDflavour(jDflavour);
			
			// Create a VeLa interpreter instance.
			VeLaInterpreter vela = new VeLaInterpreter();
			
			// Evaluate the VeLa model code.
			// A univariate function f(t:real):real is
			// assumed to exist after this completes.
			vela.program(veLaCode);
			
			// Has a model function been defined?
			if (!vela.lookupFunctions(FUNC_NAME).isPresent()) {
				throw new ObservationReadError("A function " + FUNC_NAME + "(T:REAL):REAL is not defined in the model");
			}

			Double magVal = null;
			
			for (int i = 0; i < points && !wasInterrupted(); i++) {
				
				double time = minJD + i * step;
				
				//vela.pushEnvironment(new VeLaFuncEnvironment(time));
				//try {
					String funCall = FUNC_NAME + "(" + NumericPrecisionPrefs.formatTime(time) + ")";
					Optional<Operand> result = vela.program(funCall);
					if (result.isPresent() && result.get().getType() == Type.REAL) {
						magVal = result.get().doubleVal();
					} else {
						throw new ObservationReadError("VeLa expression: Expected a Real value");
					}
				//} finally {
				//	vela.popEnvironment();
				//}

				if (magVal != null) {
					double uncertainty = 0;

					ValidObservation ob = new ValidObservation();
					
					String name = getStarInfo().getDesignation();
					ob.setName(name);
					
					ob.setDateInfo(new DateInfo(time));
					ob.setMagnitude(new Magnitude(magVal, uncertainty));
					ob.setBand(SeriesType.create(seriesNameAndDescription.desc, seriesNameAndDescription.name, Color.RED, false, false));
					ob.setRecordNumber(i);
					collectObservation(ob);
				}
				
				incrementProgress();
			}
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return OBJ_NAME + " observation source";
		}
		
		@Override
		public StarInfo getStarInfo() {
			String name = OBJ_NAME;
			if (name == null || "".equals(name)) {
				name = getSourceName();
			}
			return new StarInfo(this, name);
		}

		@Override
		public Integer getNumberOfRecords() throws ObservationReadError {
			return points;
		}

	}
	
/*
	class VeLaFuncEnvironment extends VeLaEnvironment<Operand> {

		private Map<String, String> symbol2CanonicalSymbol = new TreeMap<String, String>();

		private double time;

		public VeLaFuncEnvironment(double time) {
			super();
			this.time = time;
			reset();
		}

		
		@Override
		public Optional<Operand> lookup(String name) {
			boolean contained = false;
			Operand operand = null;

			name = name.toUpperCase();

			contained = symbol2CanonicalSymbol.containsKey(name);

			if (contained) {
				name = symbol2CanonicalSymbol.get(name);
			}

			if ("TIME".equals(name)) {
				operand = operand(name, time);
			}

			return Optional.ofNullable(operand);
		}
		
		// Cached operand creation methods.

		protected Operand operand(String name, Integer value) {
			return operand(Type.INTEGER, name, value);
		}

		protected Operand operand(String name, Double value) {
			return operand(Type.REAL, name, value);
		}

		protected Operand operand(String name, String value) {
			return operand(Type.STRING, name, value);
		}

		protected Operand operand(String name, Boolean value) {
			return operand(Type.BOOLEAN, name, value);
		}

		// Common operand factory method

		protected Operand operand(Type type, String name, Object value) {
			Operand operand = null;

			name = name.toUpperCase();
			
			if (cache.containsKey(name)) {
				operand = cache.get(name);
			} else {
				switch (type) {
				case INTEGER:
					operand = new Operand(type, (int) value);
					break;
				case REAL:
					operand = new Operand(type, (double) value);
					break;
				case STRING:
					operand = new Operand(type, (String) value);
					break;
				case BOOLEAN:
					operand = new Operand(type, (boolean) value);
					break;
				case LIST:
					operand = new Operand(type, (List<Operand>) value);
					break;
				default:
					operand = null;
				}

				bind(name, operand, true);
			}

			return operand;
		}

		public String[] symbols() {
			reset();
			
			String[] symbols = new String[symbol2CanonicalSymbol.size()];
			int i = 0;
			for (String symbol : symbol2CanonicalSymbol.keySet()) {
				symbols[i++] = symbol.toLowerCase();
			}

			return symbols;
		}

		public void reset() {
			populateMap();
		}

		// Helpers

		private void populateMap() {
			symbol2CanonicalSymbol.clear();

			symbol2CanonicalSymbol.put("TIME", "TIME");
			symbol2CanonicalSymbol.put("T", "TIME");
			symbol2CanonicalSymbol.put("JD", "TIME");
		}
		
	}
*/
	
	@SuppressWarnings("serial")
	private class ParameterDialog extends AbstractOkCancelDialog {

		private DoubleField minJD;
		private DoubleField maxJD;
		private IntegerField points;
		private JComboBox<String> jDflavour;
		private JTextArea codeArea;
		private JCheckBox addToCurrent;
		private JButton clearButton;
		private JButton loadButton;
		private JButton saveButton;
		
		public boolean isAdditive() {
			return addToCurrent.isSelected();
		}
		
		public Double getMinJD() {
			return minJD.getValue();
		}

		public Double getMaxJD() {
			return maxJD.getValue();
		}

		public Integer getPoints() {
			return points.getValue();
		}
		
		public String getCode() {
			return codeArea.getText();
		}
		
		public JDflavour getJDflavour() {
			switch (jDflavour.getSelectedIndex()) {
				case 0: return JDflavour.JD;
				case 1: return JDflavour.HJD;
				case 2: return JDflavour.BJD;
				default: return JDflavour.UNKNOWN;
			}
		}
		
		private int dateTypeToSelectedIndex(String dateType) {
			switch (dateType) {
				case "JD": 
					return 0;
				case "HJD":
					return 1;
				case "BJD":
					return 2;
				default:
					return 1; // HJD
			}
		}
		
		private String jDflavourToString(JDflavour jDflavour) {
			switch (jDflavour) {
				case JD: 
					return "JD";
				case HJD:
					return "HJD";
				case BJD:
					return "BJD";
				default:
					return "UNKNOWN";
			}
		}
		
		/**
		 * Constructor
		 */
		public ParameterDialog() {
			super("Function Code [model: f(t)]");
			
			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout((LayoutManager) new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			topPane.add(createControlPane());
			topPane.add(createCodePane());
			topPane.add(createFileControlPane());
			
			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);
			
			clearInput();
			
			this.pack();
			// Singleton mode! Use showDialog()!
			//setLocationRelativeTo(Mediator.getUI().getContentPane());
			//this.setVisible(true);
		}

		private JPanel createControlPane() {
			JPanel panel = new JPanel(new FlowLayout());
			
			minJD = new DoubleField("Minimum JD", null, null, null);
			((JTextField)(minJD.getUIComponent())).setColumns(12);
			panel.add(minJD.getUIComponent());			

			maxJD = new DoubleField("Maximum JD", null, null, null);
			((JTextField)(maxJD.getUIComponent())).setColumns(12);			
			panel.add(maxJD.getUIComponent());

			points = new IntegerField("Points", 2, null, null);
			((JTextField)(points.getUIComponent())).setColumns(12);			
			panel.add(points.getUIComponent());
			
			jDflavour = new JComboBox<String>();
			jDflavour.addItem(JDflavour.JD.toString());
			jDflavour.addItem(JDflavour.HJD.toString());
			jDflavour.addItem(JDflavour.BJD.toString());
			jDflavour.setSelectedIndex(dateTypeToSelectedIndex("HJD")); // HJD
			panel.add(jDflavour);
			
			addToCurrent = new JCheckBox("Add to current?");
			//addToCurrent.setSelected(true);
			panel.add(addToCurrent);
			
			return panel;
		}
		
		private JPanel createCodePane() {
			JPanel panel = new JPanel();
			panel.setLayout((LayoutManager) new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			codeArea = new JTextArea(25, 80);
			JScrollPane scrollPane = new JScrollPane(codeArea);
			panel.add(scrollPane);
			return panel;
		}
		
		private JPanel createFileControlPane() {
			JPanel panel = new JPanel(new FlowLayout());
			
			clearButton = new JButton(LocaleProps.get("CLEAR_BUTTON"));
			panel.add(clearButton);
			clearButton.addActionListener(createClearButtonActionListener());

			loadButton = new JButton(LocaleProps.get("LOAD_BUTTON"));
			panel.add(loadButton);
			loadButton.addActionListener(createLoadButtonActionListener());

			saveButton = new JButton(LocaleProps.get("SAVE_BUTTON"));
			panel.add(saveButton);
			saveButton.addActionListener(createSaveButtonActionListener());
			
			return panel;
		}
		
		ActionListener createClearButtonActionListener() {
			return new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					clearInput();
				}
			};
		}
		
		private ActionListener createLoadButtonActionListener() {
			return new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					readVelaXML();
				}
			};
		}
		
		private ActionListener createSaveButtonActionListener() {
			return new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					writeVelaXML();
				}
			};
		}
		
		private void clearInput() {
			minJD.setValue(null);
			maxJD.setValue(null);
			points.setValue(null);
			jDflavour.setSelectedIndex(dateTypeToSelectedIndex("HJD"));
			codeArea.setText("");
		}
		
		private void readVelaXML() {
			try {
				String content = Mediator.getInstance().getVelaXMLchoosers().readFileAsString(ParameterDialog.this);
				if (content != null) {
					clearInput();
					processXMLstring(content);
				}
			} catch (Exception ex) {
				MessageBox.showErrorDialog(ParameterDialog.this, getTitle(), ex);
			}
		}

		private void processXMLstring(String xmlString) throws Exception {
			// https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
			
			// Instantiate the Factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();			
			// process XML securely, avoid attacks like XML External Entities (XXE)
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			// parse XML file
	        DocumentBuilder db = dbf.newDocumentBuilder(); 
	        Document doc = db.parse(new ByteArrayInputStream(xmlString.getBytes()));

	        // optional, but recommended
	        // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	        doc.getDocumentElement().normalize();

	        Node root = doc.getDocumentElement();

	        if ("VELA_MODEL".equals(root.getNodeName())) {
		        Element element = (Element)root;
	        	codeArea.setText(getNodeTextContent(element, "code"));
	        	((JTextField)(minJD.getUIComponent())).setText(getNodeTextContent(element, "minJD"));
	        	((JTextField)(maxJD.getUIComponent())).setText(getNodeTextContent(element, "maxJD"));
	        	((JTextField)(points.getUIComponent())).setText(getNodeTextContent(element, "points"));
       			jDflavour.setSelectedIndex(dateTypeToSelectedIndex(getNodeTextContent(element, "datetype")));
	        } else {
	        	throw new Exception("The document root is not VELA_MODEL");
	        }
		}
		
		private String getNodeTextContent(Element element, String name) {
			String s = null;
			NodeList list = element.getElementsByTagName(name);
			if (list.getLength() == 1) {
				Node node = list.item(0);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					s = node.getTextContent();
				}
			}
			return s;			
        }
		
		private void writeVelaXML() {
			try	{
				String content = getVelaXMLstring();
				Mediator.getInstance().getVelaXMLchoosers().writeStringToFile(ParameterDialog.this, content);
			} catch (Exception ex) {
				MessageBox.showErrorDialog(ParameterDialog.this, getTitle(), ex);
			}
		}
		
		private String getVelaXMLstring() throws ParserConfigurationException, TransformerException {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(getVelaXML()), new StreamResult(writer));
			return writer.getBuffer().toString();
		}
		
		private Document getVelaXML() throws ParserConfigurationException {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();			
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			Document doc = documentBuilder.newDocument();
			
			// root element
            Element root = doc.createElement("VELA_MODEL");
            doc.appendChild(root);			
            
            Element element;
            
            element = doc.createElement("minJD");
            element.appendChild(doc.createTextNode(minJD.getStringValue()));
            root.appendChild(element);
            
            element = doc.createElement("maxJD");
            element.appendChild(doc.createTextNode(maxJD.getStringValue()));
            root.appendChild(element);

            element = doc.createElement("points");
            element.appendChild(doc.createTextNode(points.getStringValue()));
            root.appendChild(element);
          
            element = doc.createElement("datetype");
            element.appendChild(doc.createTextNode(jDflavourToString(getJDflavour())));
            root.appendChild(element);
            
            element = doc.createElement("code");
            element.appendChild(doc.createTextNode(codeArea.getText()));
            root.appendChild(element);
            
            return doc;
		}
		
		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
		 */
		@Override
		protected void cancelAction() {
			// Nothing to do.
		}

		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
		 */
		@Override
		protected void okAction() {
			boolean ok = true;
			
			Double minJDval = getMinJD();
			Double maxJDval = getMaxJD();
			Integer pointsval = getPoints();
			
			if (minJDval == null) {
				valueError(minJD);
				ok = false;
			}
			
			if (ok) {
				if (maxJDval == null) {
					valueError(maxJD);
					ok = false;
				}
			}
			
			if (ok) {
				if (pointsval == null) {
					valueError(points);
					ok = false;
				}
			}
			
			if (ok) {
				double step = (maxJDval - minJDval) / pointsval;
				if (step <= 0) {
					MessageBox.showErrorDialog(this, "Error", "Maximum JD must be greater than Minimum JD");
					ok = false;
				}
			}
			
			if (ok) {
				if ("".equals(getCode().trim())) {
					MessageBox.showErrorDialog(this, getTitle(), "VeLa model must be defined.");
					ok = false;
				}
			}

			if (ok) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}
		
		
		private void valueError(NumberFieldBase<?> field) {
			MessageBox.showErrorDialog(this, "Error", field.getName() + ": Invalid value!\n" +
					numberFieldInfo(field));				
			(field.getUIComponent()).requestFocus();
			((JTextField)(field.getUIComponent())).selectAll();
			
		}
		
		private String numberFieldInfo(NumberFieldBase<?> field) {
			String s = "";
			if (field.getMin() == null && field.getMax() != null)
				s = "Only values <= " + field.getMax() + " allowed.";
			else if (field.getMin() != null && field.getMax() == null)
				s = "Only values >= " + field.getMin() + " allowed.";
			else if (field.getMin() != null && field.getMax() != null)
				s = "Only values between " + field.getMin() + " and " + field.getMax() + " allowed.";
			return s; 
		}
		
	}
	
	private class NameAndDescription {
		public String name;
		public String desc;
		NameAndDescription(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}
	}

}
