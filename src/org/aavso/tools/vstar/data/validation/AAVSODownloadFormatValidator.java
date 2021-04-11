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

import java.io.IOException;

import org.aavso.tools.vstar.data.CommentType;
import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;

import com.csvreader.CsvReader;

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
 * 
 * From Doc, Dec 22 2009: "OK, the data download has been changed so that the
 * end of it is now Star Name, affiliation, mtype, group. I'll get the wiki page
 * changed." and "Affiliation is a tinyint(4) right now. It's not greatly
 * populated yet. Once it is I may make it an ASCII representation in the data
 * download. Group is a varchar(5) and has to do with filter wheels."
 */
public class AAVSODownloadFormatValidator extends CommonTextFormatValidator {

	private final OptionalityFieldValidator optionalFieldValidator;
	private final OptionalityFieldValidator nonOptionalFieldValidator;
	private final CompStarValidator compStarValidator;
	private final JulianDayValidator hjdValidator;
	private final CommentCodeValidator commentCodeValidator;
	private final TransformedValidator transformedValidator;
	private final CKMagValidator cMagValidator;
	private final CKMagValidator kMagValidator;
	private final OptionalityFieldValidator nameFieldValidator;
	private final MTypeValidator magTypeValidator;

	/**
	 * Constructor.
	 * 
	 * @param lineReader
	 *            The CsvReader that will be used to return fields, created with
	 *            the appropriate delimiter and data source.
	 * @param minFields
	 *            The minimum number of fields permitted in an observation line.
	 * @param maxFields
	 *            The maximum number of fields permitted in an observation line.
	 * @param fieldInfoSource
	 *            A mapping from field name to field index that makes sense for
	 *            the source.
	 */
	public AAVSODownloadFormatValidator(CsvReader lineReader, int minFields,
			int maxFields, IFieldInfoSource fieldInfoSource) throws IOException {
		super("AAVSO format observation line", lineReader, minFields,
				maxFields, COMMON_VALFLAG_PATTERN, fieldInfoSource);

		this.optionalFieldValidator = new OptionalityFieldValidator(
				OptionalityFieldValidator.CAN_BE_EMPTY);

		this.nonOptionalFieldValidator = new OptionalityFieldValidator("band",
				OptionalityFieldValidator.CANNOT_BE_EMPTY);

		this.nameFieldValidator = new OptionalityFieldValidator("name",
				OptionalityFieldValidator.CANNOT_BE_EMPTY);

		this.hjdValidator = new JulianDayValidator(
				JulianDayValidator.CAN_BE_EMPTY);

		// TODONE: AW updated this list with the list from
		// CommentType.getRegex()
		// keeps all the comment codes in one place
		this.commentCodeValidator = new CommentCodeValidator(
				CommentType.getRegex());

		this.compStarValidator = new CompStarValidator();

		this.transformedValidator = new TransformedValidator();

		this.cMagValidator = new CKMagValidator(CKMagValidator.CMAG_KIND);
		this.kMagValidator = new CKMagValidator(CKMagValidator.KMAG_KIND);

		this.magTypeValidator = new MTypeValidator();
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
	 * @return The validated ValidObservation object.
	 */
	public ValidObservation validate() throws IOException,
			ObservationValidationError, ObservationValidationWarning {

		ValidObservation observation = super.validate();

		// Validate the fields.

		Double hqUncertaintyMag = uncertaintyValueValidator
				.validate(fields[fieldIndexMap.get("HQ_UNCERTAINTY_FIELD")]);
		observation.setHqUncertainty(hqUncertaintyMag);

		// TODO: we should use a specific band validator that returns SeriesType
		String band = nonOptionalFieldValidator.validate(fields[fieldIndexMap
				.get("BAND_FIELD")]);
		observation.setBand(SeriesType.getSeriesFromShortName(band));

		String commentCode = commentCodeValidator.validate(fields[fieldIndexMap
				.get("COMMENT_CODE_FIELD")]);
		observation.setCommentCode(commentCode);

		String compStar1 = compStarValidator.validate(fields[fieldIndexMap
				.get("COMP_STAR_1_FIELD")]);
		observation.setCompStar1(compStar1);

		String compStar2 = compStarValidator.validate(fields[fieldIndexMap
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

		String cMag = cMagValidator.validate(fields[fieldIndexMap
				.get("CMAG_FIELD")]);
		observation.setCMag(cMag);

		String kMag = kMagValidator.validate(fields[fieldIndexMap
				.get("KMAG_FIELD")]);
		observation.setKMag(kMag);

		DateInfo hjdInfo = hjdValidator.validate(fields[fieldIndexMap
				.get("HJD_FIELD")]);
		observation.setHJD(hjdInfo);

		String name = nameFieldValidator.validate(fields[fieldIndexMap
				.get("NAME_FIELD")]);
		observation.setName(name);

		String affiliation = optionalFieldValidator
				.validate(fields[fieldIndexMap.get("AFFILIATION_FIELD")]);
		observation.setAffiliation(affiliation);

		MTypeType mType = magTypeValidator.validate(fields[fieldIndexMap
				.get("MTYPE_FIELD")]);
		if (mType != null) {
			observation.setMType(mType);
		}

		String group = optionalFieldValidator
				.validate(fields[fieldIndexMap.get("GROUP_FIELD")]);
		observation.setGroup(group);

		String ads_reference = optionalFieldValidator
				.validate(fields[fieldIndexMap.get("ADS_REFERENCE_FIELD")]);
		observation.setADSRef(ads_reference);

		String digitizer = optionalFieldValidator.validate(fields[fieldIndexMap
				.get("DIGITIZER_FIELD")]);
		observation.setDigitizer(digitizer);

		String credit = optionalFieldValidator.validate(fields[fieldIndexMap
				.get("CREDIT_FIELD")]);
		observation.setCredit(credit);

		return observation;
	}
}
