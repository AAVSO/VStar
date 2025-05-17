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
package org.aavso.tools.vstar.external.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.aavso.tools.vstar.data.CommentType;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.data.validation.CKMagValidator;
import org.aavso.tools.vstar.data.validation.CommentCodeValidator;
import org.aavso.tools.vstar.data.validation.CompStarValidator;
import org.aavso.tools.vstar.data.validation.JulianDayValidator;
import org.aavso.tools.vstar.data.validation.ObserverCodeValidator;
import org.aavso.tools.vstar.data.validation.ObserverTypeValidator;
import org.aavso.tools.vstar.data.validation.OptionalityFieldValidator;
import org.aavso.tools.vstar.data.validation.RealValueValidator;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.mediator.NewStarType;

import com.csvreader.CsvReader;

/**
 * New (2025) AAVSO Photometry Search Export observation source plug-in.<br/>
 * 
 * This plug-in reads exported CSV files from
 * https://apps.aavso.org/v2/data/search/photometry/
 */
public class AAVSOPhotometrySearchExportFormatObservationSource extends ObservationSourcePluginBase {

    private final static String DESC = "AAVSO Photometry Search Format";

    private Map<Integer, FIELD> name2field = new HashMap<Integer, FIELD>();

    enum FIELD {
        RECORDNUM("#", 0), TARGET("target", 1), AUID("auid", 2), JD("jd", 3), MAG("mag", 4),
        UNCERTAINTY("uncertainty", 5), FAINTERTHAN("fainterthan", 6), BAND("band", 7), TYPE("type", 8),
        OBSERVER("observer", 9), AIRMASS("airmass", 10), TRANSFORMED("transformed", 11), COMP1_C("comp1_c", 12),
        CMAG("cmag", 13), COMP2_K("comp2_k", 14), KMAG("kmag", 15), CHART("chart", 16), COMMENTCODE("commentcode", 17),
        COMMENTS("comments", 18); 

        String name;
        int index;

        FIELD(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }

    public AAVSOPhotometrySearchExportFormatObservationSource() {
        for (FIELD field : FIELD.values()) {
            name2field.put(field.index, field);
        }
    }

    @Override
    public String getDescription() {
        return DESC + " file reader";
    }

    @Override
    public String getDisplayName() {
        return "New Star from " + DESC + "...";
    }

    /**
     * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
     */
    @Override
    public String getDocName() {
        return "https://github.com/AAVSO/VStar/wiki/Observation-Source-Plug%E2%80%90ins#"
                + "aavso-photometry-search-export-format-file-reader";
    }

    @Override
    public InputType getInputType() {
        return InputType.FILE;
    }

    @Override
    public NewStarType getNewStarType() {
        return NewStarType.NEW_STAR_FROM_DOWNLOAD_FILE;
    }

    @Override
    public AbstractObservationRetriever getObservationRetriever() throws IOException, ObservationReadError {
        return new PhotometrySearchExportFileReader();
    }

    class PhotometrySearchExportFileReader extends AbstractObservationRetriever {

        private final OptionalityFieldValidator nonOptionalFieldValidator;
        private final CompStarValidator compStarValidator;
        private final JulianDayValidator jdValidator;
        private final RealValueValidator magValidator;
        private final RealValueValidator uncertaintyValidator;
        private final ObserverTypeValidator observerTypeValidator;
        private final ObserverCodeValidator observerCodeValidator;
        private final CommentCodeValidator commentCodeValidator;
        private final CKMagValidator cMagValidator;
        private final CKMagValidator kMagValidator;

        public PhotometrySearchExportFileReader() {
            this.nonOptionalFieldValidator = new OptionalityFieldValidator("band",
                    OptionalityFieldValidator.CANNOT_BE_EMPTY);

            this.jdValidator = new JulianDayValidator(JulianDayValidator.CAN_BE_EMPTY);

            this.magValidator = new RealValueValidator(FIELD.MAG.name, true, false);
            this.uncertaintyValidator = new RealValueValidator(FIELD.UNCERTAINTY.name, false, true);

            this.observerCodeValidator = new ObserverCodeValidator();
            this.observerTypeValidator = new ObserverTypeValidator();

            this.compStarValidator = new CompStarValidator();

            this.cMagValidator = new CKMagValidator(CKMagValidator.CMAG_KIND);
            this.kMagValidator = new CKMagValidator(CKMagValidator.KMAG_KIND);

            this.commentCodeValidator = new CommentCodeValidator(CommentType.getRegex());
        }

