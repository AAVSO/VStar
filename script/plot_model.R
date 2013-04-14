# This script plots observations and a model as data points and as a continuous 
# function fit.
#
# dbenn@computer.org, April 2013

# Make sure you set the working directory (via setwd()) to where the CSV files are.

# Read observation data.
dataXY = read.table("data.csv", sep=",")
dataX = unlist(dataXY[1])
dataY = unlist(dataXY[2])

# Observation data scatterplot.
plot(dataX, dataY, col="black", xlab="JD", ylab="Magnitude", ylim=rev(range(dataY)))

# Read the model data points.
modelXY = read.table("model.csv", sep=",")
modelX = modelXY[,1]
modelY = modelXY[,2]
  
# Replace model <- ... with model function created by VStar.
# This can be obtained from VStar's Analysis menu, Models...dialog, Show Model button.
# 
#model <- function(t) 13.09626234+
#+ -0.38209200 * cos(2*pi*0.00508174*(t-2454842.0))+0.11280535 * sin(2*pi*0.00508174*(t-2454842.0))  

# Plot model points and continuous function.

# Model data scatterplot.
points(modelX, modelY, col="red")

# Model function line plot.
# The JD range can be taken from model or observation data.
# Change the increment argument to increase or decrease
# the resolution of the line plot.
modelJDs = seq(modelX[1], modelX[length(modelX)], 0.1)
jds = modelJDs

lines(jds, model(jds), col="blue") # original
