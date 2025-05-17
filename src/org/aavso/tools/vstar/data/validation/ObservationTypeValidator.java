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

import org.aavso.tools.vstar.data.ObsType;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.exception.ObservationValidationWarning;

/**
 * 
 */
public class ObserverTypeValidator extends AbstractStringValidator<String> {

    @Override
    public String validate(String str) throws ObservationValidationError {
        ObsType obsType = ObsType.getObsTypeFromName(str);
        
        if (obsType == ObsType.UNKNOWN) {
            String msg = "Unknown observation type";
            throw new ObservationValidationError(msg);
        }
        
        return obsType.getDescription();
    }
}
