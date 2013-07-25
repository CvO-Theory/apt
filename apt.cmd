@echo off
set /a timerstart=((1%time:~0,2%-100)*60*60*100)+((1%time:~3,2%-100)*60*100)+((1%time:~6,2%-100)*100)+(1%time:~9,2%-100)

java -jar apt.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

set /a timerstop=((1%time:~0,2%-100)*60*60*100)+((1%time:~3,2%-100)*60*100)+((1%time:~6,2%-100)*100)+(1%time:~9,2%-100)

set /a timemilis=(%timerstop%-%timerstart%)
set /a timeseks=(%timerstop%-%timerstart%)/100
set /a timemins=(%timerstop%-%timerstart%)/6000

echo.
echo time	%timemins%m%timeseks%.%timemilis%s 1>&2


