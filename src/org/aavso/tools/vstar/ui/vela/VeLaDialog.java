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

import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextAreaTabs;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.vela.AST;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaPrefs;

/**
 * A dialog in which to run VeLa code.
 */
@SuppressWarnings("serial")
public class VeLaDialog extends TextDialog {

    private static final String tabTextSeparator = "===---===";

    private static ITextComponent<String> codeTextArea;
    private static ITextComponent<String> resultTextArea;

    private static VeLaInterpreter vela;

    private static String code = "";

    private String path;

    private static List<ITextComponent<String>> createTextAreas() {
        codeTextArea = new TextArea("VeLa Code", code, 12, 42, false, true);
        addKeyListener();

        boolean diagnosticMode = VeLaPrefs.getDiagnosticMode();

        if (diagnosticMode) {
            resultTextArea = new TextAreaTabs(Arrays.asList("Output", "LISP AST", "DOT AST"), Arrays.asList("", "", ""),
                    15, 70, true, true, tabTextSeparator);
        } else {
            resultTextArea = new TextArea("Output", "", 12, 42, true, true);
        }

        Font font = codeTextArea.getUIComponent().getFont();
        codeTextArea.getUIComponent().setFont(new Font(Font.MONOSPACED, Font.PLAIN, font.getSize()));
        resultTextArea.getUIComponent().setFont(new Font(Font.MONOSPACED, Font.PLAIN, font.getSize()));

        return Arrays.asList(codeTextArea, resultTextArea);
    }

    public VeLaDialog(String title) {
        super(title, createTextAreas(), true, true);
        path = "Untitled";
    }

    public VeLaDialog(String title, String code) {
        this(title);
        VeLaDialog.code = code;
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

        return panel;
    }

    // Helpers

    @Override
    protected void okAction() {
        VeLaDialog.code = VeLaDialog.codeTextArea.getValue();
        super.okAction();
    }

    private static void addKeyListener() {
        JTextArea area = (JTextArea) (codeTextArea.getUIComponent());
        area.addKeyListener(new KeyAdapter() {
            boolean escapeMode = false;

            @Override
            public void keyTyped(KeyEvent e) {
                char ch = e.getKeyChar();
                String newCh = null;

                if (escapeMode) {
                    switch (ch) {
                    case 'b':
                    case 'B':
                        // Boolean set
                        newCh = "\uD835\uDD39";
                        break;
                    case 'l':
                    case 'L':
                        // lambda
                        newCh = "\u03BB";
                        break;
                    case 'p':
                    case 'P':
                        // pi
                        newCh = "\u03C0";
                        break;
                    case 'r':
                    case 'R':
                        // real number set
                        newCh = "\u211D";
                        break;
                    case 'z':
                    case 'Z':
                        // integer number set
                        newCh = "\u2124";
                        break;
                    case '\\':
                        // backslash
                        newCh = "\\";
                        break;
                    }

                    e.consume();

                    int pos = area.getCaretPosition();
                    area.insert(newCh, pos);
                    escapeMode = false;

                } else if (ch == '\\') {
                    escapeMode = true;
                    e.consume();
                }
            }
        });
    }

    private void execute() {
        boolean diagnostic = VeLaPrefs.getDiagnosticMode();

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
            vela = new VeLaInterpreter(VeLaPrefs.getVerboseMode(), true, VeLaPrefs.getCodeDirsList());

            Pair<Optional<Operand>, AST> pair = vela.veLaToResultASTPair(text);

            Optional<Operand> result = pair.first;

            if (result.isPresent()) {
                AST ast = pair.second;
                if (diagnostic && ast != null) {
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
            // e.printStackTrace();

            // Show error in text area.
            String msg = e.getLocalizedMessage();
            if (msg != null) {
                error = msg;
            } else {
                error = "Error";
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

        String result = output;
        if (!error.isEmpty()) {
            result += "\n";
            result += error;
        }

        resultTextArea.setValue(areaTabsPayload(VeLaPrefs.getDiagnosticMode(), result, lispAST, dotAST));
    }

    private String areaTabsPayload(boolean verbose, String... strings) {
        StringBuffer buf = new StringBuffer();

        for (String str : strings) {
            buf.append(str);
            if (verbose) {
                buf.append(tabTextSeparator);
            } else {
                buf.append("\n");
            }
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
