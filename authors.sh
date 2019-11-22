#!/bin/bash
# @author Manuel Gieseking

if [ "$1" = "listAuthors" ]; then
	# collects all occuring names behind @author in the files
	declare -A authors

	# for all files containing an @author tag (apart from this file)
	while read -r line ; do
	#	IFS=' ' read -ra ADDR <<< "$line"
	#	file=$(echo "${ADDR[0]}" | cut -d':' -f 1)
		file=${line%:*}
		fileAuthors=${line#*@author}
		IFS=',' read -ra AUTHOR <<< "$fileAuthors"	
		for i in "${AUTHOR[@]}"; do
			authors[$i]="in"
		done
	done < <(grep -r "@author" . --exclude='authors.sh')

	# print list of occuring authors
	for author in "${!authors[@]}"; do	
		echo "Author:""$author"
	done

	## This was the output from which I manually created the following authors array which combines author names 
	## belonging to the same author.
	#Author: Daniel
	#Author: Thomas Strathmann
	#Author: Jonas Prellberg
	#Author: Sören
	#Author:: vsp
	#Author: Dennis Borde
	#Author: Renke Grunwald */
	#Author: Vincent Göbel */
	#Author: Manuel
	#Author: Uli Schlachter
	#Author: Uli Schlachter */
	#Author: Daniel (just added "withMarks")
	#Author: Renke Grunwald
	#Author: Raffaela Ferrari */
	#Author: Bjoern von der Linde
	#Author: Sören Dierkes
	#Author: Vincent Göbel
	#Author: CS
	#Author: Raffaela Ferrari
	#Author: Valentin
	#Author: vsp */
	#Author: Florian Hinz
	#Author: Dennis-Michael Borde
	#Author: daniel
	#Author: Manuel Gieseking
	#Author: Chris
	#Author: vsp
	#Author: Maike Schwammberger
	#Author: Björn von der Linde
	#Author: Vincent
else 
	declare -A authorsSqueezed
	authorsSqueezed["Daniel|daniel"]=0
	authorsSqueezed["Jonas Prellberg"]=0
	authorsSqueezed["Sören|Sören Dierkes"]=0
	authorsSqueezed["vsp|Valentin"]=0
	authorsSqueezed["Dennis Borde|Dennis-Michael Borde"]=0
	authorsSqueezed["Renke Grunwald"]=0
	authorsSqueezed["Vincent Göbel|Vincent"]=0
	authorsSqueezed["Manuel|Manuel Gieseking"]=0
	authorsSqueezed["Uli Schlachter"]=0
	authorsSqueezed["Raffaela Ferrari"]=0
	authorsSqueezed["Bjoern von der Linde|Björn von der Linde"]=0
	authorsSqueezed["CS|Chris"]=0
	authorsSqueezed["Florian Hinz"]=0
	authorsSqueezed["Maike Schwammberger"]=0

	# count the lines for each author
	for author in "${!authorsSqueezed[@]}"; do	
		authorLines=0;	
#		echo $author
		while read -r line ; do		
#			echo "LINE:$line"
			file="${line%%:*}"
			IFS=',' read -ra AUTHORS <<< "$line"
			lines=$(wc -l < $file)
			authorLines=$(($authorLines+($lines/${#AUTHORS[@]})))
		done < <(grep -E -i -w -r "author.*$author" . --exclude='authors.sh')
		authorsSqueezed[$author]=$authorLines
	done

	# print list of occuring authors
	for author in "${!authorsSqueezed[@]}"; do	
		echo "$author: ${authorsSqueezed[$author]}"
	done

	## just collected
	#Renke Grunwald: 7117
	#Daniel|daniel: 6901
	#Sören|Sören Dierkes: 1745
	#Raffaela Ferrari: 2043
	#Bjoern von der Linde|Björn von der Linde: 1078
	#Florian Hinz: 1036
	#Vincent Göbel|Vincent: 2423
	#vsp|Valentin: 20885
	#Maike Schwammberger: 2392
	#CS|Chris: 5348
	#Manuel|Manuel Gieseking: 13296
	#Dennis Borde|Dennis-Michael Borde: 5708
	#Jonas Prellberg: 4808
	#Uli Schlachter: 48472

	## divided by the number of authors of the file
	#Renke Grunwald: 6390
	#Daniel|daniel: 6264
	#Sören|Sören Dierkes: 1351
	#Raffaela Ferrari: 1847
	#Bjoern von der Linde|Björn von der Linde: 940
	#Florian Hinz: 344
	#Vincent Göbel|Vincent: 1454
	#vsp|Valentin: 15608
	#Maike Schwammberger: 1434
	#CS|Chris: 4420
	#Manuel|Manuel Gieseking: 9523
	#Dennis Borde|Dennis-Michael Borde: 2891
	#Jonas Prellberg: 4644
	#Uli Schlachter: 41385
fi
