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

import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * This class validates real values,
 */
public class RealValueValidator extends AbstractStringValidator<Double> {

    protected boolean canBeNegative;
    protected boolean canBeEmpty;

    public RealValueValidator(String kind, boolean canBeNegative, boolean canBeEmpty) {
        super(kind);
        this.canBeNegative = canBeNegative;
        this.canBeEmpty = canBeEmpty;
    }

    @Override
    public Double validate(String str) throws ObservationValidationError {
        double result = Double.NaN;

        if (this.isLegallyEmpty(str)) {
            result = 0.0;
        } else {

            try {
                result = Double.parseDouble(str);

                if (!canBeNegative && result < 0) {
                    throw new ObservationValidationError(String.format(
                            LocaleProps.get("REAL_NUMBER_VALIDATOR_KIND_ERR_MSG") + " (cannot be negative)", kind,
                            str));
                }
            } catch (NumberFormatException e) {
                throw new ObservationValidationError(
                        String.format(LocaleProps.get("REAL_NUMBER_VALIDATOR_KIND_ERR_MSG"), kind, str));
            }
        }

        return result;
    }

    @Override
    protected boolean canBeEmpty() {
        return canBeEmpty;
    }
}
