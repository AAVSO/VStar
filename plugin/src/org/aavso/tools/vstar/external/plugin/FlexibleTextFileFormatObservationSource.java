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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.awt.Color;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.data.validation.UncertaintyValueValidator;
import org.aavso.tools.vstar.data.validation.ValflagValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.Pair;

import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaValidObservationEnvironment;
import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.Type;
import org.aavso.tools.vstar.vela.VeLaParseError;
import org.aavso.tools.vstar.vela.VeLaEvalError;

// Author: PMAK (AAVSO) [profile: https://www.aavso.org/user/61706]

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
// PMAK 2024-04-19:
//   1) If there is no #FIELDS directive, only Time and Mag columns are recognized.
//   2) Synonyms: 
//        "MAG" == "MAGNITUDE"
//        "MAGERR" == "UNCERTAINTY"
//        "FILTER" == "BAND"
//        "COMMENTS" == "NOTES"
//        "FLAG" == "VALIDATION"
//   3) #DELIM can be tab, comma, space, multispace, 
//      or a single ASCII character with an ordinal value between 32 and 126 
//

/**
 * This plug-in class reads Flexible Text Format File (PMAK)
 */
public class FlexibleTextFileFormatObservationSource extends
		ObservationSourcePluginBase {

	private static final String FIELD_ERROR = " field specified more than once";
	
	private static final char DEFAULT_DELIMITER = ',';
	private static final char DEFAULT_QUOTEMARK = '"';
	
	public enum KnownFields {
		TIME, MAG, MAGERR, OBSCODE, FLAG, FILTER, NAME, COMMENTS;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#getCurrentStarName
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
		return "Flexible Text File Format Reader v1.2";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from Flexible Text Format v1.2 File...";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "FlexibleTextFileFormat Plug-In.pdf";
	}

	class FlexibleTextFileFormatRetriever extends AbstractObservationRetriever {
		private char delimiter = DEFAULT_DELIMITER;
		private boolean multispaceDelimiter = false;
		private String objName = "";
		private String defFilter = "";
		private String defObsCode = "";
		private String filterVeLa = "";
		private String titleX = "";
		private String titleY = "";
		private double magShift = 0.0;
		private double dateAdd = 0.0;
		private boolean ignoreValidationErrors = false;
		private boolean escapingQuotes = false;
		private List<String> lines = null;
		
		private Hashtable<KnownFields, Integer> fieldMap; 

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
			uncertaintyValueValidator = new UncertaintyValueValidator(new InclusiveRangePredicate(0, 1));
			// observerCodeValidator = new ObserverCodeValidator();
			valflagValidator = new ValflagValidator("G|D|T|P|U|V|Z");

			fieldMap = new Hashtable<KnownFields, Integer>();
			for (KnownFields f : KnownFields.values())
				fieldMap.put(f, -1);
			// Minimum required set of fields
			initFieldMap("TIME,MAGNITUDE");
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			try {
				int lineNum = 0;
				boolean terminateReading = false;

				for (String line : lines) {
					if (wasInterrupted())
						break;
					lineNum++;
					try {
						if (line == null) continue;
						line = line.replaceFirst("\n", "").replaceFirst("\r", "");
						if (isNullOrEmpty(line)) continue;
						if (line.startsWith("#")) {
							Pair<Boolean, String> errorState = handleDirective(line);
							if (errorState != null) {
								if (errorState.first) {
									terminateReading = true;
									throw new ObservationValidationError(
											"\nLine: " + Integer.toString(lineNum) + "\n" +
											"Reading terminated due to error:\n" + 
											errorState.second);
								} else {
									throw new ObservationValidationError(errorState.second);
								}
							}
						} else {
							String[] fields = splitWithQuotes(line,	delimiter, multispaceDelimiter,	DEFAULT_QUOTEMARK, escapingQuotes);
							ValidObservation ob = readNextObservation(fields);
							if (ob != null) {
								ob.setRecordNumber(lineNum);
								collectObservation(ob);
							}
						}
						
						incrementProgress();
					} catch (ObservationValidationError e) {
						String error = e.getMessage();
						if (isNullOrEmpty(error))
							error = e.toString();
						InvalidObservation invalidOb = new InvalidObservation(line, error);
						invalidOb.setRecordNumber(lineNum);
						addInvalidObservation(invalidOb);

						if (terminateReading)
							break;

						incrementProgress();
					} catch (ObservationValidationWarning e) {

						String error = e.getMessage();
						if (isNullOrEmpty(error))
							error = e.toString();
						InvalidObservation invalidOb = new InvalidObservation(line, error, true);
						invalidOb.setRecordNumber(lineNum);
						addInvalidObservation(invalidOb);

						ValidObservation ob = e.getObservation();
						if (ob != null) { // can be null because of VeLa filter
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
						InvalidObservation ob = invalidObservations.get(invalidObservations.size() - 1);
						throw new ObservationReadError("No observations read.\nThe first fatal error: " + ob.getError());
					}
				}
			} catch (Exception e) {
				throw new ObservationReadError("Error while reading observation source.\n"	+ e.toString());
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStreams().get(0)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		private Pair<String, String> splitDirective(String line) {
			if (line == null || line.length() < 2)
				return null;
			if (line.charAt(1) == '#' || line.charAt(1) == ' ')
				return null;
			int eqPos = line.indexOf('=');
			if (eqPos < 0)
				return null;
			String s1 = line.substring(0, eqPos);
			String s2 = line.substring(eqPos + 1);
			return new Pair<String, String>(s1, s2);
		}

		// If a line starts with #, it's either a directive or a comment.
		// Returns null on success or ERROR_TEXT
		private Pair<Boolean, String> handleDirective(String line) {
			Pair<Boolean, String> result = new Pair<Boolean, String>(true, null); 
			// Do not use split: there can be '=' in the right part.
			Pair<String, String> pair = splitDirective(line);
			// If a name-value pair, process as a directive, otherwise assume
			// it is a comment and ignore.
			if (pair != null) {
				pair.first = pair.first.trim().toUpperCase();
				pair.second = pair.second.trim();
				if ("#DELIM".equals(pair.first)) {
					Pair<String, String> translated_delim = translateDelimiter(pair.second);
					if (translated_delim.first == null) {
						result.second = translated_delim.second; // error text
						return result;
					} else {
						// test for 'multispace' delimiter (special case)
						if ("  ".equals(translated_delim.first)) {
							delimiter = ' ';
							multispaceDelimiter = true;
						} else {
							delimiter = translated_delim.first.charAt(0);
							multispaceDelimiter = false;
						}
					}
				} else if ("#DATE".equals(pair.first)) {
					if (!validObservations.isEmpty()) {
						result.second =  "#DATE directive must be specified before observations!";
						return result;
					}
					String dateType = pair.second.toUpperCase();
					if ("JD".equals(dateType)) {
						setJDflavour(JDflavour.JD);
					} else if ("HJD".equals(dateType)) {
						setJDflavour(JDflavour.HJD);
					} else if ("BJD".equals(dateType)) {
						setJDflavour(JDflavour.BJD);
					} else {
						setJDflavour(JDflavour.JD);
						result.first = false; // not a critical error
						result.second = "Unsupported date type: " + dateType + "; JD is assumed.";
						return result;
					}
				} else if ("#NAME".equals(pair.first)) {
					objName = pair.second;
				} else if ("#FILTER".equals(pair.first)) {
					defFilter = pair.second;
				} else if ("#BAND".equals(pair.first)) { // the same as #FILTER
					defFilter = pair.second;
				} else if ("#OBSCODE".equals(pair.first)) {
					defObsCode = pair.second;
				} else if ("#VELAFILTER".equals(pair.first)) {
					filterVeLa = pair.second;
				} else if ("#DEFINESERIES".equals(pair.first)) {
					String newSeries = pair.second;
					if (!isNullOrEmpty(newSeries)) {
						String[] items = newSeries.split(",");
						if (items.length > 2 && items[0].trim().length() > 0
								&& items[1].trim().length() > 0
								&& items[2].trim().length() > 0) {
							try {
								// New user-defined series
								Color color = Color.decode(items[2].trim());
								SeriesType.create(items[0].trim(), items[1].trim(), color, false, false);
							} catch (Exception e) {
								result.first = false; // not a critical error
								result.second = "#DEFINESERIES directive is invalid!";
								return result;
							}
						} else {
							result.first = false; // not a critical error
							result.second = "#DEFINESERIES directive is invalid!";
							return result;
						}
					}
				} else if ("#MAGSHIFT".equals(pair.first)) {
					double d = 0;
					try {
						d = Double.parseDouble(pair.second);
					} catch (NumberFormatException e) {
						result.second = "#MAGSHIFT directive contains invalid floating-point value!";
						return result;
					}
					magShift = d;
				} else if ("#DATEADD".equals(pair.first)) {
					double d = 0;
					try {
						d = Double.parseDouble(pair.second);
					} catch (NumberFormatException e) {
						result.second = "#DATEADD directive contains invalid floating-point value!";
						return result;
					}
					dateAdd = d;
				} else if ("#ERROR".equals(pair.first)) {
					if ("N".equalsIgnoreCase(pair.second))
						ignoreValidationErrors = true;
					else if ("Y".equalsIgnoreCase(pair.second))
						ignoreValidationErrors = false;
					else {
						result.second = "#ERROR directive is invalid";
						return result;
					}
				} else if ("#ESCAPINGQUOTES".equals(pair.first)) {
					if ("N".equalsIgnoreCase(pair.second))
						escapingQuotes = false;
					else if ("Y".equalsIgnoreCase(pair.second))
						escapingQuotes = true;
					else {
						result.second = "#ESCAPINGQUOTES directive is invalid";
						return result;
					}
				} else if ("#FIELDS".equals(pair.first)) {
					Pair<Boolean, String> errorState = initFieldMap(pair.second.toUpperCase());
					if (errorState != null) {
						if (errorState.first) {
							result.second = errorState.second;
							return result;
						}
					}
					if (fieldMap.get(KnownFields.TIME) < 0 || fieldMap.get(KnownFields.MAG) < 0) {
						result.second = "#FIELDS directive must specify at least Time and Mag fields!";
						return result;
					}
					if (errorState != null) {
						result.first = false; // not a critical error
						result.second = errorState.second;
						return result;
					}
				} else if ("#TITLEX".equals(pair.first)) {
					titleX = pair.second;
				} else if ("#TITLEY".equals(pair.first)) {
					titleY = pair.second;
				}
			}
			return null;
		}

		private boolean setFieldMapValue(KnownFields key, int value) {
			if (fieldMap.get(key) < 0) {
				fieldMap.replace(key, value);
				return true;
			} else {
				return false;
			}
		}
		
		private Pair<Boolean, String> initFieldMap(String dataFieldsString) {
			Pair<Boolean, String>result = new Pair<Boolean, String>(true, null); 
			for (Map.Entry<KnownFields, Integer> entry : fieldMap.entrySet()) {
				fieldMap.replace(entry.getKey(), -1);
			}
			String unsupportedFields = null;
			String[] dataFields = dataFieldsString.split(",");
			for (int fieldNum = 0; fieldNum < dataFields.length; fieldNum++) {
				String field = dataFields[fieldNum].trim();
				if ("TIME".equals(field)) {
					if (!setFieldMapValue(KnownFields.TIME, fieldNum)) {
						result.second = "TIME" + FIELD_ERROR;
						return result;
					}
				} else if ("MAG".equals(field) || "MAGNITUDE".equals(field))
				{
					if (!setFieldMapValue(KnownFields.MAG, fieldNum)) {
						result.second = "MAG/MAGNITUDE" + FIELD_ERROR;
						return result;
					}
				} else if ("MAGERR".equals(field) || "UNCERTAINTY".equals(field)) {
					if (!setFieldMapValue(KnownFields.MAGERR, fieldNum)) {
						result.second = "MAGERR/UNCERTAINTY" + FIELD_ERROR;
						return result;
					}
				} else if ("OBSCODE".equals(field)) {
					if (!setFieldMapValue(KnownFields.OBSCODE, fieldNum)) {
						result.second = "OBSCODE" + FIELD_ERROR;
						return result;
					}
				} else if ("FLAG".equals(field) || "VALIDATION".equals(field)) {
					if (!setFieldMapValue(KnownFields.FLAG, fieldNum)) {
						result.second = "FLAG/VALIDATION" + FIELD_ERROR;
						return result;
					}
				} else if ("FILTER".equals(field) || "BAND".equals(field)) {
					if (!setFieldMapValue(KnownFields.FILTER, fieldNum)) {
						result.second = "FILTER/BAND" + FIELD_ERROR;
						return result;
					}
				} else if ("NAME".equals(field)) {
					if (!setFieldMapValue(KnownFields.NAME, fieldNum)) {
						result.second = "NAME" + FIELD_ERROR;
						return result;
					}
				} else if ("COMMENTS".equals(field) || "NOTES".equals(field)) {
					if (!setFieldMapValue(KnownFields.COMMENTS, fieldNum)) {
						result.second = "COMMENTS/NOTES" + FIELD_ERROR;
						return result;
					}
				} else if ("".equals(field) || "*".equals(field)) {
					// ignore
				} else {
					if (unsupportedFields != null)
						unsupportedFields = unsupportedFields + ",";
					else
						unsupportedFields = "";
					unsupportedFields = unsupportedFields + field;
				}
			}
			if (unsupportedFields != null) {
				result.first = false; // not a critical error
				result.second = "These fields are not supported yet and will be ignored: " + unsupportedFields;
				return result;
			} else
				return null;
		}

		// Translate the delimiter.
		// Returns <delim, null> on success or <null, "ERROR_TEXT"> on error.
		private Pair<String, String> translateDelimiter(String delim) {
			if (isNullOrEmpty(delim))
				return new Pair<String, String>(String.valueOf(DEFAULT_DELIMITER), null);

			char delimChar;
			boolean multispace = false;
		    if ("tab".equalsIgnoreCase(delim)) {
				delimChar = '\t';
			} else if ("comma".equalsIgnoreCase(delim)) {
				delimChar = ',';
			} else if ("space".equalsIgnoreCase(delim)) {
				delimChar = ' ';
			} else if ("multispace".equalsIgnoreCase(delim)) {
				delimChar = ' ';
				multispace = true;
			} else {
				if (delim.length() != 1) {
					return new Pair<String, String>(null, 
							"Invalid delimiter specification. " + 
							"Allowed values: tab, comma, space, multispace, " + 
							"or a single ASCII character with an ordinal value between 32 and 126");
				}
				delimChar = delim.charAt(0);
				if (delimChar == '"') {
					return new Pair<String, String>(null, 
							"Invalid delimiter specification: \" is not allowed");
				}
				if ((int)delimChar < 32 || (int)delimChar > 126) {
					return new Pair<String, String>(null, 
							"Invalid delimiter specification. " + 
							"Allowed values: tab, comma, space, multispace, " + 
							"or a single ASCII character with an ordinal value between 32 and 126");
				}
				return new Pair<String, String>(String.valueOf(delimChar), null);
			}
			String delimCharAsString;
			if (!multispace) {
				delimCharAsString = String.valueOf(delimChar);
			} else {
				// special case:
				// two-space delimiter means 'multispace delimiter' 
				delimCharAsString = "  ";
			}
			return new Pair<String, String>(delimCharAsString, null);
		}

		// Read the next observation.
		private ValidObservation readNextObservation(String[] fields) 
				throws ObservationValidationError, ObservationValidationWarning {

			String observationWarnings = null;

			int timeColumn = fieldMap.get(KnownFields.TIME);
			int magColumn = fieldMap.get(KnownFields.MAG);
			if (timeColumn < 0 || magColumn < 0 || 
					fields.length <= timeColumn	|| fields.length <= magColumn) {
				throw new ObservationValidationError(
						"At least two fields expected: Time and Magnitude");
			}

			ValidObservation observation = new ValidObservation();

			DateInfo dateInfo = julianDayValidator.validate(fields[timeColumn].trim());
			// dateInfo.setJulianDay(dateInfo.getJulianDay() + dateAdd);
			// observation.setDateInfo(dateInfo);
			// Rev. 1630: DateInfo.setJulianDay() removed. Compatible code:
			observation.setDateInfo(new DateInfo(dateInfo.getJulianDay() + dateAdd));

			Magnitude magnitude = magnitudeFieldValidator.validate(fields[magColumn].trim());
			if (magnitude.isBrighterThan()) {
				String error = "Was '>' intended (brighter than) or '<'?";
				if (!ignoreValidationErrors) {
					throw new ObservationValidationError(error); // See CommonTextFormatValidator.java
				} else {
					observationWarnings = addObservationWarning(observationWarnings, error);
				}
			}
			magnitude.setMagValue(magnitude.getMagValue() + magShift);

			int magErrColumn = fieldMap.get(KnownFields.MAGERR);
			if (magErrColumn >= 0 && fields.length > magErrColumn) {
				String uncertaintyStr = fields[magErrColumn].trim();
				if (!isNullOrEmpty(uncertaintyStr)) {
					try {
						double uncertainty = uncertaintyValueValidator.validate(uncertaintyStr);
						magnitude.setUncertainty(uncertainty);
					} catch (ObservationValidationError e) {
						if (!ignoreValidationErrors) {
							throw e;
						} else {
							String error = e.getMessage();
							if (isNullOrEmpty(error))
								error = e.toString();
							observationWarnings = addObservationWarning(observationWarnings, error);
						}
					}
				}
			}

			// observation.setMagnitude must be set AFTER assignment of the uncertainty
			observation.setMagnitude(magnitude);

			String obscode = defObsCode;
			int obsCodeColumn = fieldMap.get(KnownFields.OBSCODE);
			if (obsCodeColumn >= 0 && fields.length > obsCodeColumn) {
				obscode = fields[obsCodeColumn].trim();
				if (isNullOrEmpty(obscode)) {
					obscode = defObsCode;
				}
			}
			if (!isNullOrEmpty(obscode)) {
				// observation.setObsCode(observerCodeValidator.validate(obscode));
				observation.setObsCode(obscode);
			}

			int flagColumn = fieldMap.get(KnownFields.FLAG);
			if (flagColumn >= 0 && fields.length > flagColumn) {
				String valflag = fields[flagColumn].trim();
				if (!isNullOrEmpty(valflag)) {
					try {
						ValidationType validationType = valflagValidator.validate(valflag);
						observation.setValidationType(validationType);
					} catch (ObservationValidationError e) {
						if (!ignoreValidationErrors) {
							throw e;
						} else {
							observation.setValidationType(null);
							String error = e.getMessage();
							if (isNullOrEmpty(error))
								error = e.toString();
							observationWarnings = addObservationWarning(observationWarnings, error);
						}
					}
					ValidationType vt = observation.getValidationType();
					if (vt != null)
						observation.addDetail("Validation", vt.toString(), "Validation");
					else
						observation.addDetail("Validation", "?", "Validation");
				}
			}

			SeriesType band = SeriesType.Unspecified;
			String filter = "";
			int filterColumn = fieldMap.get(KnownFields.FILTER);
			if (filterColumn >= 0 && fields.length > filterColumn) {
				filter = fields[filterColumn].trim();
			}
			if (isNullOrEmpty(filter)) {
				filter = defFilter;
			}
			if (!isNullOrEmpty(filter)) {
				band = SeriesType.getSeriesFromShortName(filter);
			}
			observation.setBand(band);

			String name = "";
			int nameColumn = fieldMap.get(KnownFields.NAME);
			if (nameColumn >= 0 && fields.length > nameColumn) {
				name = fields[nameColumn].trim();
			}
			if (isNullOrEmpty(name)) {
				name = getStarInfo().getDesignation();
			} else {
				if (isNullOrEmpty(objName)) {
					objName = name;
				}
			}
			observation.setName(name);

			int commentsColumn = fieldMap.get(KnownFields.COMMENTS);
			if (commentsColumn >= 0 && fields.length > commentsColumn) {
				String comments = fields[commentsColumn].trim();
				observation.setComments(comments);
			}

			if (!isNullOrEmpty(filterVeLa)) {
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
							observationWarnings = addObservationWarning(observationWarnings, "VeLa filter error: Expected a Boolean value");
						}
					}
				} catch (VeLaParseError | VeLaEvalError e) {
					String error = e.getMessage();
					if (isNullOrEmpty(error))
						error = e.toString();
					observationWarnings = addObservationWarning(observationWarnings, "VeLa filter error: " + error);
				} finally {
					vela.popEnvironment();
				}

				if (!includeObservation)
					observation = null;

			}

			if (observationWarnings != null) {
				throw new ObservationValidationWarning("Warning: " + observationWarnings, observation);
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
		 * @param multispaceDelimiter
		 *           delimiter is multi-space (boolean):
		 *           any number of repeating spaces are
		 *           interpreted as a single delimiter 
		 * @param quote
		 *            quote (char)
		 * @param escapingQuote
		 *            if true, use
		 *            \" for quotes inside quoted field, otherwise use ""
		 * @return the array of strings computed by splitting the input
		 * 
		 */
		private String[] splitWithQuotes(String s, char delim, boolean multispaceDelimiter,
				char quote, boolean escapingQuote) {
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
						if (multispaceDelimiter) {
							while (pos < len && s.charAt(pos) == delim) {
								pos++;
							}
						}
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
			return "Flexible Text Format File v1.2";
		}

		@Override
		public StarInfo getStarInfo() {

			String name = objName;

			if (isNullOrEmpty(name)) {
				name = getSourceName();
			}

			return new StarInfo(this, name);
		}
		
		@Override
		public String getDomainTitle() {
			return titleX;
		}

		@Override
		public String getRangeTitle() {
			return titleY;
		}

		private boolean isNullOrEmpty(String s) {
			return s == null || s.trim().length() == 0;
		}

	}
}