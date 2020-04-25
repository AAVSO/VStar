root = "/Users/david/tmp/"
objs = ["eta Aql", "bet Lyr"]
//objs = ["bet Lyr"]

//periods = [7.176641, 12.94061713]
//epochs = [2436084.656, 2455434.8702]

startJD = 2455821.5
endJD  = 2456552.5
epoch  = (startJD + endJD)/2

for (i=0;i<objs.length;i++) {
   obj = objs[i]
   vstar.loadFromAID(obj, startJD, endJD)

   //freqs = vstar.dcdftStandardScan("Visual")
   //freqs = vstar.dcdftFrequency("Visual", 0.05, 1.0, 0.001)
   periods = vstar.dcdftPeriod("Visual", 1.0, 20.0, 0.001)

   if (vstar.getError() == null) {
        //for (j=0;j<periods.length;j++) print(periods[j])
        //for (j=0;j<freqs.length;j++) print(freqs[j])

	vstar.phasePlot(periods[0], epoch)
	//vstar.phasePlot(1.0/freqs[0], epoch)
	vstar.phasePlotMode()

	path = root + obj.replace(" ", "_") + "phase_plot.png"
	vstar.saveLightCurve(path, 600, 400)
   }
}
