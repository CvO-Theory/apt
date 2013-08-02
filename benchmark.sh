#!/bin/bash

NETS_LANG=`ls nets/gpu/test-lang-*-net.apt`
NETS_BIT=`ls nets/gpu/test-bit-*-net.apt`
NETS_CYCLE=`ls nets/gpu/test-cycle-*-net.apt`

TMPFILE=`basename $0`.tmp
TIME="time -o$TMPFILE -ftime\t%E"
APTGPU="sh apt.sh cl_covera"
APTCPU="sh apt.sh covera"

function procfile {
	echo `basename $1`
	echo -n "CPU "
	$TIME $APTCPU $1 1>/dev/null 2>&1
	cat $TMPFILE
	echo -n "GPU "
	$TIME $APTGPU $1 1>/dev/null 2>&1
	cat $TMPFILE
}

echo
echo "================================================================================"
echo "Benchmark startet on $(date)"
echo "================================================================================"
echo

echo "Test set: Language-nets"
echo "-----------------------"
for net in $NETS_LANG
do
	procfile $net
done
echo

echo "Test set: Bit-nets"
echo "------------------"
for net in $NETS_BIT
do
	procfile $net
done
echo

echo "Test set: Cycle-nets"
echo "--------------------"
for net in $NETS_CYCLE
do
	procfile $net
done
echo

rm $TMPFILE

