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
package org.aavso.tools.vstar.external.lib;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;

/**
 * Builds minimal in-memory FITS files for plugin self-tests.
 */
public final class FitsTestData {

    private FitsTestData() {
    }

    /**
     * Lightkurve-style FITS with Kepler telescope metadata and one valid row.
     */
    public static byte[] createLightKurveKeplerFits() throws FitsException, IOException {
        return createFits("Kepler", new String[] { "TIME", "FLUX", "FLUX_ERR" },
                new Object[] { new double[] { 0.5 }, new float[] { 1000f }, new float[] { 10f } },
                null, null);
    }

    /**
     * Kepler/TESS archive-style FITS with PDCSAP columns (corrected flux).
     */
    public static byte[] createKeplerArchiveFits() throws FitsException, IOException {
        String[] names = { "TIME", "COL1", "COL2", "SAP_FLUX", "SAP_FLUX_ERR", "COL5", "COL6",
                "PDCSAP_FLUX", "PDCSAP_FLUX_ERR", "QUALITY" };
        Object[] cols = { new double[] { 0.5 }, new float[] { 0f }, new float[] { 0f },
                new float[] { 1000f }, new float[] { 10f }, new float[] { 0f }, new float[] { 0f },
                new float[] { 1000f }, new float[] { 10f }, new int[] { 0 } };
        return createFits("Kepler", names, cols, 2454833, 0.0);
    }

    private static byte[] createFits(String telescope, String[] columnNames, Object[] columns,
            Integer bjdRefInt, Double bjdRefFrac) throws FitsException, IOException {
        Fits fits = new Fits();

        nom.tam.fits.Data imageData = ImageHDU.encapsulate(new double[1][1]);
        ImageHDU image = new ImageHDU(ImageHDU.manufactureHeader(imageData), imageData);
        image.getHeader().addValue("TELESCOP", telescope, "");
        image.getHeader().addValue("OBJECT", "TEST", "");
        fits.addHDU(image);

        nom.tam.fits.Data tableData = BinaryTableHDU.encapsulate(columns);
        BinaryTableHDU table = new BinaryTableHDU(BinaryTableHDU.manufactureHeader(tableData), tableData);
        for (int i = 0; i < columnNames.length; i++) {
            table.setColumnName(i, columnNames[i], "");
        }
        if (bjdRefInt != null && bjdRefFrac != null) {
            table.getHeader().addValue("BJDREFI", bjdRefInt.longValue(), "");
            table.getHeader().addValue("BJDREFF", bjdRefFrac.doubleValue(), "");
        }
        fits.addHDU(table);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fits.write(new DataOutputStream(out));
        return out.toByteArray();
    }
}
