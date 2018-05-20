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
package org.aavso.tools.vstar.ui.dialog.vela;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.VeLaInterpreter;

/**
 * A dialog in which to run VeLa code.
 */
@SuppressWarnings("serial")
public class VeLaDialog extends TextDialog {

	private static ITextComponent<String> codeTextArea;
	private static ITextComponent<String> resultTextArea;
	private static JCheckBox verbosityCheckBox;
	private static VeLaInterpreter vela;

	static {
		codeTextArea = new TextArea("VeLa Code", 10, 40);
		resultTextArea = new TextArea("Result", 10, 40);

		verbosityCheckBox = new JCheckBox("Verbose?");
		verbosityCheckBox.setSelected(false);

		vela = new VeLaInterpreter(false);
	}

	public VeLaDialog() {
		super("VeLa", Arrays.asList(codeTextArea, resultTextArea));
	}

	protected JPanel createButtonPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		JButton cancelButton = new JButton(LocaleProps.get("CANCEL_BUTTON"));
		cancelButton.addActionListener(createCancelButtonListener());
		panel.add(cancelButton);

		JButton clearButton = new JButton(LocaleProps.get("CLEAR_BUTTON"));
		clearButton.addActionListener(e -> {
			codeTextArea.setValue("");
			resultTextArea.setValue("");
		});
		panel.add(clearButton);

		JButton runButton = new JButton(LocaleProps.get("RUN_BUTTON"));
		runButton.addActionListener(e -> {
			execute();
		});
		panel.add(runButton);

		// verbosityCheckBox.addActionListener(e -> {
		// vela.setVerbose(verbosityCheckBox.isSelected());
		// });
		// panel.add(verbosityCheckBox);

		return panel;
	}

	// Helpers

	private void execute() {
		String text = codeTextArea.getValue();

		// Clear the result text area and capture standard output and error.
		resultTextArea.setValue("");

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outStream));

		ByteArrayOutputStream errStream = new ByteArrayOutputStream();
		System.setErr(new PrintStream(errStream));

		try {
			// Compile and execute the code.
			Optional<Operand> result = vela.program(text);

			// Any standard error or output to show?
			boolean wasErrOutput = showOutput(errStream, true);
			showOutput(outStream, false);

			// Is there a result to show?
			if (result.isPresent() && !wasErrOutput) {
				String str = resultTextArea.getValue();
				str += result.get().toHumanReadableString();
				resultTextArea.setValue(str);
			}
		} catch (Exception e) {
			// Show error in text area.
			String msg = e.getLocalizedMessage();
			if (msg != null) {
				resultTextArea.setValue(msg + "\n");
			}

			// Any standard error to show in relation to this exception?
			showOutput(errStream, true);
		} finally {
			// Reset standard output and error to console.
			System.setOut(new PrintStream(new FileOutputStream(
					FileDescriptor.out)));

			System.setErr(new PrintStream(new FileOutputStream(
					FileDescriptor.err)));
		}
	}

	private boolean showOutput(ByteArrayOutputStream stream, boolean clearStack) {
		boolean wasOutput = false;

		if (stream.size() != 0) {
			String str = resultTextArea.getValue();
			str += stream.toString() + "\n";
			resultTextArea.setValue(str);
			wasOutput = true;
		}

		return wasOutput;
	}
}
