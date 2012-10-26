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
import java.io.InputStreamReader;

import org.aavso.tools.vstar.data.CommentType;
import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.validation.CommentCodeValidator;
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.data.validation.MagnitudeValueValidator;
import org.aavso.tools.vstar.data.validation.TransformedValidator;
import org.aavso.tools.vstar.data.validation.UncertaintyValueValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;

/**
 * This plug-in class reads AAVSO upload (extended and visual) format files,
 * yielding an observation list.
 * 
 * See the following for information about the extended and visual file formats:
 * 
 * http://www.aavso.org/aavso-extended-file-format
 * http://www.aavso.org/aavso-visual-file-format
 */
public class AAVSOUploadFileFormatObservationSource extends
		ObservationSourcePluginBase {

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
		return InputType.FILE;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#
	 *      getObservationRetriever ()
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new AAVSOUploadFileFormatRetriever();
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "An AAVSO Upload File (Visual and Extended) Format reader.";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "New Star from AAVSO Upload Format File...";
	}

	class AAVSOUploadFileFormatRetriever extends AbstractObservationRetriever {
		private String fileType;
		private String obscode;
		private String software;
		private String delimiter;
		private String dateType;
		private String obsType;

		private JulianDayValidator julianDayValidator;
		private MagnitudeFieldValidator magnitudeFieldValidator;
		private UncertaintyValueValidator uncertaintyValueValidator;
		private TransformedValidator transformedValidator;
		private MagnitudeValueValidator magnitudeValueValidator;
		private CommentCodeValidator commentCodeValidator;

		/**
		 * Constructor.
		 */
		public AAVSOUploadFileFormatRetriever() {
			julianDayValidator = new JulianDayValidator();

			magnitudeFieldValidator = new MagnitudeFieldValidator();

			uncertaintyValueValidator = new UncertaintyValueValidator(
					new InclusiveRangePredicate(0, 1));

			transformedValidator = new TransformedValidator();

			// What should the range be for CCD/PEP, Visual/PTG?
			magnitudeValueValidator = new MagnitudeValueValidator(
					new InclusiveRangePredicate(-10, 25));

			this.commentCodeValidator = new CommentCodeValidator(CommentType
					.getRegex());
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getInputStreams().get(0)));

			String line = null;
			int lineNum = 1;
			int obNum = 1;

			do {
				try {
					line = reader.readLine();
					if (line != null) {
						line = line.replaceFirst("\n", "").replaceFirst("\r",
								"");
						if (!isEmpty(line)) {
							if (line.startsWith("#")) {
								handleDirective(line);
							} else {
								String[] fields = line.split(delimiter);
								collectObservation(readNextObservation(fields,
										obNum));
								obNum++;
							}
						}
						lineNum++;
					}
				} catch (Exception e) {
					// Create an invalid observation.
					// Record the line number rather than observation number for
					// error reporting purposes, but still increment the latter.
					String error = e.getLocalizedMessage();
					InvalidObservation ob = new InvalidObservation(line, error);
					ob.setRecordNumber(lineNum);
					obNum++;
					addInvalidObservation(ob);
				}
			} while (line != null);
		}

		// If a line starts with #, it's either a directive or a comment.
		private void handleDirective(String line) throws ObservationReadError {
			String[] pair = line.toUpperCase().split("=");

			pair[0] = pair[0].trim();
			pair[1] = pair[1].trim();
			
			if ("#TYPE".equals(pair[0])) {
				fileType = pair[1];
				if (!"EXTENDED".equals(fileType) && !"VISUAL".equals(fileType)) {
					throw new ObservationReadError("Invalid file type: "
							+ fileType);
				}
			} else if ("#OBSCODE".equals(pair[0])) {
				obscode = pair[1].toUpperCase();
			} else if ("#SOFTWARE".equals(pair[0])) {
				software = pair[1];
			} else if ("#DELIM".equals(pair[0])) {
				delimiter = translateDelimiter(pair[1]);
				if (isEmpty(delimiter)) {
					throw new ObservationReadError("No delimiter specified.");
				}
			} else if ("#DATE".equals(pair[0])) {
				dateType = pair[1];
			} else if ("#OBSTYPE".equals(pair[0])) {
				obsType = pair[1];
				if ("EXTENDED".equals(fileType)) {
					if (!"CCD".equals(obsType) && !"PEP".equals(obsType)) {
						throw new ObservationReadError(
								"Unknown observation type: " + obsType);
					}
				} else if ("VISUAL".equals(fileType)) {
					if (!"VISUAL".equals(obsType) && !"PTG".equals(obsType)) {
						throw new ObservationReadError(
								"Unknown observation type: " + obsType);
					}
				}
			}
		}

		// Translate the delimiter.
		private String translateDelimiter(String delim)
				throws ObservationReadError {
			if ("tab".equalsIgnoreCase(delim)) {
				delim = "\t";
			} else if ("space".equalsIgnoreCase(delim)) {
				delim = " ";
			} else if (delim != null) {
				try {
					int ordVal = Integer.parseInt(delim);
					if (ordVal < 32 || ordVal > 126) {
						throw new ObservationReadError(
								String
										.format("Ordinal delimiter value '%d' out of range 32..126"));
					}
					// Escape it in case it's a regex meta-character.
					delim = "\\" + String.valueOf((char) ordVal);
				} catch (NumberFormatException e) {
					// Nothing to do.
				}
			}

			return delim;
		}

		// Read the next observation.
		private ValidObservation readNextObservation(String[] fields, int obNum)
				throws ObservationValidationError {
			ValidObservation observation = null;

			if ("VISUAL".equals(fileType)) {
				observation = readNextVisualObservation(fields, obNum);
			} else {
				observation = readNextExtendedObservation(fields, obNum);
			}

			return observation;
		}

		// Extended format observation reader.
		private ValidObservation readNextExtendedObservation(String[] fields,
				int obNum) throws ObservationValidationError {

			ValidObservation observation = commonReadNextObservation(fields,
					obNum);

			String uncertaintyStr = fields[3].trim();
			if (!isNA(uncertaintyStr)) {
				double uncertainty = uncertaintyValueValidator
						.validate(uncertaintyStr);
				observation.getMagnitude().setUncertainty(uncertainty);
			}

			String filter = fields[4].trim();
			SeriesType band = SeriesType.getSeriesFromShortName(filter);
			observation.setBand(band);

			String transformedStr = fields[5].trim();
			if (!isNA(transformedStr)) {
				boolean transformed = transformedValidator
						.validate(transformedStr);
				// Defaults to false.
				observation.setTransformed(transformed);
			}

			// ValidObservation defaults to STD.
			String mtypeStr = fields[6].trim();
			MTypeType mtype = null;
			if (!isNA(mtypeStr)) {
				if ("DIF".equals(mtypeStr)) {
					mtype = MTypeType.DIFF;
				} else if ("STD".equals(mtypeStr)) {
					mtype = MTypeType.STD;
				} else if ("ABS".equals(mtypeStr)) {
					mtype = MTypeType.STD;
				}
			}
			if (mtype != null) {
				observation.setMType(mtype);

			}

			String cname = fields[7].trim();
			if (isNA(cname)) {
				if (mtype == MTypeType.DIFF) {
					throw new ObservationValidationError(
							"Magnitude type is differential but there is no CNAME.");
				} else {
					cname = "";
				}
			} else {
				cname += ": ";
			}

			String cmagStr = fields[8].trim();
			if (!isNA(cmagStr)) {
				// Note: Could CKMagValidator here, but its max field width
				// seems not to represent the reality of some instrumental
				// magnitudes, for example.
				double cmag = magnitudeValueValidator.validate(cmagStr);
				observation.setCMag(cname + cmag);
			}

			String kname = fields[9].trim();
			if (isNA(kname)) {
				kname = "";
			} else {
				kname += ": ";
			}

			String kmagStr = fields[10].trim();
			if (!isNA(kmagStr)) {
				// Note: Could CKMagValidator here, but its max field width
				// seems not to represent the reality of some instrumental
				// magnitudes, for example.
				double kmag = magnitudeValueValidator.validate(kmagStr);
				observation.setKMag(kname + kmag);
			}

			String airmass = fields[11].trim();
			if (!isNA(airmass)) {
				observation.setAirmass(airmass);
			}

			String group = fields[12].trim();
			if (group.length() > 5) {
				throw new ObservationValidationError(
						"GROUP has more than 5 characters.");
			}

			String chart = fields[13].trim();
			if (!isNA(chart)) {
				observation.setCharts(chart);
			}

			handleComments(fields[14].trim(), group, observation);

			return observation;
		}

		// Visual format observation reader.
		private ValidObservation readNextVisualObservation(String[] fields,
				int obNum) throws ObservationValidationError {

			ValidObservation observation = commonReadNextObservation(fields,
					obNum);

			observation.setBand(SeriesType.Visual);

			String commentCode = fields[3].trim();
			if (!isNA(commentCode)) {
				commentCode = commentCodeValidator.validate(commentCode);
				if (commentCode != null) {
					observation.setCommentCode(commentCode);
				}
			}

			String comp1 = fields[4].trim();
			if (!isNA(comp1)) {
				observation.setCompStar1(comp1);
			}

			String comp2 = fields[5].trim();
			if (!isNA(comp2)) {
				observation.setCompStar2(comp2);
			}

			String chart = fields[6].trim();
			if (!isNA(chart)) {
				observation.setCharts(chart);
			}

			if (fields.length == 8) {
				// The visual format doesn't say that if N/A, "na" must be
				// used in this field like the Extended format does, so we
				// allow for the possibility that this field is missing.
				handleComments(fields[7].trim(), null, observation);
			}

			return observation;
		}

		// Common (to extended and visual formats) observation reader.
		// Reads: name, date, magnitude. Sets record number, observer code.
		private ValidObservation commonReadNextObservation(String[] fields,
				int obNum) throws ObservationValidationError {
			ValidObservation observation = new ValidObservation();

			String name = fields[0].trim();

			observation.setRecordNumber(obNum);
			observation.setName(name);
			observation.setObsCode(obscode);

			// TODO: handle calendar date format.
			if (!"JD".equals(dateType) && !"HJD".equals(dateType)) {
				throw new ObservationValidationError("Unsupported date type: "
						+ dateType);
			} else {
				DateInfo dateInfo = julianDayValidator.validate(fields[1].trim());
				observation.setDateInfo(dateInfo);
			}

			Magnitude magnitude = magnitudeFieldValidator.validate(fields[2].trim());
			observation.setMagnitude(magnitude);

			return observation;
		}

		private void handleComments(String notes, String group,
				ValidObservation observation) {
			// Combine some fields as comments.
			String comments = "";

			if (!isNA(notes)) {
				comments = notes;
			}

			if (!isEmpty(software)) {
				if (!isEmpty(comments)) {
					comments += "; ";
				}
				comments += "software: " + software;
			}

			if (!isNA(group)) {
				if (!isEmpty(comments)) {
					comments += "; ";
				}
				comments += "group: " + group;
			}

			if (!isEmpty(comments)) {
				observation.setComments(comments);
			}
		}

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "AAVSO Upload Format File (" + fileType.toLowerCase() + ")" ;
		}
	}

	private boolean isEmpty(String str) {
		return str != null && "".equals(str.trim());
	}

	private boolean isNA(String str) {
		return "NA".equalsIgnoreCase(str);
	}
}
