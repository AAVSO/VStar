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
package org.aavso.tools.vstar.data.validation;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationValidationError;

/**
 * This class accepts a line of text for tokenising, validation, and
 * ValidObservation instance creation given an AAVSO Download text format
 * source:
 * 
 * JD(0), MAGNITUDE(1), UNCERTAINTY(2), HQ_UNCERTAINTY(3), BAND(4),
 * OBSERVER_CODE(5), COMMENT_CODE(6), COMP_STAR_1(7), COMP_STAR_2(8), CHARTS(9),
 * COMMENTS(10), TRANSFORMED(11), AIRMASS(12), VALFLAG(13), CMAG(14), KMAG(15),
 * HJD(16), NAME(17), MTYPE(18)
 * 
 * REQ_VSTAR_AAVSO_DATA_DOWNLOAD_FILE_READ
 */
public class AAVSODownloadFormatValidator extends CommonTextFormatValidator {

	// TODO:
	// - Should we have a specific Band validator?
	// - Is -5..25 a suitable range for CMag and KMag?
	// - Throw an exception if both comparison stars are empty?
	// - What are the format constraints on compStar{1,2}
	// - Is the charts field optional? Sample data says yes, but
	// pre-validation filters page suggests otherwise.
	// - Create a validator for 'transformed' field?
	// - MType should have its own validator and associated enum type.
	// - Assert which fields should not be null at end of validate().

	private final OptionalityFieldValidator optionalFieldValidator;
	private final OptionalityFieldValidator nonOptionalFieldValidator;
	private final JulianDayValidator hjdValidator;
	private final CommentCodeValidator commentCodeValidator;
	private final TransformedValidator transformedValidator;
	private final MagnitudeValueValidator magnitudeValueValidator;

	/**
	 * Constructor.
	 * 
	 * @param delimiter
	 *            The field delimiter to use.
	 * @param minFields
	 *            The minimum number of fields permitted in an observation line.
	 * @param maxFields
	 *            The maximum number of fields permitted in an observation line.
	 * @param fieldInfoSource
	 *            A mapping from field name to field index that makes sense for
	 *            the source.
	 */
	public AAVSODownloadFormatValidator(String delimiter, int minFields,
			int maxFields, ITableFieldInfoSource fieldInfoSource) {
		super("AAVSO format observation line", delimiter, minFields, maxFields,
				"G|D|P", fieldInfoSource);

		this.optionalFieldValidator = new OptionalityFieldValidator(
				OptionalityFieldValidator.CAN_BE_EMPTY);

		this.nonOptionalFieldValidator = new OptionalityFieldValidator(
				OptionalityFieldValidator.CANNOT_BE_EMPTY);

		this.hjdValidator = new JulianDayValidator(
				JulianDayValidator.CAN_BE_EMPTY);

		this.commentCodeValidator = new CommentCodeValidator(
				"B|U|W|L|D|Y|K|S|Z|I|V");

		this.transformedValidator = new TransformedValidator();

		this.magnitudeValueValidator = new MagnitudeValueValidator(
				new InclusiveRangePredicate(-5, 25),
				MagnitudeValueValidator.CAN_BE_EMPTY);
	}

	/**
	 * Validate an observation line and either return a ValidObservation
	 * instance, or throw an exception indicating the error.
	 * 
	 * Given that the AAVSO download format should contain observations that
	 * have been scrutinised by AAVSO staff, field validation should in
	 * principle be unnecessary, but it cannot be harmful either. Some fields
	 * below are unvalidated (or sometimes just checked for optionality) however
	 * for the reason just stated.
	 * 
	 * @param line
	 *            The line of text to be tokenised and validated.
	 * @return The validated ValidObservation object.
	 * @throws ObservationValidationError
	 */
	public ValidObservation validate(String line)
			throws ObservationValidationError {

		ValidObservation observation = super.validate(line);

		// Validate the fields.

		Double hqUncertaintyMag = uncertaintyValueValidator
				.validate(fields[fieldIndexMap.get("HQ_UNCERTAINTY_FIELD")]);
		observation.setHqUncertainty(hqUncertaintyMag);

		String band = nonOptionalFieldValidator.validate(fields[fieldIndexMap
				.get("BAND_FIELD")]);
		observation.setBand(band);

		String commentCode = commentCodeValidator.validate(fields[fieldIndexMap
				.get("COMMENT_CODE_FIELD")]);
		observation.setCommentCode(commentCode);

		String compStar1 = optionalFieldValidator.validate(fields[fieldIndexMap
				.get("COMP_STAR_1_FIELD")]);
		observation.setCompStar1(compStar1);

		String compStar2 = optionalFieldValidator.validate(fields[fieldIndexMap
				.get("COMP_STAR_2_FIELD")]);
		observation.setCompStar2(compStar2);

		String charts = optionalFieldValidator.validate(fields[fieldIndexMap
				.get("CHARTS_FIELD")]);
		observation.setCharts(charts);

		String comments = optionalFieldValidator.validate(fields[fieldIndexMap
				.get("COMMENTS_FIELD")]);
		observation.setComments(comments);

		boolean isTransformed = transformedValidator
				.validate(fields[fieldIndexMap.get("TRANSFORMED_FIELD")]);
		observation.setTransformed(isTransformed);

		String airmass = optionalFieldValidator.validate(fields[fieldIndexMap
				.get("AIRMASS_FIELD")]);
		observation.setAirmass(airmass);

		Double cMag = magnitudeValueValidator.validate(fields[fieldIndexMap
				.get("CMAG_FIELD")]);
		observation.setCMag(cMag != null ? cMag.toString() : ""); // see ValidationObject comment

		Double kMag = magnitudeValueValidator.validate(fields[fieldIndexMap
				.get("KMAG_FIELD")]);
		observation.setKMag(kMag != null ? kMag.toString() : ""); // see ValidationObject comment

		DateInfo hjdInfo = hjdValidator.validate(fields[fieldIndexMap
				.get("HJD_FIELD")]);
		observation.setHJD(hjdInfo);

		String name = nonOptionalFieldValidator.validate(fields[fieldIndexMap
				.get("NAME_FIELD")]);
		observation.setName(name);

		// For now, we just store this.
		String mType = optionalFieldValidator.validate(fields[fieldIndexMap
				.get("MTYPE_FIELD")]);
		if ("NULL".equals(mType)) {
			mType = null;
		}
		observation.setMType(mType);

		return observation;
	}
}
