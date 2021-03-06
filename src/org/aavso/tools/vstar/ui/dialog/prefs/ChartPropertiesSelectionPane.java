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
import java.awt.Dimension;
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

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.ChartPropertiesPrefs;

/**
 * This preferences pane permits the selection of general chart properties.
 */
@SuppressWarnings("serial")
public class ChartPropertiesSelectionPane extends JPanel implements
IPreferenceComponent {
	
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
	
	private ColorRectComponent backColorRect;	
	private ColorRectComponent gridColorRect;
	
	
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
		
		chartPropertiesPane.add(createButtonPane());
		
		this.add(chartPropertiesPane);
		
	}

	protected JPanel createControlPane() {
		JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));

		panel.add(new JLabel("Background Color"));
		backColorRect = new ColorRectComponent(Color.WHITE);
		panel.add(backColorRect);		
		JButton backColorBtn = new JButton("Select...");
		backColorBtn.addActionListener(createSelectBackgroundColorActionListener());
		panel.add(backColorBtn);
				
		panel.add(new JLabel("Gridline Color"));
		gridColorRect = new ColorRectComponent(Color.WHITE);
		panel.add(gridColorRect);		
		JButton gridColorBtn = new JButton("Select...");
		gridColorBtn.addActionListener(createSelectGridlineColorActionListener());
		panel.add(gridColorBtn);
		
		return panel;
	}
	
	protected JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton setDefaultsButton = new JButton("Set Default Colors");
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
		if (delta) {
			ChartPropertiesPrefs.storeChartPropertiesPrefs();
			backColorRect.resetColorChanged();
			gridColorRect.resetColorChanged();
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
	}
	
}
