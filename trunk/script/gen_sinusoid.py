from math import sin, cos, radians
from random import random, seed
from time import time

# Generate a pseudo-random sinusoid in VStar's simple format.

seed(time())

for x in range(0,360):
    theta = radians(x)
    
    if random() > 0.5:
        obscode = 'BDJB'
    else:
        obscode = 'BSJ'

    #print("{},{},{},{}".format(2445000+x, 
    #                      10+sin(theta)+cos(theta)+random()/4, random()/10,
    #                      obscode))
    print("{},{},{},{}".format(2445000+x, 
                          sin(theta), 0,
                          obscode))
