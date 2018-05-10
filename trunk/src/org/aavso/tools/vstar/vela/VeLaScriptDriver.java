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

/**
 * This class allows a VeLa program to be run from the command-line.
 */
public class VeLaScriptDriver {
	public static void main(String[] args) {
		if (args.length == 1 || args.length == 2) {
			BufferedReader reader = null;
			try {
				String velaSourceFile = null;
				boolean verbose = false;
				if (args.length == 1) {
					velaSourceFile = args[0];
				} else {
					verbose = "--verbose".equals(args[0]);
					velaSourceFile = args[1];
				}
				VeLaInterpreter vela = new VeLaInterpreter(verbose);
				reader = new BufferedReader(new FileReader(velaSourceFile));
				StringBuffer buf = new StringBuffer();
				String line = reader.readLine();
				while (line != null) {
					buf.append(line);
					buf.append("\n");
					line = reader.readLine();
				}
				vela.program(buf.toString());
			} catch (Throwable t) {
				System.out.println(t.getLocalizedMessage());
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
