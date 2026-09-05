objs = ["L2 Pup"]

for (i=0;i<objs.length;i++) {
    obj = objs[i]
    info = vstar.getStarInfo(obj)
    if (vstar.getError() == null) {
        print(info.getPeriod())
        print(info.getEpoch() == null)
    }
}


