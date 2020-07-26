// Windowed Fourier analysis of T UMi

objName = "T UMi"
startJD = 2420000

alert("foo")

i = 1
while (i <= 15) {
    endJD = startJD+2000
    vstar.loadFromAID(objName, startJD, endJD)

    periods = vstar.dcdftPeriod("Visual", 100, 500, 1)

    err = vstar.getError()
    if (err == null) {
        averageJD = (startJD + endJD)/2
        print(averageJD + "," + periods[0])
    } else {
        print(err)
    }

    startJD = startJD+1900
    i = i+1
}
