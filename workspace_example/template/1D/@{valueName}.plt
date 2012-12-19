#/usr/bin/gnuplot
set title "${valueName}"
plot "${valueName}.dat" using 1:2
pause -1