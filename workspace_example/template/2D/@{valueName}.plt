#/usr/bin/gnuplot
set pm3d map
set title "${valueName}"
splot '${valueName}.dat' using 1:2:3 with points palette pt 5 ps 0.5
pause -1