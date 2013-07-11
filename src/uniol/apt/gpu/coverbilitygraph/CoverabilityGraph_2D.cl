#if !defined(numTransitions) || !defined(numPlaces)
#error numTransitions OR numPlaces not defined!
#define numTransitions	1
#define numPlaces	1
#endif

kernel void Fire(
	global const int* matForward, global const int* matBackward,
	global int* markings, int numMarkingsDone, int numMarkingsToDo
) {
/*
	// check assertions
	if(	   (get_global_size(0) != numTransitions)
		|| (get_global_size(1) != numMarkingsToDo)
	)
		return;
*/
	int idTransition = get_global_id(0);
	int idMarking = get_global_id(1);

	int idMatrix = idTransition*numPlaces;
	int idInMarking = (numMarkingsDone + idMarking) * numPlaces;
	int idOutMarking = (numMarkingsDone + numMarkingsToDo + idMarking*numTransitions + idTransition) * numPlaces;

	bool fireable = true;

	// if not fireable, the token count will be negative.
	for(int i=0; i<numPlaces; ++i) {
		int token = markings[idInMarking+i] - matBackward[idMatrix+i];
		markings[idOutMarking+i] = token;
		if(token < 0) {
			fireable = false;
			break;
		}
	}

	if(fireable) {
		for(int i=0; i<numPlaces; ++i) {
			markings[idOutMarking+i] += matForward[idMatrix+i];
		}
	} else {
		for(int i=0; i<numPlaces; ++i) {
			markings[idOutMarking+i] = -1;
		}
	}
}

kernel void Dedupe(global int* markings, int numMarkingsDone, int numMarkingsToDo) {
/*
	// check asssertions
//	if(        (get_global_size(0) != (numMarkingsDone+numMarkingsToDo+numMarkingsToDo*numTransitions))
		|| (get_global_size(1) != (numMarkingsToDo*numTransitions))
	)
		return;
*/
	int idMarkingDone = get_global_id(0) * numPlaces;
	int idMarkingToDo = (numMarkingsDone+numMarkingsToDo+get_global_id(1)) * numPlaces;

	if(idMarkingDone >= idMarkingToDo)
		return;

	bool equal = true;

	for(int i=0; i<numPlaces; ++i) {
		if(markings[idMarkingDone+i] != markings[idMarkingToDo+i]) {
			equal = false;
			break;
		}
	}

	if(equal) {
		for(int i=0; i<numPlaces; ++i) {
			markings[idMarkingToDo] = -1;
		}
	}
}

kernel void Arrange(global int* outMarkingsToDo, global int* markings, int numMarkingsDone, int numMarkingsToDo) {
	global int* start = markings + (numMarkingsDone + numMarkingsToDo) * numPlaces;
	global int* end = start + numMarkingsToDo * numTransitions * numPlaces;
	global int* dst;
	global int* src;

	int count = 0;

	for(dst = start; (dst < end) && (*dst >= 0); dst += numPlaces)
		++count;

	for(src = dst+numPlaces; src < end; ) {
		if(*src >= 0) {
			++count;
			for(int i=0; i<numPlaces; ++i)
				*dst++ = *src++;
		} else {
			src += numPlaces;
		}
	}

	*outMarkingsToDo = count;
}
