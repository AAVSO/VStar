with (vstar) {
  loadFromFile("/Users/david/_cs2_data/5star/lx_cyg_2439800_2447500.csv") 
  pause(1000)

  println("Phase Plot...")
  phasePlotMode()
  pause(1000)

  getBands()

  println("Light Curve...")
  lightCurveMode()
  pause(1000)

  println("Bye")
  exit()
}
