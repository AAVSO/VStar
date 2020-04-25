// Loads specified objects from AID, carries out WWZ and
// generates phase plots for each period returned by the
// time-period analysis.

root = "/Users/david/tmp/L2PupWWZ/"

objs = ["L2 Pup"]

startJD = 2410000.0
endJD   = 2456552.5

for (i=0;i<objs.length;i++) {
    obj = objs[i]

    info = vstar.getStarInfo(obj)
    if (vstar.getError() != null) {
        break
    }

    vstar.loadFromAID(obj, startJD, endJD)

    results = vstar.wwzPeriod("Visual", 100.0, 200.0, 0.5, 0.001, 50)

    if (vstar.getError() == null) {
        //for (j=0;j<results.length;j++) {
        for (j=0;j<2;j++) {
    	    t = results[j][0]
	    p = results[j][1]
            epoch = info.getEpoch()
            if (epoch == null) {
                epoch = (startJD + endJD)/2
            }
	    print(t + "," + p)
            vstar.phasePlot(p, epoch)
            vstar.phasePlotMode()
            path = root + obj.replace(" ", "_") + t+":"+p + "-phase_plot.png"
            vstar.saveLightCurve(path, 600, 400)
        }
    }
}


