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

package org.aavso.tools.vstar.external.plugin;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import org.aavso.tools.vstar.external.lib.ConvertHelper;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;
import org.aavso.tools.vstar.util.date.AbstractHJDConverter;
import org.aavso.tools.vstar.util.help.Help;
import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

public class JDtoBJDTool extends GeneralToolPluginBase {
	
	//RAInfo lastRA = null;
	//DecInfo lastDec = null;
	
	@Override
	public void invoke() {
		new JDtoBJDToolDialog();
	}

	@Override
	public String getDescription() {
		return "Convert JD to BJD_TDB";
	}

	@Override
	public String getDisplayName() {
		return "JD to BJD_TDB";
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return("JDtoBJDTool.pdf");
	}
	
	@SuppressWarnings("serial")
	class JDtoBJDToolDialog extends JDialog {
		
		private static final String sTITLE = "BJD Converter";
		
		private ConvertHelper.CoordPane coordPane;
		
		private JTextArea textArea1;
		private JTextArea textArea2;
		private JButton bUTCtoBJD;
		private JButton bHJDtoBJD;
		private JButton bUTCtoHJD;
		
		protected boolean closed = false;
		
		/**
		 * Constructor
		 */
		public JDtoBJDToolDialog()	{
			super(DocumentManager.findActiveWindow());
			setTitle(sTITLE);
			setModalityType(Dialog.ModalityType.MODELESS);
			
			ActionListener cancelListener = createCancelButtonListener();
		    getRootPane().registerKeyboardAction(cancelListener, 
		    		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
		    		JComponent.WHEN_IN_FOCUSED_WINDOW);

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			//coordPane = new ConvertHelper.CoordPane(lastRA, lastDec, false);
			coordPane = new ConvertHelper.CoordPane(null, null, false);
			
			topPane.add(coordPane);
			topPane.add(createMainPane());
			topPane.add(createButtonPane2(cancelListener));
			
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
		
		private JPanel createMainPane() {
			JPanel panel = new JPanel();
			textArea1 = new JTextArea(20, 30);
			panel.add(new JScrollPane(textArea1), BorderLayout.EAST);
			panel.add(createButtonPane(), BorderLayout.CENTER);
			textArea2 = new JTextArea(20, 30);
			textArea2.setEditable(false);
			panel.add(new JScrollPane(textArea2), BorderLayout.WEST);
			return panel;
		}

		private JPanel createButtonPane() {
			JPanel panel = new JPanel();
			
			bUTCtoBJD = new JButton("UTC->BJD");
			bUTCtoBJD.setToolTipText("Via " + ConvertHelper.ASTROUTILS_URL + " service");
			bUTCtoBJD.addActionListener(createUTCtoBJDButtonListener());
			panel.add(bUTCtoBJD, BorderLayout.NORTH);
			
			bHJDtoBJD = new JButton("HJD->BJD");
			bHJDtoBJD.setToolTipText("Via " + ConvertHelper.ASTROUTILS_URL + " service");
			bHJDtoBJD.addActionListener(createHJDtoBJDButtonListener());
			panel.add(bHJDtoBJD, BorderLayout.CENTER);

			bUTCtoHJD = new JButton("UTC->HJD");
			bUTCtoHJD.setToolTipText("Via internal routine");
			bUTCtoHJD.addActionListener(createUTCtoHJDButtonListener());
			panel.add(bUTCtoHJD, BorderLayout.SOUTH);
			
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			return panel;
		}
		
		private JPanel createButtonPane2(ActionListener cancelListener) {
			JPanel panel = new JPanel();

			JButton helpButton = new JButton("Help");
			helpButton.addActionListener(createHelpButtonListener());
			panel.add(helpButton);
			
			JButton cancelButton = new JButton("Close");
			cancelButton.addActionListener(cancelListener);
			panel.add(cancelButton);
			
			return panel;
		}

		// Return a listener for the "UTC to BJD" button.
		private ActionListener createUTCtoBJDButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ConvertUTCtoBJD();
				}
			};
		}

		// Return a listener for the "HJD to BJD" button.
		private ActionListener createHJDtoBJDButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ConvertHJDtoBJD();
				}
			};
		}

		// Return a listener for the "HJD to BJD" button.
		private ActionListener createUTCtoHJDButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ConvertUTCtoHJD();
				}
			};
		}
		
		// Return a listener for the "Close" button.
		private ActionListener createCancelButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closed = true;
					setVisible(false);
					dispose();
				}
			};
		}
		
		// Return a listener for the "Help" button.
		protected ActionListener createHelpButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Help.openPluginHelp(getDocName());
				}
			};
		}
	
		private void ConvertUTCtoBJD() {
			ConvertProc("utc2bjd");
		}
		
		private void ConvertHJDtoBJD() {
			ConvertProc("hjd2bjd");
		}
		
		private void ConvertUTCtoHJD() {
			ConvertProc("utc2hjd");
		}
		
		private void ConvertProc(String func) {
			
			Pair<RAInfo, DecInfo> coord = coordPane.getCoordinates();
			
			if (coord == null) return;
			
			//lastRA = coord.first;
			//lastDec = coord.second;
			//double ra = lastRA.toDegrees();
			//double dec = lastDec.toDegrees();
			
			double ra = coord.first.toDegrees();
			double dec = coord.second.toDegrees(); 
			
			String s = textArea1.getText().trim();
			
			if ("".equals(s)) {
				MessageBox.showErrorDialog("Error", "Empty list");
				return;
			}

			List<String> tempList = new ArrayList<String>(Arrays.asList(s.split("\n")));
			List<Double> times = new ArrayList<Double>();
			s = null;
			for (String s1: tempList) {
				String s2 = s1.trim();
				if (!"".equals(s2)) {
					double d;
					try {
						d = NumberParser.parseDouble(s2);
					} catch (Exception ex) {
						String error = ex.getMessage();
						if (error == null || "".equals(error))
							error = ex.toString();
						MessageBox.showErrorDialog("Error", "Cannot convert " + s2 + " to double.\nError: " + error);
						return;
					}
					times.add(d);
					if (s != null) 
						s += "\n";
					else
						s = "";
					s += s2;
				}
			}
			textArea1.setText(s);
			
			if ("utc2hjd".equals(func)) {
				performLocalConvertUTC2HJD(times, ra, dec);
			} else {
				performConvert(times, ra, dec, func);
			}
		}
	
		void performLocalConvertUTC2HJD(List<Double> times, double ra, double dec) {
			List<Double> result = new ArrayList<Double>();
			AbstractHJDConverter converter = AbstractHJDConverter.getInstance(EpochType.J2000);			
			for (Double d : times) {
				double d1 = converter.convert(d, new RAInfo(EpochType.J2000, ra), new DecInfo(EpochType.J2000, dec));
				result.add(d1);
			}
			displayOutput(result);
		}
		
		private void performConvert(List<Double>times, double ra, double dec, String func) {
			//System.out.println(urlString);
			textArea2.setText("Please wait...");
			bUTCtoBJD.setEnabled(false);
			bHJDtoBJD.setEnabled(false);
			bUTCtoHJD.setEnabled(false);
			SwingWorker<ConvertResult, Object> worker = new JDtoBJDToolSwingWorker(this, times, ra, dec, func);
			worker.execute();
		}
		
		public void displayOutput(List<Double> out_times) {
			textArea2.setText(null);
			if (out_times == null)
				return;
			String s_out = "";
			for (Double d : out_times) {
				String s1 = NumericPrecisionPrefs.getTimeOutputFormat().format(d);
				s_out += (s1 + "\n");
			}
			textArea2.setText(s_out);
		}
		
	}
	
	private class ConvertResult {
		public List<Double> out_times = null;
		public String error = null;
	}
	
	class JDtoBJDToolSwingWorker extends SwingWorker<ConvertResult, Object>	{

		private JDtoBJDTool.JDtoBJDToolDialog dialog;
		private List<Double> times;
		private double ra;
		private double dec;
		private String func;

		public JDtoBJDToolSwingWorker(JDtoBJDTool.JDtoBJDToolDialog dialog, List<Double>times, double ra, double dec, String func)
		{
			this.dialog = dialog;
			this.times = times;
			this.ra = ra;
			this.dec = dec;
			this.func = func;
		}
		
		@Override
		protected ConvertResult doInBackground() throws Exception {
			ConvertResult result = new ConvertResult();
			try {
				result.out_times = ConvertHelper.getConvertedListOfTimes(times, ra, dec, func);
				result.error = null;
			} catch (Exception ex) {
				result.out_times = null;
				result.error = ex.getMessage();
				if (result.error == null || "".equals(result.error))
					result.error = ex.toString();
				
			}
			return result;
		}
		
		@Override
		protected void done()  
		{
			ConvertResult result = null;
			String error = "Unknown error";
			try { 
				result = get();
				if (result != null && result.error != null) {
					error = result.error;
					result = null;
				}
			} catch (InterruptedException ex) { 
				error = "Interrupted";
			} catch (ExecutionException ex)	{ 
				Throwable cause = ex.getCause();
				error = cause.getMessage();
				if (error == null || "".equals(error))
					error = cause.toString();
				error = "Execution Exception: \n" + error;
			} finally {
				if (!dialog.closed)
				{
					dialog.bUTCtoBJD.setEnabled(true);
					dialog.bHJDtoBJD.setEnabled(true);
					dialog.bUTCtoHJD.setEnabled(true);
					if (result != null) {
						dialog.displayOutput(result.out_times);
					} else {
						dialog.displayOutput(null);
						MessageBox.showErrorDialog("Error", error);
					}
					dialog.pack();
				}
			}
		}
				
				
		
	}

}
