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

import java.util.Locale;

import org.aavso.tools.vstar.auth.AuthenticationSourceTest;
import org.aavso.tools.vstar.auth.VSXWebServiceMemberInfoTest;
import org.aavso.tools.vstar.data.filter.ObservationFilterTest;
import org.aavso.tools.vstar.data.validation.CKMagValidationTest;
import org.aavso.tools.vstar.data.validation.CommentCodeValidationTest;
import org.aavso.tools.vstar.data.validation.JulianDayValidationTest;
import org.aavso.tools.vstar.data.validation.MTypeValidationTest;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidationTest;
import org.aavso.tools.vstar.input.database.VSXWebServiceAIDCSV2ObservationReaderTest;
import org.aavso.tools.vstar.input.database.VSXWebServiceAIDCSVObservationReaderTest;
import org.aavso.tools.vstar.input.database.VSXWebServiceAIDXMLAttributeObservationReaderTest;
import org.aavso.tools.vstar.input.database.VSXWebServiceStarInfoSourceTest;
import org.aavso.tools.vstar.input.text.ObservationFieldSplitterTest;
import org.aavso.tools.vstar.input.text.TextFormatObservationReaderTest;
import org.aavso.tools.vstar.plugin.PluginManagerTest;
import org.aavso.tools.vstar.util.DecInfoTest;
import org.aavso.tools.vstar.util.RAInfoTest;
import org.aavso.tools.vstar.util.comparator.RankedIndexPairComparatorTest;
import org.aavso.tools.vstar.util.date.B1950EpochHJDConverterTest;
import org.aavso.tools.vstar.util.date.J2000EpochHJDConverterTest;
import org.aavso.tools.vstar.util.date.MeeusDateUtilTest;
import org.aavso.tools.vstar.util.locale.NumberParserTest;
import org.aavso.tools.vstar.util.period.dcdft.CleanestTest;
import org.aavso.tools.vstar.util.period.dcdft.DcDftTest;
import org.aavso.tools.vstar.util.period.dcdft.FreqRangeTopHitsDcDftTest;
import org.aavso.tools.vstar.util.period.dcdft.SinglePeriodModelDcDftTest;
import org.aavso.tools.vstar.util.period.dcdft.StdScanTopHitsDcDftTest;
import org.aavso.tools.vstar.util.period.dcdft.TwoPeriodModelDcDftTest;
import org.aavso.tools.vstar.util.period.wwz.WWZTUmi2420000To2425000Test;
import org.aavso.tools.vstar.util.polyfit.TSPolynomialFitterTest;
import org.aavso.tools.vstar.util.stats.DescStatsTest;
import org.aavso.tools.vstar.util.stats.PhaseCalcsTest;
import org.aavso.tools.vstar.util.stats.anova.CommonsMathAnovaTest;
import org.aavso.tools.vstar.util.stats.anova.EpsAurVisJD2454700ToJD2455000AnovaTest;
import org.aavso.tools.vstar.vela.VeLaTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		// Ensure no locale-related errors		
		Locale.setDefault(Locale.ENGLISH);
		
		TestSuite suite = new TestSuite("Test suite for VStar");
		
		// $JUnit-BEGIN$
		suite.addTestSuite(AuthenticationSourceTest.class);
		suite.addTestSuite(VSXWebServiceMemberInfoTest.class);
		suite.addTestSuite(ObservationFilterTest.class);
		suite.addTestSuite(CKMagValidationTest.class);
		suite.addTestSuite(CommentCodeValidationTest.class);
		suite.addTestSuite(JulianDayValidationTest.class);
		suite.addTestSuite(MTypeValidationTest.class);
		suite.addTestSuite(MagnitudeFieldValidationTest.class);
		suite.addTestSuite(VSXWebServiceAIDCSV2ObservationReaderTest.class);
		suite.addTestSuite(VSXWebServiceAIDCSVObservationReaderTest.class);
		suite.addTestSuite(VSXWebServiceAIDXMLAttributeObservationReaderTest.class);
		suite.addTestSuite(VSXWebServiceStarInfoSourceTest.class);
		suite.addTestSuite(ObservationFieldSplitterTest.class);
		suite.addTestSuite(TextFormatObservationReaderTest.class);
		suite.addTestSuite(PluginManagerTest.class);
		suite.addTestSuite(DecInfoTest.class);
		suite.addTestSuite(RAInfoTest.class);
		suite.addTestSuite(RankedIndexPairComparatorTest.class);
		suite.addTestSuite(B1950EpochHJDConverterTest.class);
		suite.addTestSuite(J2000EpochHJDConverterTest.class);
		suite.addTestSuite(MeeusDateUtilTest.class);
		suite.addTestSuite(NumberParserTest.class);
		suite.addTestSuite(CleanestTest.class);
		suite.addTestSuite(DcDftTest.class);
		suite.addTestSuite(FreqRangeTopHitsDcDftTest.class);
		suite.addTestSuite(SinglePeriodModelDcDftTest.class);
		suite.addTestSuite(StdScanTopHitsDcDftTest.class);
		suite.addTestSuite(TwoPeriodModelDcDftTest.class);
		suite.addTestSuite(WWZTUmi2420000To2425000Test.class);
		suite.addTestSuite(TSPolynomialFitterTest.class);
		suite.addTestSuite(DescStatsTest.class);
		suite.addTestSuite(PhaseCalcsTest.class);
		suite.addTestSuite(CommonsMathAnovaTest.class);
		suite.addTestSuite(EpsAurVisJD2454700ToJD2455000AnovaTest.class);
		suite.addTestSuite(VeLaTest.class);
		// $JUnit-END$
		
		return suite;
	}
}
