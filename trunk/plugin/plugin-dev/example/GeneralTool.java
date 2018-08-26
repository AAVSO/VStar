package example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;
import org.aavso.tools.vstar.util.locale.NumberParser;

/**
 * A general tool plugin that displays a list of AID series long and short names
 * in a dialog.
 */
public class GeneralTool extends GeneralToolPluginBase {

	@Override
	public void invoke() {
		new TextDialog("AID band names", aidSeries());
	}

	@Override
	public String getDescription() {
		return "Show AID band full and short names";
	}

	@Override
	public String getDisplayName() {
		return "AID Band Names";
	}

	private ITextComponent<String> aidSeries() {
		List<SeriesType> seriesList = new ArrayList<SeriesType>();

		seriesList.add(SeriesType.Visual);
		seriesList.add(SeriesType.Johnson_V);
		seriesList.add(SeriesType.Johnson_B);
		seriesList.add(SeriesType.Johnson_R);
		seriesList.add(SeriesType.Johnson_I);
		seriesList.add(SeriesType.Cousins_I);
		seriesList.add(SeriesType.Cousins_R);
		seriesList.add(SeriesType.Unfiltered_with_R_Zeropoint);
		seriesList.add(SeriesType.Unfiltered_with_V_Zeropoint);
		seriesList.add(SeriesType.Tri_Color_Blue);
		seriesList.add(SeriesType.Tri_Color_Green);
		seriesList.add(SeriesType.Tri_Color_Red);
		seriesList.add(SeriesType.H_NIR_1pt6micron);
		seriesList.add(SeriesType.J_NIR_1pt2micron);
		seriesList.add(SeriesType.K_NIR_2pt2micron);
		seriesList.add(SeriesType.Sloan_g);
		seriesList.add(SeriesType.Sloan_i);
		seriesList.add(SeriesType.Sloan_r);
		seriesList.add(SeriesType.Sloan_u);
		seriesList.add(SeriesType.Stromgren_b);
		seriesList.add(SeriesType.Stromgren_Hbn);
		seriesList.add(SeriesType.Stromgren_Hbw);
		seriesList.add(SeriesType.Stromgren_u);
		seriesList.add(SeriesType.Stromgren_v);
		seriesList.add(SeriesType.Stromgren_y);
		seriesList.add(SeriesType.Optec_Wing_A);
		seriesList.add(SeriesType.Optec_Wing_B);
		seriesList.add(SeriesType.Optec_Wing_C);
		seriesList.add(SeriesType.PanSTARRS_Y);
		seriesList.add(SeriesType.PanSTARRS_Z_short);
		seriesList.add(SeriesType.Halpha);
		seriesList.add(SeriesType.Halpha_continuum);
		seriesList.add(SeriesType.Orange_Liller);
		seriesList.add(SeriesType.Red);
		seriesList.add(SeriesType.Blue);
		seriesList.add(SeriesType.Green);
		seriesList.add(SeriesType.Yellow);
		seriesList.add(SeriesType.Clear_Blue_Blocking);

		Collections.sort(seriesList);

		return new TextArea("Long & short names",
				createAIDSeriesString(seriesList));
	}

	private String createAIDSeriesString(List<SeriesType> seriesList) {

		StringBuffer buf = new StringBuffer();

		for (SeriesType series : seriesList) {
			buf.append(series.getDescription());
			buf.append(" (");
			buf.append(series.getShortName());
			buf.append(")\n");
		}

		return buf.toString();
	}
}
