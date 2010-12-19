/**
 * 
 */
package org.aavso.tools.vstar.external.plugin;

import javax.swing.JOptionPane;

import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.util.date.AbstractDateUtil;

/**
 * A general tool plugin that converts a JD to a calendar date.
 */
public class JDToDateTool extends GeneralToolPluginBase {

	@Override
	public void invoke() {
		String str = JOptionPane.showInputDialog("Enter JD");
		if (str != null && str.trim().length() != 0) {
			try {
				double jd = Double.parseDouble(str);
				String cal = AbstractDateUtil.getInstance().jdToCalendar(jd);
				MessageBox.showMessageDialog("JD to Calendar Date", String
						.format("%1.4f => %s", jd, cal));
			} catch (NumberFormatException e) {
				MessageBox.showErrorDialog("JD Error", String.format(
						"'%s' is not a number.", str));
			} catch (IllegalArgumentException e) {
				MessageBox.showErrorDialog("JD Error", String.format(
						"'%s' is an invalid JD: %s", str, e.getMessage()));
			}
		}
	}

	@Override
	public String getDescription() {
		return "Convert JD to Calendar Date";
	}

	@Override
	public String getDisplayName() {
		return "JD to Calendar Date...";
	}
}
