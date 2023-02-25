#!/usr/bin/python

# How to get Gaia DR3 photometry:
# 0) Find the Gaia DR3 identifier for the object of interest (i.e., via https://vizier.cds.unistra.fr/viz-bin/VizieR )
# 1) Go to https://gaia.aip.de/query/
# 2) Enter the query:
#    select * from gaiadr3.epoch_photometry where source_id=<put numeric source id here>
# 3) After submitting the query, go to the [Download] tab and download the result as 'Comma separated Values'. Save it as 'GaiaDR3.csv'
# 4) Run this script. The data in the VStar Flexible Table Format should be in the 'GaiaDR3.txt' file.

import sys
import os
import ast
import pandas as pd

# Default input file name.
input_file_name = "GaiaDR3.csv"

GAIA_EPOCH = 2_455_197.5 # https://gea.esac.esa.int/archive/documentation/GDR3/Gaia_archive/chap_datamodel/sec_dm_photometry/ssec_dm_epoch_photometry.html
INVALID_MAG = 99.99
MAX_DELTA_T = 1.0 / 24.0 / 30.0 # 2-minutes tolerance

def getMagForT(time, times, mags):
    minDeltaT = MAX_DELTA_T
    mag = INVALID_MAG
    for i in range(0, len(times)):
        if (times[i] > 0 and mags[i] != INVALID_MAG and abs(times[i] - time) <= minDeltaT):
            minDeltaT = abs(times[i] - time)
            mag = mags[i]
    return mag

# https://gea.esac.esa.int/archive/documentation/GDR2/Data_processing/chap_cu5pho/sec_cu5pho_calibr/ssec_cu5pho_PhotTransf.html?fbclid=IwAR0RfPdrTpBIFXLjzRjKns5YlXsL1ycEYK_4Mu2bnrQEiYQ_DlNKAhy1iJ0#Ch5.T8
def v_form_GaiaG(g, bp, rp):
    bp_minus_rp = bp - rp
    return g - (-0.0176 - 0.00686 * bp_minus_rp - 0.1732 * bp_minus_rp**2)

def print_data(output_file, times, mags, filter):
    for i in range(0, len(times)):
        if times[i] > 0:
            output_file.write(str(times[i] + GAIA_EPOCH) + "\t" + str(mags[i]) + "\t\t" + str(filter) + "\n")

output_file_name = None

if (len(sys.argv) == 3):
    input_file_name = sys.argv[1]
    output_file_name = sys.argv[2]
elif (len(sys.argv) == 2):
    input_file_name = sys.argv[1]
elif (len(sys.argv) == 1):
    pass
else:
    print("Usage:")
    print("GaiaDR3.py input_file_name [output_file_name]")
    sys.exit(1)


if (output_file_name == None):
    output_file_name = os.path.splitext(input_file_name)[0] + ".txt"
    
#print("input_file_name :", input_file_name)
#print("output_file_name:", output_file_name)

d = pd.read_csv(input_file_name)
source_id = d["source_id"][0]
g_transit_time = ast.literal_eval(d["g_transit_time"][0].replace("NaN", "0"))
g_transit_mag = ast.literal_eval(d["g_transit_mag"][0].replace("NaN", str(INVALID_MAG)))
bp_obs_time = ast.literal_eval(d["bp_obs_time"][0].replace("NaN", "0"))
bp_mag = ast.literal_eval(d["bp_mag"][0].replace("NaN", str(INVALID_MAG)))
rp_obs_time = ast.literal_eval(d["rp_obs_time"][0].replace("NaN", "0"))
rp_mag = ast.literal_eval(d["rp_mag"][0].replace("NaN", str(INVALID_MAG)))

# VStar Flexible Text Format
output_file = open(output_file_name, "w")
# header
output_file.write("#NAME=source_id=" + str(source_id) + "\n\n");
output_file.write("#DATE=BJD\n");
output_file.write("#DELIM=tab\n");
output_file.write("#FIELDS=TIME,MAG,,FILTER,COMMENTS\n\n");
output_file.write("#DEFINESERIES=Gaia G,g_transit_mag,#00FF00\n");
output_file.write("#DEFINESERIES=Gaia BP,bp_mag,#0000FF\n");
output_file.write("#DEFINESERIES=Gaia RP,rp_mag,#FF0000\n");
output_file.write("#DEFINESERIES=V from Gaia G,v_from_g,#009900\n\n");

# original magnitudes
print_data(output_file, g_transit_time, g_transit_mag, "g_transit_mag")
output_file.write("\n")
print_data(output_file, bp_obs_time, bp_mag, "bp_mag")
output_file.write("\n")
print_data(output_file, rp_obs_time, rp_mag, "rp_mag")
output_file.write("\n")

# V from Gaia G
for i in range(0, len(g_transit_time)):
    t = g_transit_time[i]
    g = g_transit_mag[i]
    if (t > 0 and g != INVALID_MAG):
        bp = getMagForT(t, bp_obs_time, bp_mag)
        rp = getMagForT(t, rp_obs_time, rp_mag)
        if (bp != INVALID_MAG and rp != INVALID_MAG):
            v = v_form_GaiaG(g, bp, rp)
            comment = "V from G= " + "%.3f" % g + ", BP= " + "%.3f" % bp + ", RP= " + "%.3f" % rp
            output_file.write(str(t + GAIA_EPOCH) + "\t" + str(v) + "\t\tv_from_g\t" + comment + "\n")

output_file.close()

        

    