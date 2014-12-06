#!/bin/bash

BASEDIR="$(dirname $0)"

if [ ! -f "$BASEDIR/apt.jar" ] ; then
	echo "apt.jar not found! Run 'ant jar' first!" >&2
	exit 127
fi

java -jar "$BASEDIR/apt.jar" "$@"
