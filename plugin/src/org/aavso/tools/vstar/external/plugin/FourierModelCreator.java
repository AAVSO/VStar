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

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.model.plot.ContinuousModelFunction;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;

/**
 * This plug-in creates a Fourier model from a number of periods and one or more
 * of their harmonics.
 */
public class FourierModelCreator extends ModelCreatorPluginBase {

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModel getModel(List<ValidObservation> obs) {
		// TODO Auto-generated method stub
		return null;
	}

	class FourierModel implements IModel {

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<ValidObservation> getFit() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, String> getFunctionStrings() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getKind() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ContinuousModelFunction getModelFunction() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<PeriodFitParameters> getParameters() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<ValidObservation> getResiduals() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasFuncDesc() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void execute() throws AlgorithmError {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void interrupt() {
			// TODO Auto-generated method stub
			
		}
	}
}
