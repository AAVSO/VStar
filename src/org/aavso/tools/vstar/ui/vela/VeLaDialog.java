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
package org.aavso.tools.vstar.ui.vela;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.vela.AST;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.VeLaInterpreter;

/**
 * A dialog in which to run VeLa code.
 */
@SuppressWarnings("serial")
public class VeLaDialog extends TextDialog {

    private static ITextComponent<String> codeTextArea;
    private static TextArea resultTextArea;
    private static JCheckBox verbosityCheckBox;

    private static VeLaInterpreter vela;

    private String path;

    static {
        codeTextArea = new TextArea("VeLa Code", "", 12, 42, false, true);
        // resultTextArea = new TextAreaTabs(Arrays.asList("Output", "Error",
        // "AST", "DOT"), Arrays.asList("", "", "", ""), 15, 70,
        // true, true);
        // resultTextArea = new TextAreaTabs(Arrays.asList("Output", "Error"),
        // Arrays.asList("", ""), 10, 40, true, true);
        resultTextArea = new TextArea("Output", "", 12, 42, true, true);

        verbosityCheckBox = new JCheckBox("Verbose?");
        verbosityCheckBox.setSelected(false);
        verbosityCheckBox.setVisible(false);

        // vela = new VeLaInterpreter(false);
    }

    public VeLaDialog(String title) {
        super(title, Arrays.asList(codeTextArea, resultTextArea));
        path = "Untitled";
    }

    public VeLaDialog(String title, String code) {
        this(title);
        codeTextArea.setValue(code);
    }

    public VeLaDialog() {
        this("VeLa");
    }

    /**
     * @return the most recently loaded/saved file path
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the VeLa code.
     * 
     * @return A string containing the code.
     */
    public String getCode() {
        return codeTextArea.getStringValue();
    }

    @Override
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

        JButton loadButton = new JButton(LocaleProps.get("LOAD_BUTTON"));
        loadButton.addActionListener(e -> {
            try {
                Pair<String, String> content = Mediator.getInstance().getVelaFileLoadDialog().readFileAsString(this,
                        null);
                if (content != null) {
                    codeTextArea.setValue(content.first);
                }
            } catch (Exception ex) {
                MessageBox.showErrorDialog(this, getTitle(), ex);
            }
        });

        panel.add(loadButton);

        JButton saveButton = new JButton(LocaleProps.get("SAVE_BUTTON"));
        saveButton.addActionListener(e -> {
            try {
                String content = codeTextArea.getValue();
                Mediator.getInstance().getVelaFileSaveDialog().writeStringToFile(this, content, null);
            } catch (Exception ex) {
                MessageBox.showErrorDialog(this, getTitle(), ex);
            }
        });
        panel.add(saveButton);

        JButton dismissButton = new JButton(LocaleProps.get("OK_BUTTON"));
        dismissButton.addActionListener(e -> {
            okAction();
        });
        panel.add(dismissButton);

        panel.add(verbosityCheckBox);

        return panel;
    }

    // Helpers

    private void execute() {
        boolean verbose = verbosityCheckBox.isSelected();

        String text = codeTextArea.getValue();

        String output = "";
        String error = "";
        String lispAST = "";
        String dotAST = "";

        // Capture standard output and error
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errStream));

        try {
            Mediator.getUI().setScriptingStatus(true);

            // Compile and execute the code.
            vela = new VeLaInterpreter(false, true, Collections.emptyList());
            vela.setVerbose(verbose);

            Pair<Optional<Operand>, AST> pair = vela.veLaToResultASTPair(text);

            Optional<Operand> result = pair.first;

            if (result.isPresent()) {
                AST ast = pair.second;
                if (verbose && ast != null) {
                    lispAST = ast.toString();
                    dotAST = ast.toFullDOT();
                }
            }

            // Any standard error or output to show?
            error = showOutput(errStream);
            output = showOutput(outStream);

            // Is there a result to show and no error?
            if (result.isPresent() && "".equals(error)) {
                output += result.get().toHumanReadableString();
            }
        } catch (Exception e) {
            // Show error in text area.
            String msg = e.getLocalizedMessage();
            if (msg != null) {
                error = msg;
            }

            if (msg != null && !msg.equals(errStream.toString())) {
                // Any standard error to show in relation to this exception?
                // Don't repeat msg.
                error += showOutput(errStream);
            }
        } finally {
            // Reset standard output and error to console.
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));

            Mediator.getUI().setScriptingStatus(false);
        }

        resultTextArea.setValue(areaTabsPayload(output, error));
    }

    private String areaTabsPayload(String... strings) {
        StringBuffer buf = new StringBuffer();

        for (String str : strings) {
            buf.append(str);
            buf.append("\n");
        }

        return buf.toString().trim();
    }

    private String showOutput(ByteArrayOutputStream stream) {
        String str = "";

        if (stream.size() != 0) {
            str = stream.toString() + "\n";
        }

        return str;
    }
}
