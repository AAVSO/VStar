/**
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.awt.Color;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.data.validation.UncertaintyValueValidator;
//import org.aavso.tools.vstar.data.validation.ObserverCodeValidator;
import org.aavso.tools.vstar.data.validation.ValflagValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.util.Pair;

import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaValidObservationEnvironment;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.Type;
import org.aavso.tools.vstar.vela.VeLaParseError;
import org.aavso.tools.vstar.vela.VeLaEvalError;

// Author: PMAK (AAVSO) [profile: https://www.aavso.org/user/61706]

// This pligin reads text files having the following format:
//
// #NAME=(object name). May appear in any line.
// #DATE=(date column type: JD or HJD or BJD, default: JD). May appear before observations only.
// #DELIM=(delimiter, default: comma). May appear in any line (i.e. to change delimiter)
// #FILTER=(default value, if not specified in FILTER column). May appear in any line (i.e. to change filter)
// #OBSCODE=(default Observer Code value, if not specified in ObsCode column). May appear in any line (i.e. to change ObsCode)
// #MAGSHIFT=(value to be added to magnitudes, default = 0.0). May appear in any line (i.e. to change magnitude shift)
// #DATEADD=(value to be added to date values, default = 0.0). May appear in any line (i.e. to change value to be added to dates)
// #ERROR=(N to ignore all validation errors but time and mag; Y: do not ignore errors; any other values are ignored). May appear in any line.
// #ESCAPINGQUOTES=(N: use "" iside quotes; Y: use \" inside quotes; any other values are ignored). May appear in any line.
// #VELAFILTER=<VeLa expression>
// #DEFINESERIES=<new user-defined series>
// #any comment
// <JD|HJD|BJD><delim><MAG>[<delim><MAG_ERR>[<delim><ODSCODE>[<delim>[<VALIDATION_FLAG>[<delim><FILTER>[<delim><any additional fields are ignored>]]]]]]
//
// Only date and magnitude values are required, all others are optional.
// Names of directives are case insensitive, e.g. #MagShift is the same as #MAGSHIFT
// Observer Code is not validated (intentionally)
//
// You may specify an order of fields in an input file using #FIELDS directive.
// Valid field designations:
//   Time   : julian daty field, mandatory
//   Mag    : magnitude field, mandatory
//   MagErr : uncertainty field, optional
//   ObsCode: observation code field, optional
//   Flag   : validation flag field, optional
//   Filter : filter field, optional
// Any other field names are ignored, the empty name is allowed.
//
// Example1 (any extra columns will be ignored): 
//   #FIELDS=Time,Mag,MagErr,ObsCode,Flag,Filter
// Example2 (file contains Time values at the first column, 
//           Magnitude values in the second column and Filter values in the 6th column. 
//           Other columns (between Mag and Filter and any extra columns) will be ignored: 
//   #FIELDS=Time,Mag,,,,Filter
//
// If a line contains fewer fields then specified, missing colums get default values.
//
// Example:
//
// #NAME=Star Name
// #MAGSHIFT=-0.08
// #DATE=JD
// #JD,Mag,Mag Error,ObsCode,Flag,Filter(Band)
// 2458033.3141,13.308,0.040,PMAK,,V
// 2458037.2615,13.150,0.030,PMAK,,V
// 2458039.3227,13.086,0.010,PMAK,,V
// 2458043.1981,13.001,0.020,PMAK,,V
// 2458045.2762,12.959,0.030,PMAK,,V
// 2458045.2866,12.970,0.040,PMAK,,V
// 2458049.2442,12.951,0.025,PMAK,,V
// <..>
//
//
// PMAK 2019-06-23:
//   1) delimiter can be one-char only
//   2) splitWithQuotes() instead of split()
//   3) translateDelimiter: only the first character of a delimiter is used.
//
// PMAK 2019-06-25:
//   1) Quoted fields (using " as a quote char) (see splitWithQuotes())
//   3) #ESCAPINGQUOTES directive (see above)
//   2) Support for "comment", "name" standard AAVSO fields.
//   To read AAVSO download format, use 
//   #FIELDS=time,mag,magerr,,filter,obscode,,,,,comments,,,flag,,,,name
//
// PMAK 2019-07-02:
//   VStar Rev. 1630: DateInfo.setJulianDay() removed. Compatible code is used.
//
// PMAK 2020-01-08:
//   1) BJD date type added.
//   2) Date type checking softened: if #DATE is not JD|HJD|BJD, JD is assumed.
//
// PMAK 2020-02-05:
//   1) new directive #VELAFILTER.
//   2) Minor fixes.
//
// PMAK 2020-02-06:
//   1) new directive #DEFINESERIES.
//      Example (new series with name "TESS +0.73"):
// #DEFINESERIES= TESS +0.73, TESS +0.73, #660099
// #FILTER=       TESS +0.73
//
// PMAK 2020-04-25:
//   1) Series created by #DEFINESERIES is not user-defined now 
//      (so it can appear in "Descriptive Statistic by Series")
//   2) VeLa Filter code improved
//   3) addObservationWarning helper
//

/**
 * This plug-in class reads Flexible Text Format File (PMAK)
 */
