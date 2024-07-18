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
package org.aavso.tools.vstar.vela;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.Pair;

/**
 * This class allows a VeLa program to be run from the command-line or for its
 * LISP or DOT AST to be sent to standard output.
 */
public class VeLaScriptDriver {
    public static void main(String[] args) {
        if (args.length >= 1 && args.length <= 5) {
            BufferedReader reader = null;
            try {
                // Process command-line arguments.
                String velaSourceFile = null;
                boolean verbose = false;
                boolean restartOnError = false;
                boolean lispAST = false;
                boolean dotAST = false;

                for (String arg : args) {
                    if (arg.startsWith("--")) {
                        if ("--verbose".equals(arg)) {
                            verbose = true;
                        }
                        if ("--restart".equals(arg)) {
                            restartOnError = true;
                        }
                        if ("--lisp".equals(arg)) {
                            lispAST = true;
                        }
                        if ("--dot".equals(arg)) {
                            dotAST = true;
                        }
                    } else {
                        velaSourceFile = arg;
                    }
                }

                if (Mediator.getUI() != null) {
                    Mediator.getUI().setScriptingStatus(true);
                }

                // Run interpreter, optionally restarting it on error.
                VeLaInterpreter vela = new VeLaInterpreter(verbose);
                do {
                    try {
                        reader = new BufferedReader(new FileReader(velaSourceFile));
                        StringBuffer buf = new StringBuffer();
                        String line = reader.readLine();
                        while (line != null) {
                            buf.append(line);
                            buf.append("\n");
                            line = reader.readLine();
                        }

                        Pair<Optional<Operand>, AST> pair = vela.veLaToResultASTPair(buf.toString());

                        Optional<Operand> result = pair.first;
                        if (result.isPresent()) {
                            System.out.println(result.get());
                        }

                        if (lispAST) {
                            System.out.println(pair.second.toString());
                        }

                        if (dotAST) {
                            System.out.println(pair.second.toFullDOT());
                        }

                    } catch (Throwable t) {
                        System.err.println(t.getLocalizedMessage());
                        t.printStackTrace();
                    }
                } while (restartOnError);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                }

                if (Mediator.getUI() != null) {
                    Mediator.getUI().setScriptingStatus(false);
                }
            }
        }
    }
}
