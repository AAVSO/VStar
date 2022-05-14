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
package org.aavso.tools.vstar.util.period.dcdft;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.MiraDataSmall;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;

/**
 * Single period DCDFT derived Fourier model error test.
 */
public class DcDftDerivedFourierModelErrorTest extends MultiPeriodicModelDcDftTestBase {

	private static final double[][] expectedModelData = { { 53520.3, 3.5463729458620854 },
			{ 53538.9, 3.5814144916056385 }, { 53554.43, 3.883172669140103 }, { 53564.9, 4.2140725989090875 },
			{ 53575.38, 4.633636574023391 }, { 53585.27, 5.095942182917438 }, { 53596.39, 5.672285846367929 },
			{ 53605.62, 6.178696667410371 }, { 53615.57, 6.7343022858334685 }, { 53624.28, 7.213907854299972 },
			{ 53635.9, 7.820615933293786 }, { 53644.93, 8.248996166080888 }, { 53653.85, 8.62173407668023 },
			{ 53666.1, 9.03282030702743 }, { 53674.97, 9.246459976126285 }, { 53684.2, 9.386390065970533 },
			{ 53695.75, 9.437033583280463 }, { 53705.47, 9.370852811426731 }, { 53714.48, 9.222557813608296 },
			{ 53724.32, 8.970216237821214 }, { 53733.47, 8.658306626800576 }, { 53745.17, 8.166294652443385 },
			{ 53755.04, 7.686276022083018 }, { 53763.51, 7.240280563368235 }, { 53774.23, 6.6493832966416395 },
			{ 53785.1, 6.043165455021687 }, { 53794.58, 5.5284891796491005 }, { 53802.77, 5.107701637469683 },
			{ 53813.87, 4.591396397004367 }, { 53823.21, 4.220121494815597 }, { 53885.06, 3.8302877888538767 },
			{ 53916.9, 5.0450748979537865 }, { 53921.8, 5.290684286058075 }, { 53935.66, 6.033140580097026 },
			{ 53944.37, 6.518546865330231 }, { 53952.97, 6.996790554792577 }, { 53966.4, 7.71168285686055 },
			{ 53974.71, 8.117209075184379 }, { 53983.86, 8.516738866897542 }, { 53996.34, 8.961692340114658 },
			{ 54004.31, 9.175212146505668 }, { 54012.99, 9.338215514814246 }, { 54024.68, 9.436025616787603 },
			{ 54034.79, 9.404774163447291 }, { 54046.98, 9.225978077608552 }, { 54056.04, 8.998302945938013 },
			{ 54064.9, 8.704381071632447 }, { 54076.66, 8.219794050468668 }, { 54084.63, 7.841072368694966 },
			{ 54094.64, 7.322829363793132 }, { 54106.32, 6.681407401827773 }, { 54114.48, 6.225538775221794 },
			{ 54124.56, 5.671815927285571 }, { 54135.55, 5.101943952752439 }, { 54145.83, 4.621901872981052 },
			{ 54153.69, 4.301045217165385 }, { 54165.48, 3.9114563632326202 }, { 54173.4, 3.719969121810515 },
			{ 54183.25, 3.5681108588462678 }, { 54195.77, 3.5203292009555724 }, { 54251.31, 5.131302856946654 },
			{ 54267.31, 5.977172448899207 }, { 54281.45, 6.765063384029326 }, { 54295.76, 7.541445067514839 },
			{ 54304.36, 7.974290478640801 }, { 54318.6, 8.599331435945736 }, { 54323.86, 8.793697821222704 },
			{ 54334.7, 9.120330618232355 }, { 54356.63, 9.434156932547104 }, { 54364.66, 9.424170235382075 },
			{ 54375.65, 9.301156008802732 }, { 54385.94, 9.075912232248182 }, { 54393.87, 8.834995049932417 },
			{ 54406.19, 8.358360920037567 }, { 54413.77, 8.012847301399743 }, { 54423.67, 7.5154436358571886 },
			{ 54434.55, 6.927768823395037 }, { 54445.05, 6.342373099072735 }, { 54454.09, 5.841767831004475 },
			{ 54464.9, 5.2686353183178865 }, { 54475.52, 4.754352734878032 }, { 54485.29, 4.34181560323333 },
			{ 54494.96, 4.004868787465211 }, { 54500.99, 3.8357928380997204 } };

