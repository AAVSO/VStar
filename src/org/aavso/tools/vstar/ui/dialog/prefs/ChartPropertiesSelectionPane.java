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
package org.aavso.tools.vstar.ui.dialog.prefs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.IntegerField;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.ChartPropertiesPrefs;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.ui.FontChooserPanel;

/**
 * This preferences pane permits the selection of general chart properties.
 */
@SuppressWarnings("serial")
public class ChartPropertiesSelectionPane extends JPanel implements
IPreferenceComponent {
	
	class FontDialog extends
			AbstractOkCancelDialog {

		private Font font = null;
		private FontChooserPanel fontChooserPanel;
		
		/**
		 * Constructor
		 */
		public FontDialog(Font font) {
			super("Font");
			this.font = font;
			
			Container contentPane = this.getContentPane();
			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));			
			
			fontChooserPanel = new FontChooserPanel(font);
			topPane.add(fontChooserPanel);
			
			// OK, Cancel
			topPane.add(createButtonPane());

			contentPane.add(topPane);

			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			this.setVisible(true);
		}
		
		public Font getFont() {
			return font;
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

			font = fontChooserPanel.getSelectedFont();
			
			if (ok) {
				cancelled = false;
				setVisible(false);
				dispose();
			}
		}
	}	
	
	/**
	 * A color rectangle
	 */
	class ColorRectComponent extends JComponent {
		
		private Color color;
		private boolean colorChanged;

		public boolean getColorChanged() {
			return colorChanged;
		}
		
		public void resetColorChanged() {
			colorChanged = false;
		}
		
		public ColorRectComponent(Color color) {
			setColor(color);
			resetColorChanged();
		}

		public Color getColor() {
			return color;
		}
		
		public void setColor(Color color) {
			if (color == null)
				color = Color.BLACK;
			if (!color.equals(this.color)) {
				colorChanged = true;
				this.color = color;
				repaint();
			}
		}
		
		@Override
		public void paint(Graphics g) {
			Graphics2D gfx = (Graphics2D) g;
			int height = this.getHeight();
			int width = this.getWidth();
			Shape rect = new Rectangle2D.Float(0, 0, width - 1, height - 1);
			gfx.setPaint(color);
			gfx.fill(rect);
			gfx.setPaint(Color.BLACK);
			gfx.draw(rect);
		}
	}
	
	class ChartFontLabel extends JLabel {
		
		private boolean fontChanged = false;
		
		public ChartFontLabel(String text) {
			super(text);
		}
		
		@Override
		public void setFont(Font font) {
			super.setFont(font);
			fontChanged = true;
		}
		
		public void resetFontChanged() {
			fontChanged = false;
		}
		
		public boolean isFontChanged() {
			return fontChanged;
		}
		
		public void setFontChanged(boolean status) {
			fontChanged = status;
		}
	}
	
	private ColorRectComponent backColorRect;	
	private ColorRectComponent gridColorRect;

	private ChartFontLabel regularFontLabel;
	private ChartFontLabel smallFontLabel;
	private ChartFontLabel extraLargeFontLabel;
	private ChartFontLabel largeFontLabel;
	
	private JSpinner pngScaleFactor; 
	private Integer changedScaleFactor = null;
	
	/**
	 * Constructor.
	 */
	public ChartPropertiesSelectionPane() {
		super();
		
		JPanel chartPropertiesPane = new JPanel();
		chartPropertiesPane.setLayout(new BoxLayout(chartPropertiesPane, BoxLayout.PAGE_AXIS));
		chartPropertiesPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		chartPropertiesPane.add(createControlPane());
		
		chartPropertiesPane.add(Box.createRigidArea(new Dimension(10, 10)));		
		
		chartPropertiesPane.add(createControlPane2());
		
		chartPropertiesPane.add(Box.createRigidArea(new Dimension(10, 10)));
		
		chartPropertiesPane.add(createButtonPane());
		
		this.add(chartPropertiesPane);
		
	}

	private JSpinner createScaleSpinner(int initial, String title) {
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
				initial, 
				ChartPropertiesPrefs.MIN_PNG_SCALE_FACTOR,
				ChartPropertiesPrefs.MAX_PNG_SCALE_FACTOR,
				1);
		JSpinner spinner = new JSpinner(spinnerModel);
		spinner.setBorder(BorderFactory.createTitledBorder(title));
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner src = (JSpinner)(e.getSource());
				changedScaleFactor = (Integer)src.getValue();
			}
		});
		return spinner;
	}
	
	protected JPanel createControlPane() {
		JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
		JButton selectBtn;

		panel.add(new JLabel("Background Color"));
		backColorRect = new ColorRectComponent(Color.WHITE);
		panel.add(backColorRect);		
		selectBtn = new JButton("Select...");
		selectBtn.addActionListener(createSelectBackgroundColorActionListener());
		panel.add(selectBtn);
				
		panel.add(new JLabel("Gridline Color"));
		gridColorRect = new ColorRectComponent(Color.WHITE);
		panel.add(gridColorRect);		
		selectBtn = new JButton("Select...");
		selectBtn.addActionListener(createSelectGridlineColorActionListener());
		panel.add(selectBtn);

		panel.add(new JLabel("Regular Chart Font"));		
		regularFontLabel = new ChartFontLabel("Regular 3.14159");
		panel.add(regularFontLabel);
		selectBtn = new JButton("Select...");
		selectBtn.addActionListener(createSelectRegularFontActionListener());
		panel.add(selectBtn);
		
		panel.add(new JLabel("Small Chart Font"));		
		smallFontLabel = new ChartFontLabel("Small 3.14159");
		panel.add(smallFontLabel);
		selectBtn = new JButton("Select...");
		selectBtn.addActionListener(createSelectSmallFontActionListener());
		panel.add(selectBtn);

		panel.add(new JLabel("Large Chart Font"));		
		largeFontLabel = new ChartFontLabel("Large 3.14159");
		panel.add(largeFontLabel);
		selectBtn = new JButton("Select...");
		selectBtn.addActionListener(createSelectLargeFontActionListener());
		panel.add(selectBtn);

		panel.add(new JLabel("XLarge Chart Font"));		
		extraLargeFontLabel = new ChartFontLabel("XLarge 3.14159");
		panel.add(extraLargeFontLabel);
		selectBtn = new JButton("Select...");
		selectBtn.addActionListener(createSelectExtraLargeFontActionListener());
		panel.add(selectBtn);
		
		return panel;
	}
	
	protected JPanel createControlPane2() {
		JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));

		panel.add(new JLabel("Use the factor"));
		pngScaleFactor = createScaleSpinner(ChartPropertiesPrefs.MIN_PNG_SCALE_FACTOR, "Factor");
		panel.add(pngScaleFactor);
		panel.add(new JLabel("while saving PNG"));
		
		return panel;
	}
	
	protected JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton setDefaultsButton = new JButton("Set Defaults");
		setDefaultsButton.addActionListener(createSetDefaultsButtonActionListener());
		panel.add(setDefaultsButton, BorderLayout.LINE_START);

		JButton applyButton = new JButton(LocaleProps.get("APPLY_BUTTON"));
		applyButton.addActionListener(createApplyButtonActionListener());
		panel.add(applyButton, BorderLayout.LINE_END);

		return panel;
	}
	
	
	// Select Background color action button listener.
	private ActionListener createSelectBackgroundColorActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(ChartPropertiesSelectionPane.this,
	                     "Choose Background Color",
	                     backColorRect.getColor());
				if (newColor != null) {
					backColorRect.setColor(newColor);
				}
			}
		};
	}
	
	// Select Gridline color action button listener.
	private ActionListener createSelectGridlineColorActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(ChartPropertiesSelectionPane.this,
	                     "Choose Gridline Color",
	                     gridColorRect.getColor());
				if (newColor != null) {
					gridColorRect.setColor(newColor);
				}
			}
		};
	}

	// Select Regular Font action button listener.
	private ActionListener createSelectRegularFontActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FontDialog paramDialog = new FontDialog(regularFontLabel.getFont());
				if (paramDialog.isCancelled())
					return;
				Font newFont = paramDialog.getFont();
				if (newFont != null) {
					regularFontLabel.setFont(newFont);
				}
			}
		};
	}

	// Select Small Font action button listener.
	private ActionListener createSelectSmallFontActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FontDialog paramDialog = new FontDialog(smallFontLabel.getFont());
				if (paramDialog.isCancelled())
					return;
				Font newFont = paramDialog.getFont();
				if (newFont != null) {
					smallFontLabel.setFont(newFont);
				}
			}
		};
	}

	// Select Large Font action button listener.
	private ActionListener createSelectLargeFontActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FontDialog paramDialog = new FontDialog(largeFontLabel.getFont());
				if (paramDialog.isCancelled())
					return;
				Font newFont = paramDialog.getFont();
				if (newFont != null) {
					largeFontLabel.setFont(newFont);
				}
			}
		};
	}

	// Select Extra Large Font action button listener.
	private ActionListener createSelectExtraLargeFontActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FontDialog paramDialog = new FontDialog(extraLargeFontLabel.getFont());
				if (paramDialog.isCancelled())
					return;
				Font newFont = paramDialog.getFont();
				if (newFont != null) {
					extraLargeFontLabel.setFont(newFont);
				}
			}
		};
	}
	
	// Set defaults action button listener.
	private ActionListener createSetDefaultsButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChartPropertiesPrefs.setDefaultChartPrefs();
				reset();
				updateCharts();
			}
		};
	}
	
	// Set apply button listener.
	private ActionListener createApplyButtonActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		};
	}
	
	private void updateCharts() {
		Mediator.getInstance().updateChartProperties();
	}
	
	/**
	 * Updates the chart properties.
	 */
	@Override
	public void update() {
		boolean delta = false;
		if (backColorRect.getColorChanged()) {
			ChartPropertiesPrefs.setChartBackgroundColor(backColorRect.getColor());
			delta = true;
		}
		if (gridColorRect.getColorChanged()) {
			ChartPropertiesPrefs.setChartGridlinesColor(gridColorRect.getColor());
			delta = true;
		}
		if (regularFontLabel.isFontChanged()) {
			ChartPropertiesPrefs.setChartRegularFont(regularFontLabel.getFont());
			delta = true;
		}
		if (smallFontLabel.isFontChanged()) {
			ChartPropertiesPrefs.setChartSmallFont(smallFontLabel.getFont());
			delta = true;
		}
		if (largeFontLabel.isFontChanged()) {
			ChartPropertiesPrefs.setChartLargeFont(largeFontLabel.getFont());
			delta = true;
		}
		if (extraLargeFontLabel.isFontChanged()) {
			ChartPropertiesPrefs.setChartExtraLargeFont(extraLargeFontLabel.getFont());
			delta = true;
		}
		if (changedScaleFactor != null) {
			ChartPropertiesPrefs.setScaleFactor(changedScaleFactor);
			delta = true;
		}
		if (delta) {
			ChartPropertiesPrefs.storeChartPropertiesPrefs();
			backColorRect.resetColorChanged();
			gridColorRect.resetColorChanged();
			regularFontLabel.resetFontChanged();
			smallFontLabel.resetFontChanged();
			largeFontLabel.resetFontChanged();
			extraLargeFontLabel.resetFontChanged();
			changedScaleFactor = null; 
			updateCharts();			
		}
	}
	
	/**
	 * Prepare this pane.
	 */
	@Override
	public void reset() {
		backColorRect.setColor(ChartPropertiesPrefs.getChartBackgroundColor());
		gridColorRect.setColor(ChartPropertiesPrefs.getChartGridlinesColor());
		backColorRect.resetColorChanged();		
		gridColorRect.resetColorChanged();
		
		Font font;
		StandardChartTheme chartTheme = (StandardChartTheme)StandardChartTheme.createJFreeTheme();

		font = ChartPropertiesPrefs.getChartRegularFont();
		if (font == null)
			font = chartTheme.getRegularFont();
		regularFontLabel.setFont(font);
		regularFontLabel.resetFontChanged();
		
		font = ChartPropertiesPrefs.getChartSmallFont();
		if (font == null)
			font = chartTheme.getSmallFont();
		smallFontLabel.setFont(font);
		smallFontLabel.resetFontChanged();

		font = ChartPropertiesPrefs.getChartLargeFont();
		if (font == null)
			font = chartTheme.getLargeFont();
		largeFontLabel.setFont(font);
		largeFontLabel.resetFontChanged();
		
		font = ChartPropertiesPrefs.getChartExtraLargeFont();
		if (font == null)
			font = chartTheme.getExtraLargeFont();
		extraLargeFontLabel.setFont(font);
		extraLargeFontLabel.resetFontChanged();
		
		pngScaleFactor.setValue(ChartPropertiesPrefs.getScaleFactor());
		changedScaleFactor = null;
	}
	
}
