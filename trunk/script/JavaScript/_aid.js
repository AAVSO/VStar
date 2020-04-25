with (vstar) {
  root = "/Users/david/tmp/"

  objs = ["RS Oph", "T Pyx"]

  for (i=0;i<objs.length;i++) {
    obj = objs[i]

    println("Loading " + obj)
    loadFromAID(obj, 2454852, 2455852)

    path = root + obj.replace(" ", "_") + "_2454852_2455852.tsv"
    println("Saving " + path)
    saveCurrentData(path, "\t")
  }

  exit()
}