        @Override
        public void retrieveObservations() throws ObservationReadError, InterruptedException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStreams().get(0)));

            CsvReader csvReader = new CsvReader(reader);
            int lineNum = 0;

            try {
                if (!csvReader.readHeaders()) {
                    throw new ObservationReadError("Not a " + DESC + " format file");
                }

                while (csvReader.readRecord()) {
                    try {
                        lineNum++;

                        ValidObservation ob = new ValidObservation();
                        ob.setMagnitude(new Magnitude());

                        String[] values = csvReader.getValues();

                        for (int i = 0; i < values.length; i++) {
                            addToObservation(ob, values[i], i);
                        }

                        collectObservation(ob);
                    } catch (ObservationValidationError e) {
                        // Create an invalid observation.
                        // Record the line number rather than observation number for
                        // error reporting purposes.
                        String error = e.getLocalizedMessage();
                        String line = csvReader.getRawRecord();
                        InvalidObservation ob = new InvalidObservation(line, error);
                        ob.setRecordNumber(lineNum);
                        addInvalidObservation(ob);
                    }
                }
            } catch (IOException e) {
                throw new ObservationReadError(e.getLocalizedMessage());
            }
        }

        @Override
        public String getSourceName() {
            return getInputName();
        }

        @Override
        public String getSourceType() {
            return "Photometry Search Export File";
        }

        // Helpers

        private FIELD indexToField(int index) {
            assert name2field.containsKey(index);
            return name2field.get(index);
        }

        private void addToObservation(ValidObservation ob, String value, int index) throws ObservationValidationError {
            FIELD field = indexToField(index);

            switch (field) {
            case RECORDNUM:
                ob.setRecordNumber(Integer.parseInt(value));
                break;

            case TARGET:
                ob.setName(value);
                break;

            case AUID:
                ob.setName(ob.getName() + " (" + value+ ")");
                break;

            case JD:
                ob.setDateInfo(jdValidator.validate(value));
                ob.setJDflavour(JDflavour.JD);
                break;

            case MAG:
                double mag = magValidator.validate(value);
                ob.getMagnitude().setMagValue(mag);
                break;

            case UNCERTAINTY:
                double uncertainty = uncertaintyValidator.validate(value);
                ob.getMagnitude().setUncertainty(uncertainty);
                break;

            case FAINTERTHAN:
                // "True" or "False" -> Java Boolean value -> magnitude modifier
                if (Boolean.parseBoolean(value.toLowerCase())) {
                    ob.getMagnitude().setMagModifier(MagnitudeModifier.FAINTER_THAN);
                }
                break;

            case BAND:
                String band = nonOptionalFieldValidator.validate(value);
                ob.setBand(SeriesType.getSeriesFromDescription(band));
                break;

            case TYPE:
                ob.setObsType(observerTypeValidator.validate(value));
                break;

            case OBSERVER:
                ob.setObsCode(observerCodeValidator.validate(value));
                break;

            case AIRMASS:
                ob.setAirmass(value);
                break;

            case TRANSFORMED:
                ob.setTransformed(Boolean.getBoolean(value));
                break;

            case COMP1_C:
                ob.setCompStar1(compStarValidator.validate(value));
                break;

            case CMAG:
                ob.setCMag(cMagValidator.validate(value));
                break;

            case COMP2_K:
                ob.setCompStar2(compStarValidator.validate(value));
                break;

            case KMAG:
                ob.setKMag(kMagValidator.validate(value));
                break;

            case CHART:
                ob.setCharts(value);
                break;

            case COMMENTCODE:
                ob.setCommentCode(commentCodeValidator.validate(value));
                break;

            case COMMENTS:
                ob.setComments(value);
                break;
            }
        }
    }
}
