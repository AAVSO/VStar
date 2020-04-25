startjd = 2456628
endjd = 2456993
vstar.loadFromFile("/Users/david/_cs2_data/5star/lx_cyg_2439800_2447500.csv")
series_str = vstar.getSeries()
err = vstar.getError()
print(err == null)
print(series_str)
series = series_str.split(",")
print(series.length)
print(series[0])
