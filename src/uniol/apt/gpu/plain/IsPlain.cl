// Author: Dennis-Michael Borde
// checks whether a given Petri net is plain.
// result is initialized true
kernel void IsPlain(global const int* weights, global int* result, int count) {
	// get index for global data array
	int gid = get_global_id(0);

	// bound check
	if(gid >= count)
		return;

	// only write to result if it is not plain, to avoid write collisions
	if(weights[gid] != 1)
		*result = 0;
}