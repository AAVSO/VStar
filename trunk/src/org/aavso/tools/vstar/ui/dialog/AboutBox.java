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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * This class creates and displays VStar's About Box.
 * 
 * See also
 * https://sourceforge.net/tracker/?func=detail&aid=2870716&group_id=263306
 * &atid=1152052 which will result in this being done better.
 */
public class AboutBox extends JDialog {

	public AboutBox() {
		super();
		this.setTitle("About VStar");
		this.setModal(true);
		this.setAlwaysOnTop(true);

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createMainPane());
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}

	private Component createMainPane() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

		JPanel iconPanel = new JPanel();
		iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.PAGE_AXIS));

		Icon camargo = ResourceAccessor
				.getIconResource("/images/EpsAur_1_0.jpg");
		JLabel camargoLabel = new JLabel(camargo);
		JPanel camargoPanel = new JPanel(new BorderLayout());
		camargoPanel.add(camargoLabel, BorderLayout.CENTER);
		iconPanel.add(camargoPanel);

		iconPanel.add(Box.createRigidArea(new Dimension(75, 1)));

		Icon pose = ResourceAccessor.getIconResource("/images/vstar_pose1.jpg");
		JLabel poseLabel = new JLabel(pose);
		JPanel posePanel = new JPanel(new BorderLayout());
		posePanel.add(poseLabel, BorderLayout.CENTER);
		iconPanel.add(posePanel);

		iconPanel.add(Box.createRigidArea(new Dimension(75, 1)));

		Icon thieme = ResourceAccessor
				.getIconResource("/images/tenstar_artist_conception1.jpg");
		JLabel thiemeLabel = new JLabel(thieme);
		JPanel thiemePanel = new JPanel(new BorderLayout());
		thiemePanel.add(thiemeLabel, BorderLayout.CENTER);
		iconPanel.add(thiemePanel);

		iconPanel.add(Box.createRigidArea(new Dimension(75, 1)));

		Icon nsf = ResourceAccessor.getIconResource("/images/nsf1.jpg");
		JLabel nsfLabel = new JLabel(nsf);

		Icon cs = ResourceAccessor.getIconResource("/images/citizen_sky.png");
		JLabel csLabel = new JLabel(cs);

		Icon aavso = ResourceAccessor.getIconResource("/images/aavso.jpg");
		JLabel aavsoLabel = new JLabel(aavso);

		JPanel orgPanel = new JPanel(new BorderLayout());
		orgPanel.add(csLabel, BorderLayout.LINE_START);
		orgPanel.add(nsfLabel, BorderLayout.CENTER);
		orgPanel.add(aavsoLabel, BorderLayout.LINE_END);
		iconPanel.add(orgPanel);

		topPanel.add(iconPanel);

		topPanel.add(Box.createRigidArea(new Dimension(25, 1)));

		JTextArea textArea = new JTextArea();
		textArea.setBorder(BorderFactory.createEtchedBorder());
		textArea.setEditable(false);
		textArea.setText(getAboutBoxText());
		topPanel.add(textArea);

		return new JScrollPane(topPanel);
	}

	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(createDismissButtonListener());
		panel.add(dismissButton, BorderLayout.LINE_END);

		this.getRootPane().setDefaultButton(dismissButton);

		return panel;
	}

	// Return a listener for the "Dismiss" button.
	protected ActionListener createDismissButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}

	// Comment from Aaron in email (6 July 2009):
	//	
	// I think you can use anything in HOA or the Citizen Sky web site.
	// However, you'll need to retain whatever credit is shown in VSA and
	// maybe add "as appeared in VSA at
	// "http://www.aavso.org/education/vsa/".
	//
	// Add a credit to the National Science Foundation to the About
	// box (if you haven't already). A good NSF logo is here:
	// http://www.nsf.gov/policies/logos.jsp
	//
	// The NSF credit should be something like "This project was funded in
	// part by grant No. 000379097 from the National Science Foundation."

	public String getAboutBoxText() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("VStar ");
		strBuf.append(ResourceAccessor.getVersionString());
		strBuf.append(" (svn revision ");
		strBuf.append(ResourceAccessor.getRevNum());
		strBuf.append(")\n\n");

		strBuf.append("A variable star observation data analysis tool\n");
		strBuf.append("developed for:\n\n");
		strBuf.append("  The American Association of Variable Star\n");
		strBuf.append("  Observers: http://www.aavso.org/\n\n");
		strBuf.append("as part of:\n\n");
		strBuf
				.append("  The Citizen Sky Project: http://www.citizensky.org/\n\n");

		strBuf
				.append("This project was funded in part by grant No. 000379097\n");
		strBuf.append("from the National Science Foundation.\n\n");

		strBuf.append("Lead Developer: David Benn\n");
		strBuf.append("Contact: aavso@aavso.org\n");
		strBuf.append("License: GNU Affero General Public License\n\n");

		strBuf.append("Illustrations of Epsilon Aurigae are by Citizen Sky\n");
		strBuf.append("participants Nico Camargo and Brian Thieme.\n\n");

		strBuf.append("Thanks to the staff of AAVSO for their support and\n");
		strBuf.append("ongoing encouragement, in particular:\n\n");
		strBuf.append(" Sara Beck, Arne Henden, Doc Kinne, Aaron Price,\n");
		strBuf
				.append(" Matt Templeton, Rebecca Turner, and Elizabeth Waagen\n\n");

		strBuf.append("As guide, domain expert, AAVSO liason, and advocate,\n");
		strBuf.append("Sara has been indispensible.\n\n");

		strBuf.append("Thanks to Michael Umbricht for early testing, bug\n");
		strBuf.append("reports, advocacy, great conversations, and beer.\n\n");

		strBuf.append("Discussions with Grant Foster, the original VStar developer,\n");
		strBuf.append("and reading his book:\n");
		strBuf.append("  \"Analyzing Light Curves: A Practical Guide\" provided\n");
		strBuf.append("knowledge that would have been much harder to\n");
		strBuf.append("obtain otherwise.\n\n");

		strBuf.append("Doug Welch, Ken Mogul, and Alan Plummer have\n");
		strBuf
				.append("provided a keep-it-real end-user perspective. Doug's\n");
		strBuf
				.append("and Aaron's knowledge and advice are always appreciated.\n\n");

		strBuf.append("Thanks to the Weber brothers (Adam and George)\n");
		strBuf.append("for code and bug-fix contributions, Nico Camargo for\n");
		strBuf.append("the beautiful toolbar icons and to the broader\n");
		strBuf.append("Citizen Sky VStar Software Development for testing,\n");
		strBuf.append("advice, discussions, and encouragement.\n\n");

		strBuf.append("Finally, thanks Arne, for the conversation at NACAA\n");
		strBuf.append("2008 that got the ball rolling, and for the 2011 Director's\n");
		strBuf.append("Award.");
		
		return strBuf.toString();
	}
}