	private static final double[][] expectedResidualData = { { 53520.3, 0.15362705413791478 },
			{ 53538.9, -0.08141449160563852 }, { 53554.43, 0.1501273308598967 }, { 53564.9, 0.38592740109091217 },
			{ 53575.38, 0.4413634259766095 }, { 53585.27, 0.7415578170825627 }, { 53596.39, 0.9677141536320706 },
			{ 53605.62, 0.7313033325896292 }, { 53615.57, 0.48169771416653173 }, { 53624.28, 0.1913921457000285 },
			{ 53635.9, -0.18731593329378615 }, { 53644.93, -0.24419616608088823 }, { 53653.85, -0.3717340766802302 },
			{ 53666.1, -0.6028203070274305 }, { 53674.97, -0.6643599761262848 }, { 53684.2, -0.6632900659705321 },
			{ 53695.75, -0.5259335832804641 }, { 53705.47, -0.4152528114267309 }, { 53714.48, -0.26425781360829603 },
			{ 53724.32, 0.017283762178786688 }, { 53733.47, 0.3416933731994245 }, { 53745.17, 0.8037053475566154 },
			{ 53755.04, 1.1505239779169818 }, { 53763.51, 1.542519436631765 }, { 53774.23, 1.94151670335836 },
			{ 53785.1, 2.1068345449783132 }, { 53794.58, 1.2648108203508999 }, { 53802.77, 0.2222983625303172 },
			{ 53813.87, -0.5144963970043666 }, { 53823.21, -0.6201214948155971 }, { 53885.06, 0.46971221114612316 },
			{ 53916.9, 0.5549251020462131 }, { 53921.8, 0.6093157139419256 }, { 53935.66, 0.5668594199029737 },
			{ 53944.37, 0.3981531346697684 }, { 53952.97, 0.10320944520742259 }, { 53966.4, -0.31168285686054986 },
			{ 53974.71, -0.3958090751843786 }, { 53983.86, -0.6076388668975428 }, { 53996.34, -0.794992340114657 },
			{ 54004.31, -0.6952121465056678 }, { 54012.99, -0.6096155148142461 }, { 54024.68, -0.7087256167876035 },
			{ 54034.79, -0.6047741634472903 }, { 54046.98, -0.40597807760855176 }, { 54056.04, -0.32690294593801283 },
			{ 54064.9, -0.12158107163244658 }, { 54076.66, 0.03350594953133168 }, { 54084.63, 0.19222763130503484 },
			{ 54094.64, 0.1741706362068678 }, { 54106.32, -0.23660740182777307 }, { 54114.48, -0.8791387752217936 },
			{ 54124.56, -1.6086159272855705 }, { 54135.55, -1.9160439527524389 }, { 54145.83, -2.185801872981052 },
			{ 54153.69, -2.0231452171653848 }, { 54165.48, -1.3682563632326201 }, { 54173.4, -0.9042691218105148 },
			{ 54183.25, -0.42521085884626775 }, { 54195.77, -0.020329200955572446 }, { 54251.31, 0.8686971430533461 },
			{ 54267.31, 0.5228275511007929 }, { 54281.45, 0.3849366159706742 }, { 54295.76, -0.061445067514838136 },
			{ 54304.36, -0.1599904786408004 }, { 54318.6, -0.09933143594573579 }, { 54323.86, -0.21039782122270445 },
			{ 54334.7, -0.2703306182323555 }, { 54356.63, -0.35235693254710476 }, { 54364.66, -0.1928702353820757 },
			{ 54375.65, 0.08774399119726795 }, { 54385.94, 0.37408776775181707 }, { 54393.87, 0.47410495006758424 },
			{ 54406.19, 0.8598390799624323 }, { 54413.77, 0.995452698600257 }, { 54423.67, 1.2768563641428106 },
			{ 54434.55, 1.6008311766049639 }, { 54445.05, 1.2862269009272644 }, { 54454.09, 0.07383216899552547 },
			{ 54464.9, -0.6253353183178865 }, { 54475.52, -0.6619527348780325 }, { 54485.29, -0.4798156032333303 },
			{ 54494.96, -0.17156878746521143 }, { 54500.99, 0.047507161900279815 } };

	public DcDftDerivedFourierModelErrorTest(String name) {
		super(name, MiraDataSmall.data);
	}

	/**
	 * This test creates a model using the top-hit (period = ~332.6645, freq =
	 * ~0.003006) and carries out checks including for model error values.
	 */
	public void testModel() {
		// Perform a DCDFT with frequency range.
		// Equivalent to calling Grant Foster's R code:
		// dc = dcdft(t, x, .0022, .004, resmag=5)
		// (0.000050377 = nustep = .25/tspan/resmag)
		TSDcDft dcdft = new TSDcDft(obs, 0.0022, 0.004, 0.000050377);
		try {
			dcdft.execute();

			// Specify the harmonics (periods/frequencies) upon which the model
			// is to be based.
			double topFreq = dcdft.getTopHits().get(PeriodAnalysisCoordinateType.FREQUENCY).get(0);
			assertTrue(Tolerance.areClose(0.003006, topFreq, 1e-7, true));

			double topPeriod = dcdft.getTopHits().get(PeriodAnalysisCoordinateType.PERIOD).get(0);
			assertTrue(Tolerance.areClose(332.6645, topPeriod, DELTA, true));

			List<Harmonic> harmonics = new ArrayList<Harmonic>();
			harmonics.add(new Harmonic(topFreq));

			// Specify the expected model parameters (generated by the model
			// creation process).
			List<PeriodFitParameters> expectedParams = new ArrayList<PeriodFitParameters>();
			expectedParams
					.add(new PeriodFitParameters(new Harmonic(topFreq), 2.96087, 2.86083, 0.76317, 6.47752, 54013.0));

			// Drum roll please...
			commonTest(dcdft, harmonics, expectedParams, expectedModelData, expectedResidualData);

			// What are the standard errors?
			assertTrue(Tolerance.areClose(2.3577e-5, model.standardErrorOfTheFrequency(), 1e-9, true));
			assertTrue(Tolerance.areClose(0.1242, model.standardErrorOfTheSemiAmplitude(), DELTA, true));

			// What is the FWHM?
			Pair<Double, Double> fwhm = model.fwhm();
			double fwhmLo = fwhm.first;
			double fwhmHi = fwhm.second;
			assertTrue(Tolerance.areClose(0.0026534, fwhmLo, 1e-7, true));
			assertTrue(Tolerance.areClose(0.0034594, fwhmHi, 1e-7, true));

		} catch (AlgorithmError e) {
			fail();
		}

	}
}
