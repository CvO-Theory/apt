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
	if(	   (get_local_size(0) != numPlaces)
		|| (get_global_size(0) != numPlaces)
		|| (get_global_size(1) != numTransitions)
		|| (get_global_size(2) != numMarkingsToDo)
//		|| (get_num_groups(1) != numMarkings*numTransitions) // wieso 1 und nicht 0?
	)
		return;
*/
	int idPlace = get_local_id(0);
	int idTransition = get_global_id(1);
	int idMarking = get_global_id(2);

	int idMatrix = idTransition*numPlaces + idPlace;
	int idInMarking = (numMarkingsDone + idMarking) * numPlaces + idPlace;
	int idOutMarking = (numMarkingsDone + numMarkingsToDo + idMarking*numTransitions + idTransition) * numPlaces + idPlace;

	local bool fireable;

	// initialize fireable true and wait
	fireable = true;
	barrier(CLK_LOCAL_MEM_FENCE);

	// if not fireable, the token count at this place will be negative.
	int token = markings[idInMarking] - matBackward[idMatrix];
	if(token < 0)
		fireable = false;
	barrier(CLK_LOCAL_MEM_FENCE);

	if(fireable)
		markings[idOutMarking] = token + matForward[idMatrix];
	else
		markings[idOutMarking] = -1;
}

kernel void Dedupe(global int* markings, int numMarkingsDone, int numMarkingsToDo) {
/*
	// check asssertions
	if(        (get_local_size(0) != numPlaces)
		|| (get_global_size(0) != numPlaces)
//		|| (get_global_size(1) != (numMarkingsDone+numMarkingsToDo+numMarkingsToDo*numTransitions))
		|| (get_global_size(2) != (numMarkingsToDo*numTransitions))
	)
		return;
*/
	int idPlace = get_local_id(0);
	int idMarkingDone = get_global_id(1) * numPlaces + idPlace;
	int idMarkingToDo = (numMarkingsDone+numMarkingsToDo+get_global_id(2)) * numPlaces + idPlace;

	if(idMarkingDone >= idMarkingToDo)
		return;

	local bool equal;

	equal = true;
	barrier(CLK_LOCAL_MEM_FENCE);

	if(markings[idMarkingDone] != markings[idMarkingToDo])
		equal = false;
	barrier(CLK_LOCAL_MEM_FENCE);

	if(equal)
		markings[idMarkingToDo] = -1;
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