public class FlexibleTextFileFormatObservationSource extends
		ObservationSourcePluginBase {

	private static final char DEFAULT_DELIMITER = ',';
	private static final char DEFAULT_QUOTEMARK = '"';

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getCurrentStarName
	 *      ()
	 */
	@Override
	public String getCurrentStarName() {
		return getInputName();
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getInputType()
	 */
	@Override
	public InputType getInputType() {
		return InputType.FILE_OR_URL;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#
	 *      getObservationRetriever ()
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new FlexibleTextFileFormatRetriever();
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Flexible Text File Format Reader";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from Flexible Text Format File...";
	}

	class FlexibleTextFileFormatRetriever extends AbstractObservationRetriever {
		private char delimiter = DEFAULT_DELIMITER;
		private String dateType = "JD";
		private String objName = "";
		private String defFilter = "";
		private String defObsCode = "";
		private String filterVeLa = "";
		private double magShift = 0.0;
		private double dateAdd = 0.0;
		private boolean ignoreValidationErrors = false;
		private boolean escapingQuotes = false;
		private int timeColumn = 0;
		private int magColumn = 1;
		private int magErrColumn = 2;
		private int obsCodeColumn = 3;
		private int flagColumn = 4;
		private int filterColumn = 5;
		private int nameColumn = -1;
		private int commentsColumn = -1;
		private List<String> lines = null;

		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;
		// private ObserverCodeValidator observerCodeValidator;
		private ValflagValidator valflagValidator;

		private VeLaInterpreter vela = null;

		/**
		 * Constructor
		 */
		public FlexibleTextFileFormatRetriever() {
			super(getVelaFilterStr());
			julianDayValidator = new JulianDayValidator();
			magnitudeFieldValidator = new MagnitudeFieldValidator();
			uncertaintyValueValidator = new UncertaintyValueValidator(
					new InclusiveRangePredicate(0, 1));
			// observerCodeValidator = new ObserverCodeValidator();
			valflagValidator = new ValflagValidator("G|D|T|P|V|Z");
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			try {
				int lineNum = 0;
				int readErrorCount = 0;
				boolean terminateReading = false;

				for (String line : lines) {
					lineNum++;
					try {
						if (line != null) {
							line = line.replaceFirst("\n", "").replaceFirst(
									"\r", "");
							if (!isEmpty(line)) {
								if (line.startsWith("#")) {
									String errorText = handleDirective(line);
									if (errorText != null) {
										terminateReading = true;
										throw new ObservationValidationError(
												"Reading terminated due to fatal error: "
														+ errorText);
									}
								} else {
									// String[] fields =
									// line.split(String.valueOf(delimiter));
									String[] fields = splitWithQuotes(line,
											delimiter, DEFAULT_QUOTEMARK,
											escapingQuotes);
									ValidObservation ob = readNextObservation(
											fields, lineNum);
									if (ob != null)
										collectObservation(ob);
								}
							}
						}
						incrementProgress();
					} catch (ObservationValidationError e) {
						readErrorCount++;

						String error = e.getLocalizedMessage();
						if (error == null || isEmpty(error))
							error = e.toString();
						InvalidObservation invalidOb = new InvalidObservation(
								line, error);
						invalidOb.setRecordNumber(lineNum);
						addInvalidObservation(invalidOb);

						if (terminateReading)
							break;

						incrementProgress();
					} catch (ObservationValidationWarning e) {
						readErrorCount++;

						String error = e.getLocalizedMessage();
						if (error == null || isEmpty(error))
							error = e.toString();
						InvalidObservation invalidOb = new InvalidObservation(
								line, error, true);
						invalidOb.setRecordNumber(lineNum);
						addInvalidObservation(invalidOb);

						ValidObservation ob = e.getObservation();
						if (ob != null) { // should never be null.
							ob.setRecordNumber(lineNum);
							collectObservation(ob);
						}

						incrementProgress();
					}
				}

				// Give the user a chance to see what went wrong
				// even if no valid records read.
				if (validObservations.isEmpty()) {
					if (!invalidObservations.isEmpty()) {
						InvalidObservation ob = invalidObservations.get(0);
						throw new ObservationReadError(
								"No observations read.\n"
										+ "The first read error: "
										+ ob.getError());
					}
				}

				// Notify the user if there were errors.
				if (readErrorCount > 0) {
					MessageBox.showWarningDialog(getDescription(),
							"There are errors.\n"
									+ "See 'Observations' pane for details.");
				}

			} catch (Exception e) {
				throw new ObservationReadError(
						"Error while reading observation source.\n"
								+ e.toString());
			}
		}

		@Override
		public Integer getNumberOfRecords() throws ObservationReadError {
			if (lines == null) {
				try {
					readLines();
				} catch (IOException e) {
					throw new ObservationReadError("Error reading lines");
				}
			}
			return lines.size();
		}

		// Read all lines from the source.
		private void readLines() throws IOException {
			lines = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getInputStreams().get(0)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		private String[] splitDirective(String line) {
			if (line == null)
				return null;
			int eqPos = line.indexOf('=');
			if (eqPos < 0)
				return null;
			String s1 = line.substring(0, eqPos);
			String s2 = line.substring(eqPos + 1);
			return new String[] { s1, s2 };
		}

		// If a line starts with #, it's either a directive or a comment.
		// Returns null on success or ERROR_TEXT on error.
		private String handleDirective(String line)
				throws ObservationValidationError {
			// Do not use split: there can be '=' in the right part.
			// String[] pair = line.split("="); // do not uppercase!
			String[] pair = splitDirective(line);

			// If a name-value pair, process as a directive, otherwise assume a
			// comment and ignore.
			if (pair != null && pair.length == 2) {
				pair[0] = pair[0].trim().toUpperCase();
				pair[1] = pair[1].trim();

				// System.out.println(line);
				// System.out.println(pair[0]);
				// System.out.println(pair[1]);

				if ("#DELIM".equals(pair[0])) {
					Pair<String, String> translated_delim = translateDelimiter(pair[1]);
					if (translated_delim.first == null) {
						return translated_delim.second; // error text
					} else {
						delimiter = translated_delim.first.charAt(0);
					}

				} else if ("#DATE".equals(pair[0])) {
					if (!validObservations.isEmpty()) {
						return "#DATE directive must be specified before observations!";
					}
					dateType = pair[1].toUpperCase();
					setBarycentric(false);
					setHeliocentric(false);
					if ("HJD".equals(dateType)) {
						setHeliocentric(true);
					} else if ("BJD".equals(dateType)) {
						setBarycentric(true);
					}
				} else if ("#NAME".equals(pair[0])) {
					objName = pair[1];
				} else if ("#FILTER".equals(pair[0])) {
					defFilter = pair[1];
				} else if ("#OBSCODE".equals(pair[0])) {
					defObsCode = pair[1];
				} else if ("#VELAFILTER".equals(pair[0])) {
					filterVeLa = pair[1];
				} else if ("#DEFINESERIES".equals(pair[0])) {
					String newSeries = pair[1];
					if (!isEmpty(newSeries)) {
						String[] items = newSeries.split(",");
						if (items.length > 2 && items[0].trim().length() > 0
								&& items[1].trim().length() > 0
								&& items[2].trim().length() > 0) {
							try {
								// System.out.println("#DEFINESERIES=" +
								// newSeries);
								// System.out.println("items[0] = " + items[0]);
								// System.out.println("items[1] = " + items[1]);
								// System.out.println("items[2] = " + items[2]);
								// System.out.println("Color    = " +
								// Color.decode(items[2].trim()).toString());
								// New user-defined series
								SeriesType.create(items[0].trim(),
										items[1].trim(),
										Color.decode(items[2].trim()), false,
										false);
							} catch (Exception e) {
								// System.out.println(e.toString());
								// do not return error! Instead, throw
								// ObservationValidationError.
								// return "#DEFINESERIES directive is invalid!";
								throw new ObservationValidationError(
										"#DEFINESERIES directive is invalid: "
												+ e.toString());
							}
						} else {
							throw new ObservationValidationError(
									"#DEFINESERIES directive is invalid!");
						}
					}
				} else if ("#MAGSHIFT".equals(pair[0])) {
					double d = 0;
					try {
						d = Double.parseDouble(pair[1]);
					} catch (NumberFormatException e) {
						return "#MAGSHIFT directive contains invalid floating-point value!";
					}
					magShift = d;
				} else if ("#DATEADD".equals(pair[0])) {
					double d = 0;
					try {
						d = Double.parseDouble(pair[1]);
					} catch (NumberFormatException e) {
						return "#DATEADD directive contains invalid floating-point value!";
					}
					dateAdd = d;
				} else if ("#ERROR".equals(pair[0])) {
					if ("N".equalsIgnoreCase(pair[1]))
						ignoreValidationErrors = true;
					else if ("Y".equalsIgnoreCase(pair[1]))
						ignoreValidationErrors = false;
					else
						;
				} else if ("#ESCAPINGQUOTES".equals(pair[0])) {
					if ("N".equalsIgnoreCase(pair[1]))
						escapingQuotes = false;
					else if ("Y".equalsIgnoreCase(pair[1]))
						escapingQuotes = true;
					else
						;
				} else if ("#FIELDS".equals(pair[0])) {
					initFieldMap(pair[1].toUpperCase());
					if (timeColumn < 0 || magColumn < 0)
						return "#FIELDS directive must specify at least Time and Mag fields!";
				}

			}
			return null;
		}

		private void initFieldMap(String dataFieldsString) {
			timeColumn = -1;
			magColumn = -1;
			magErrColumn = -1;
			obsCodeColumn = -1;
			flagColumn = -1;
			filterColumn = -1;
			nameColumn = -1;
			commentsColumn = -1;
			String[] dataFields = dataFieldsString.split(",");
			for (int fieldNum = 0; fieldNum < dataFields.length; fieldNum++) {
				String field = dataFields[fieldNum].trim();
				if ("TIME".equals(field) && timeColumn < 0)
					timeColumn = fieldNum;
				else if ("MAG".equals(field) && magColumn < 0)
					magColumn = fieldNum;
				else if ("MAGERR".equals(field) && magErrColumn < 0)
					magErrColumn = fieldNum;
				else if ("OBSCODE".equals(field) && obsCodeColumn < 0)
					obsCodeColumn = fieldNum;
				else if ("FLAG".equals(field) && flagColumn < 0)
					flagColumn = fieldNum;
				else if ("FILTER".equals(field) && filterColumn < 0)
					filterColumn = fieldNum;
				else if ("NAME".equals(field) && nameColumn < 0)
					nameColumn = fieldNum;
				else if ("COMMENTS".equals(field) && commentsColumn < 0)
					commentsColumn = fieldNum;
			}
		}

		// Translate the delimiter.
		// Returns <delim, null> on success or <null, "ERROR_TEXT"> on error.
		private Pair<String, String> translateDelimiter(String delim) {
			if (delim == null || delim == "")
				return new Pair<String, String>(
						String.valueOf(DEFAULT_DELIMITER), null);

			char delimChar;
			if ("tab".equalsIgnoreCase(delim)) {
				delimChar = '\t';
			} else if ("comma".equalsIgnoreCase(delim)) {
				delimChar = ',';
			} else if ("space".equalsIgnoreCase(delim)) {
				delimChar = ' ';
			} else {
				int ordVal;
				try {
					ordVal = Integer.parseInt(delim);
				} catch (NumberFormatException e) {
					// returns the first char of the delimiter
					delimChar = delim.charAt(0);
					return new Pair<String, String>(String.valueOf(delimChar),
							null);
				}
				if (ordVal < 32 || ordVal > 126) {
					return new Pair<String, String>(
							null,
							String.format(
									"Ordinal delimiter value '%d' out of range 32..126",
									ordVal));
				}
				delimChar = (char) ordVal;
			}
			return new Pair<String, String>(String.valueOf(delimChar), null);
		}

		// Read the next observation.
		private ValidObservation readNextObservation(String[] fields,
				int lineNum) throws ObservationValidationError,
				ObservationValidationWarning {

			String observationWarnings = null;

			if (timeColumn < 0 || magColumn < 0 || fields.length <= timeColumn
					|| fields.length <= magColumn) {
				throw new ObservationValidationError(
						"At least two fields expected: Time and Magnitude");
			}

			ValidObservation observation = new ValidObservation();

			// observation.setName(getStarInfo().getDesignation());

			if (!"JD".equals(dateType) && !"HJD".equals(dateType)
					&& !"BJD".equals(dateType)) {
				observationWarnings = addObservationWarning(
						observationWarnings, "Unsupported date type: "
								+ dateType);
			}
			DateInfo dateInfo = julianDayValidator.validate(fields[timeColumn]
					.trim());
			// dateInfo.setJulianDay(dateInfo.getJulianDay() + dateAdd);
			// observation.setDateInfo(dateInfo);
			// Rev. 1630: DateInfo.setJulianDay() removed. Compatible code:
			observation.setDateInfo(new DateInfo(dateInfo.getJulianDay()
					+ dateAdd));

			Magnitude magnitude = magnitudeFieldValidator
					.validate(fields[magColumn].trim());
			if (magnitude.isBrighterThan()) {
				throw new ObservationValidationError(
						"Was '>' intended (brighter than) or '<'?"); // See
																		// CommonTextFormatValidator.java
			}
			magnitude.setMagValue(magnitude.getMagValue() + magShift);

			if (magErrColumn >= 0 && fields.length > magErrColumn) {
				String uncertaintyStr = fields[magErrColumn].trim();
				if (!isEmpty(uncertaintyStr)) {
					try {
						double uncertainty = uncertaintyValueValidator
								.validate(uncertaintyStr);
						magnitude.setUncertainty(uncertainty);
					} catch (ObservationValidationError e) {
						if (!ignoreValidationErrors) {
							throw e;
						} else {
							String error = e.getMessage();
							if (error == null || isEmpty(error))
								error = e.toString();
							observationWarnings = addObservationWarning(
									observationWarnings, error);
						}
					}
				}
			}

			// observation.setMagnitude must be set AFTER assignment of
			// uncertainty (because of caching!)
			observation.setMagnitude(magnitude);
			observation.setRecordNumber(lineNum);

			String obscode = defObsCode;
			if (obsCodeColumn >= 0 && fields.length > obsCodeColumn) {
				obscode = fields[obsCodeColumn].trim();
				if (isEmpty(obscode)) {
					obscode = defObsCode;
				}
			}
			if (!isEmpty(obscode)) {
				// observation.setObsCode(observerCodeValidator.validate(obscode));
				observation.setObsCode(obscode);
			}

			if (flagColumn >= 0 && fields.length > flagColumn) {
				String valflag = fields[flagColumn].trim();
				if (!isEmpty(valflag)) {
					try {
						ValidationType validationType = valflagValidator
								.validate(valflag);
						observation.setValidationType(validationType);
					} catch (ObservationValidationError e) {
						if (!ignoreValidationErrors) {
							throw e;
						} else {
							String error = e.getMessage();
							if (error == null || isEmpty(error))
								error = e.toString();
							observationWarnings = addObservationWarning(
									observationWarnings, error);
						}
					}
				}
			}

			SeriesType band = SeriesType.Unspecified;
			String filter = "";
			if (filterColumn >= 0 && fields.length > filterColumn) {
				filter = fields[filterColumn].trim();
			}
			if (isEmpty(filter)) {
				filter = defFilter;
			}
			if (!isEmpty(filter)) {
				band = SeriesType.getSeriesFromShortName(filter);
			}
			observation.setBand(band);

			String name = "";
			if (nameColumn >= 0 && fields.length > nameColumn) {
				name = fields[nameColumn].trim();
			}
			if (isEmpty(name)) {
				name = getStarInfo().getDesignation();
			}
			observation.setName(name);

			if (commentsColumn >= 0 && fields.length > commentsColumn) {
				String comments = fields[commentsColumn].trim();
				observation.setComments(comments);
			}

			if (filterVeLa != null && !isEmpty(filterVeLa)) {
				if (vela == null)
					vela = new VeLaInterpreter();

				boolean includeObservation = true;
				vela.pushEnvironment(new VeLaValidObservationEnvironment(
						observation));
				try {
					Optional<Operand> result = vela.program(filterVeLa);
					if (result.isPresent()) {
						if (result.get().getType() == Type.BOOLEAN) {
							includeObservation = result.get().booleanVal();
						} else {
							observationWarnings = addObservationWarning(
									observationWarnings,
									"VeLa filter error: Expected a Boolean value");
						}
					}
				} catch (VeLaParseError | VeLaEvalError e) {
					String error = e.getMessage();
					if (error == null || isEmpty(error))
						error = e.toString();
					observationWarnings = addObservationWarning(
							observationWarnings, "VeLa filter error: " + error);
				} finally {
					vela.popEnvironment();
				}

				if (!includeObservation)
					observation = null;

			}

			if (observationWarnings != null) {
				throw new ObservationValidationWarning("Warning: "
						+ observationWarnings, observation);
			}

			return observation;
		}

		private String addObservationWarning(String warnings, String warning) {
			if (warnings != null)
				warnings += "; ";
			else
				warnings = "";
			warnings += warning;
			return warnings;
		}

		/**
		 * 
		 * @param s
		 *            input string
		 * @param delim
		 *            delimiter (char)
		 * @param quote
		 *            quote (char)
		 * @param escapingQuote
		 *            if true, use
		 *            \" for quotes inside quoted field, otherwise use ""
		 * @return the array of strings computed by splitting the input
		 * 
		 */
		private String[] splitWithQuotes(String s, char delim, char quote,
				boolean escapingQuote) {
			List<String> fields = new ArrayList<String>();
			char esc = '\\';
			boolean inQuotes = false;
			int len = s.length();
			int pos = 0;
			StringBuffer field = new StringBuffer();
			if (pos < len && s.charAt(pos) == quote) {
				inQuotes = true;
				pos++;
			}
			while (pos < len) {
				if (inQuotes) {
					if (pos == len - 1) {
						if (s.charAt(pos) == quote) {
							pos++;
							inQuotes = false;
						} else {
							field.append(s.charAt(pos));
							pos++;
						}
					} else {
						if (s.charAt(pos) == (escapingQuote ? esc : quote)
								&& s.charAt(pos + 1) == quote) {
							field.append(quote);
							pos++;
							pos++;
						} else if (s.charAt(pos) == quote) {
							pos++;
							inQuotes = false;
						} else {
							field.append(s.charAt(pos));
							pos++;
						}
					}
				} else {
					if (s.charAt(pos) == delim) {
						fields.add(field.toString());
						field.delete(0, field.length());
						pos++;
						if (pos < len && s.charAt(pos) == quote) {
							inQuotes = true;
							pos++;
						}
					} else {
						field.append(s.charAt(pos));
						pos++;
					}
				}
			}
			fields.add(field.toString());

			return fields.toArray(new String[fields.size()]);
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "Flexible Text Format File V1.0";
		}

		@Override
		public StarInfo getStarInfo() {

			String name = objName;

			if (name == null || isEmpty(name)) {
				name = getSourceName();
			}

			return new StarInfo(this, name);
		}

		private boolean isEmpty(String str) {
			return str != null && "".equals(str.trim());
		}

	}
}