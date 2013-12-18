/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * This class represents a help content frame.
 * 
 * Adapted from the JDK Sun API docs.
 * 
 * @deprecated The user manual has replaced this.
 */
@SuppressWarnings("serial")
public class HelpContentsDialog extends JDialog {

	/**
	 * Constructor.
	 */
	public HelpContentsDialog() {
		super(DocumentManager.findActiveWindow());
		this.getContentPane().add(createHelpPane());
	}

	/**
	 * Create a scrollable help pane capable of navigating hyperlinks.
	 */
	private Component createHelpPane() {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(createHyperlinkListener());
		setTitle("VStar Help");

		java.net.URL helpURL = ResourceAccessor.getHelpHTMLResource();

		if (helpURL != null) {
			try {
				editorPane.setPage(helpURL);
			} catch (IOException e) {
				MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
						"Help Contents...", "Unable to read URL: " + helpURL);
			}
		} else {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
					"Help Contents...", "Unable to find HelpContents.html");
		}

		// Put the editor pane in a scroll pane.
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(600, 600));
		editorScrollPane.setMinimumSize(new Dimension(100, 100));

		return editorScrollPane;
	}

	/**
	 * Return a hyperlink listener.
	 */
	private HyperlinkListener createHyperlinkListener() {
		final JDialog parent = this;
		return new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					JEditorPane pane = (JEditorPane) e.getSource();
					if (e instanceof HTMLFrameHyperlinkEvent) {
						// TODO: cursor change doesn't seem to work; need JFrame
						// vs JDialog?
						parent.setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
						HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
						HTMLDocument doc = (HTMLDocument) pane.getDocument();
						doc.processHTMLFrameHyperlinkEvent(evt);
					} else {
						try {
							pane.setPage(e.getURL());
							parent.setCursor(null);
						} catch (IOException ex) {
							parent.setCursor(null);
							MessageBox.showErrorDialog(Mediator.getUI()
									.getComponent(), "Help Contents...", ex);
						}
					}
				}
			}
		};
	}
}
