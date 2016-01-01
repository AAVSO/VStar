# This example script does the following:
# - Loads a set of objects from AID.
# - Performs a DCDFT period search on each.
# - Creates a phase plot with the top-hit.
# - Saves the phase plot as a PNG file.

# Change this to a path of your choosing.
root = "/Users/david/tmp/"

objs = ["eta Aql", "bet Lyr"]

startJD = 2455821.5
endJD  = 2456552.5
epoch  = (startJD + endJD)/2

for (i=0;i<objs.length;i++) {
   // Load a dataset from AID.
   obj = objs[i]
   vstar.loadFromAID(obj, startJD, endJD)

   // Carry out a period search with DCDFT.
   // Uncomment the desired DCDFT mode.
   freqs = vstar.dcdftStandardScan("Visual")
   //freqs = vstar.dcdftFrequency("Visual", 0.05, 1.0, 0.001)
   //periods = vstar.dcdftPeriod("Visual", 1.0, 20.0, 0.001)

   if (vstar.getError() == null) {
       // Show top-hit periods or frequencies in console.
       // Uncomment the desired frequency or period top-hit array loop.
       //for (j=0;j<periods.length;j++) print(periods[j])
       //for (j=0;j<freqs.length;j++) print(freqs[j])

       // Create a phase plot with the top-hit.
       // Uncomment the desired frequency or period top-hit phase plot call.
       //vstar.phasePlot(periods[0], epoch)
       vstar.phasePlot(1.0/freqs[0], epoch)

       // Save the phase plot as a PNG file.
       vstar.phasePlotMode()
       path = root + obj.replace(" ", "_") + "phase_plot.png"
       vstar.saveLightCurve(path, 600, 400)
    }
}
