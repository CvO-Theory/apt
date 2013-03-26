#!/bin/bash

# author Daniel

#################
# CONFIGURATION #
#################

# RUN CONFIGURATION
numberOfRuns=15
maxAttributes=3
maxTimePerRunInS=5

# SET GENERATOR
generator=chance

# GET ACTUAL TIMESTAMP
timestamp=$(date +"%Y-%m-%d_%H-%M-%S")

# FILENAMES
logFolder=log/

destinationFilename=check_test_log_${timestamp}.html
destinationFile=${logFolder}${destinationFilename}

testNetsPrefix=check_test_net_${timestamp}_
testNetsPrefixWithFolder=$logFolder$testNetsPrefix
testNetsPostfix=.apt

# HTML TITLE
htmlTitle="Check Test from ${timestamp}"

# JAVA CALL
javaCall="java"

# LOCATION OF APT.JAR
aptJarLocation="./apt.jar"

########################
# END OF CONFIGURATION #
########################

# CREATE LOG FOLDER
mkdir -p $logFolder

# HTML HEADER
echo "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" > $destinationFile
echo "<html>" >> $destinationFile
echo "  <head>" >> $destinationFile
echo "    <title>${htmlTitle}</title>" >> $destinationFile
echo "  </head>" >> $destinationFile
echo "  <body>" >> $destinationFile

# NAMES OF ATTRIBUTES FROM CHECK MODULE
possibleAttributesCheck=(snet !snet tnet !tnet bounded !bounded freeChoice !freeChoice isolated !isolated 2-marking 3-marking !2-marking !3-marking persistent !persistent plain !plain pure !pure reversible !reversible stronglyLive !stronglyLive !strongly_2-separable !strongly_3-separable !weakly_2-separable !weakly_3-separable)

# NAMES OF MATCHING APT MODULES
possibleAttributesAPT=(snet snet tnet tnet bounded bounded fc fc isolated isolated k_marking k_marking k_marking k_marking persistent persistent plain plain pure pure reversible reversible strongly_live strongly_live strong_separation_length-2 strong_separation_length-3 weak_separation_length-2 weak_separation_length-3)

# HEAD
echo "    <h1>${htmlTitle}</h1>" >> $destinationFile

# LOOP FOR MULTIPLE TEST RUNS
i=1
while [ $i -le $numberOfRuns ]
do
	# RANDOM: HOW MANY ATTRIBUTES WILL BE USED IN THIS TEST RUN
	numberOfAttributes=$((($RANDOM % $maxAttributes) + 1))

	# INIT VARIABLES FOR TEST RUN
	j=1
	attributesCheck=()
	attributesAPT=()

	# RANDOM: CHOOSE ATTRIBUTES
	while [ $j -le $numberOfAttributes ]
	do
		index=$(($RANDOM % ${#possibleAttributesCheck[*]}))
		attributesCheck+=(${possibleAttributesCheck[$index]})
		attributesAPT+=(${possibleAttributesAPT[$index]})
		((j++))
	done

	echo "      <hr>" >> $destinationFile
	echo "      <h2>Call to generate net:</h2>" >> $destinationFile

	# CALL CHECK MODULE
	${javaCall} -jar ${aptJarLocation} check ${maxTimePerRunInS} ${generator} ${attributesCheck[*]} > ${testNetsPrefixWithFolder}${i}${testNetsPostfix}
	echo "      <pre>${javaCall} -jar ${aptJarLocation} check ${maxTimePerRunInS} ${generator} ${attributesCheck[*]} > ${testNetsPrefixWithFolder}${i}${testNetsPostfix}</pre>" >> $destinationFile

	# FIND OUT IF A NET WAS FOUND
	noNetFound=$(cat ${testNetsPrefixWithFolder}${i}${testNetsPostfix} | grep "no net was found")

	if [ -n "$noNetFound" ]
	then # NO NET FOUND
		echo "      <h2>$noNetFound</h2>" >> $destinationFile
		echo "      <h2>Required attributes: <font color=\"#008000\"><i>${attributesCheck[*]}</i></font></h2>" >> $destinationFile
	else # NET FOUND
		echo "      <h2>New petri-net created. (<a href=\"${testNetsPrefix}${i}${testNetsPostfix}\">Show net...</a>)</h2>" >> $destinationFile
		echo "      <h2>Required attributes: <font color=\"#008000\"><i>${attributesCheck[*]}</i></font></h2>" >> $destinationFile
		echo "      <p>Petri-net will be checked...</p>" >> $destinationFile

		# ANALYSE EVERY ATTRIBUTE
		for attributeToCheck in ${attributesAPT[*]}
		do
			if [[ "$attributeToCheck" == *separation* ]]
			then # IF SEPARATION IS ATTRIBUTE -> SET K
				k=$(echo $attributeToCheck | cut -d"-" -f2)
				callPostfix="5 $k"
				attributeToCheck=$(echo $attributeToCheck | cut -d"-" -f1)
			else # NOT SEPARATION -> NO K NEEDED
				callPostfix=""
			fi

			# CALL APT MODULE TO ANALYSE NET
			echo "      <h3>${javaCall} -jar ${aptJarLocation} $attributeToCheck ${testNetsPrefixWithFolder}${i}${testNetsPostfix} ${callPostfix}</h3>" >> $destinationFile
			echo "      <pre>" >> $destinationFile
			${javaCall} -jar ${aptJarLocation} $attributeToCheck ${testNetsPrefixWithFolder}${i}${testNetsPostfix} $callPostfix >> $destinationFile 2>&1
			echo "      </pre>" >> $destinationFile
		done
	fi

	((i++))
done

# END OF HTML FILE
echo "  </body>" >> $destinationFile
echo "</html>" >> $destinationFile

# vim: ft=c:noet:sw=8:sts=8:ts=8:tw=120
