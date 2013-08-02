@echo off
if not defined TMPFILE ( set TMPFILE="CON" )

set /a timerstart = ((1%time:~0,2%-100)*60*60*100)+((1%time:~3,2%-100)*60*100)+((1%time:~6,2%-100)*100)+(1%time:~9,2%-100)

java -Xmx2g -jar apt.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

set /a timerstop=((1%time:~0,2%-100)*60*60*100)+((1%time:~3,2%-100)*60*100)+((1%time:~6,2%-100)*100)+(1%time:~9,2%-100)

set /a timediff=%timerstop%-%timerstart%
set /a timemilis=(%timediff%)%%(100)
set /a timeseks=(%timediff%/100)%%(60)
set /a timemins=(%timediff%/6000)%%(60)
set /a timehours=%timediff%/360000
              
echo.
echo time	%timehours%:%timemins%:%timeseks%.%timemilis% > %TMPFILE%
