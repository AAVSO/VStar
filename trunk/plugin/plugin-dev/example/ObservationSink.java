package example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;

/**
 * An observation sink plugin that saves observations as simple XML files
 * consisting of time, magnitude, uncertainty, and band.
 */
public class ObservationSink extends ObservationSinkPluginBase {

	@Override
	public String getDisplayName() {
		return "Simple XML File";
	}

	@Override
	public String getDescription() {
		return "Simple XML observation file format";
	}

	@Override
	public void save(PrintWriter writer, List<ValidObservation> obs,
			String delimiter) throws IOException {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		for (ValidObservation ob : obs) {
			writer.print("<observation ");
			writer.printf("time=%f magnitude=%f error=%f band=%s/>\n",
					ob.getJD(), ob.getMag(),
					ob.getMagnitude().getUncertainty(), ob.getBand());
		}
	}
}
