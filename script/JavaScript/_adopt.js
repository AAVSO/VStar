root = "/Users/david/Desktop/adopt-a-star/"
adoptees = ['Landolt', 'Landolt']
objs = ['SU Tau', 'V0348 Sgr']
startjd = 2456779
endjd = 2457144
for (i=0;i<objs.length;i++) {
  obj = objs[i]
  adoptee = adoptees[i]
  vstar.lightCurveMode()
  vstar.loadFromAID(obj, startjd, endjd)
  if (vstar.getError() == null) {
    vstar.makeVisible("Johnson B")
    vstar.makeVisible("Cousins R")
    vstar.makeVisible("Cousins I")
    path = root + adoptee + "-" + obj + "_plot.png"
    vstar.saveLightCurve(path, 600, 400)
  }
}
vstar.exit()
