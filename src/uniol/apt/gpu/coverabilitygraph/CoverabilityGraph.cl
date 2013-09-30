// Device program to calculate the coverability graph of a given Petri net.
// Author: Dennis-Michael Borde

// these definitions must be set in host program before compiling the device program.
// recompilation must be done, if another Petri net should be processed.
#if !defined(numTransitions) || !defined(numPlaces)
#error numTransitions OR numPlaces not defined!
#define numTransitions	1
#define numPlaces	1
#endif

// These definitions must be consistent to the host program.
#define OMEGA		(-1)
#define NOTANUMBER	(-2)
#define INVALID		(0xFFFFFFFF)

// These defines are for backward compatibility
#ifndef kernel
#define kernel __kernel
#endif

#ifndef constant
#define constant __constant
#endif

#ifndef local
#define local __local
#endif

#ifndef global
#define global __global
#endif

typedef struct _Edge Edge;
typedef struct _Vertex Vertex;
typedef struct _FireResult FireResult;
typedef struct _Info Info;

// holds an edge of the coverability graph. Edges are managed in an adjacency list, thus the source is implied by the index.
struct _Edge {
	unsigned int idTransition; // the transition fired (label). 
	unsigned int idTarget; // the vertex id of the target.
};

// holds a vertex of the coverability graph.
struct _Vertex {
	int marking[numPlaces];
	unsigned int idParent;  // of course there may be more parents in the graph, but here one of them is enough to know.
	unsigned int idEdges;
	unsigned int numEdges;
};

// holds a result item of the fire kernel. Source marking and transition are implied by the index.
struct _FireResult {
	bool isFireable; // was the transition fireable?
	bool isDuplicateFireResult; // is there a duplicate among the other fire results?
	bool isDuplicateVertex; // is there a duplicate among the vertices in the graph?
	int marking[numPlaces]; // the new marking, only valid if isFireable==true.
	unsigned int idFireResult; // an index to the duplicate in the fire results or INVALID.
	unsigned int idVertex; // an index to the duplicate in the vertices or INVALID.
};

// holds global information about the calculation process. 
struct _Info {
	unsigned int numMarkingsDone; // how many vertices are visited?
	unsigned int numMarkingsToDo; // how many vertices are unvisited?
	unsigned int writePtrEdge; // where to put the next edge to.
	unsigned int writePtrVertex; // where to put the next vertex to.
	unsigned int skipNextKernelCall; // true, if the next call of a kernel may be skiped.
	global Edge* edges; // pointer to the edges buffer.
	global Vertex* vertices; // pointer to the vertices buffer.
	global FireResult* fireResults; // pointer to the fireresults buffer.
	global const int* matForward; // the forward matrix of the Petri net (column first).
	global const int* matBackward; // the backward matrix of the Petri net (column first).
};

bool equals(global int* m1, global int* m2);

// returns the sizes of the structs, so the host program is able to calculate correct buffer sizes.
kernel void GetSizes(global unsigned int* sizes) {
	sizes[0] = sizeof(Info);
	sizes[1] = sizeof(Edge);
	sizes[2] = sizeof(Vertex);
	sizes[3] = sizeof(FireResult);
}

// setup global information for the calculation process.
kernel void Init(
	global void* bufInfo, // buffer to store the global information
	global void* bufEdges, // buffer to store edges
	global void* bufVertices, // buffer to store vertices
	global void* bufFireResults, // buffer to store fireresults
	global const int* matForward, // the forward matrix of the Petri net (column first)
	global const int* matBackward, // the backward matrix of the Petri net (column first)
	global int* initialMarking // the first vertex as a marking (a field of numPlaces integer values).
) {
	global Info* info = (global Info*)bufInfo;
	global Edge* edges = (global Edge*)bufEdges;
	global Vertex* vertices = (global Vertex*)bufVertices;
	global FireResult* fireResults = (global FireResult*)bufFireResults;
	// initialize info structure
	info->numMarkingsDone = 0;
	info->numMarkingsToDo = 1;
	info->writePtrEdge = 0;
	info->writePtrVertex = 1;
	info->edges = edges;
	info->vertices = vertices;
	info->fireResults = fireResults;
	info->matForward = matForward;
	info->matBackward = matBackward;
	info->skipNextKernelCall = 0;
	// initialize first vertex
	vertices[0].idParent = INVALID;
	vertices[0].idEdges = INVALID;
	vertices[0].numEdges = 0;
	for(int i=0; i<numPlaces; ++i) {
		vertices[0].marking[i] = initialMarking[i];
	}
}

// if buffers have changed, global information must be updated (see Init).
kernel void Update(
	global void* bufInfo,
	global void* bufEdges,
	global void* bufVertices,
	global void* bufFireResults
) {
	global Info* info = (global Info*)bufInfo;
	global Edge* edges = (global Edge*)bufEdges;
	global Vertex* vertices = (global Vertex*)bufVertices;
	global FireResult* fireResults = (global FireResult*)bufFireResults;
	info->edges = edges;
	info->vertices = vertices;
	info->fireResults = fireResults;
}

// fires all transitions for all unvisited vertices and stores the result as a fireresult.
kernel void Fire(global void* bufInfo) {
	global Info* info = (global Info*)bufInfo;

	// what unvisited vertex and what transition?
	unsigned int idTransition = get_global_id(0);
	unsigned int idVertex = get_global_id(1);

	// get column of forward and backward matrix.
	unsigned int idMatrix = idTransition*numPlaces;
	global const int* vecForward = &info->matForward[idMatrix];
	global const int* vecBackward = &info->matBackward[idMatrix];

	// calc pointer to the vertex and to the position to store the fireresult.
	unsigned int idParent = info->numMarkingsDone + idVertex;
	unsigned int idResult = idVertex*numTransitions + idTransition;
	global Vertex* parent = &info->vertices[idParent];
	global FireResult* result = &info->fireResults[idResult];

	bool fireable = true; // assume, transition is fireable.

	for(int i=0; i<numPlaces; ++i) { // for each place .. 
		int token = parent->marking[i]; // .. get the tokens,
		int back = vecBackward[i]; // .. how many to toss,
		int forw = vecForward[i]; // .. how many to add,

		if(token >= back) { // if there are enough to toss (remember OMEGA is defined negative here)
			token += forw - back; // .. calculate the new count of tokens.
			if(token < 0) { // check for overflow
				token = NOTANUMBER;
			}
		} else if(token != OMEGA) { // if there arent enough tokens, it may be OMEGA, since it is defined negative here. 
			fireable = false; // it is not OMEGA, so the transition is not fireable!
			break; // we can stop here.
		}

		result->marking[i] = token; // save the new token count in the marking.
	}
	// init the rest of the fireresult.
	result->isFireable = fireable;
	result->isDuplicateFireResult = false;
	result->isDuplicateVertex = false;
	result->idFireResult = INVALID;
	result->idVertex = INVALID;
}

// Check, whether the fireresults cover a known marking on its path from M_0 and change the tokens accordingly.
kernel void CheckCover(global void* bufInfo) {
	Info info = *((global Info*)bufInfo);

	// get vertex and transition
	unsigned int idTransition = get_global_id(0);
	unsigned int idVertex = get_global_id(1);

	// calculate the pointer to the fireresult.
	unsigned int idResult = idVertex*numTransitions + idTransition;
	global FireResult* result = &info.fireResults[idResult];
	if(!result->isFireable) // if the transition wasnt activated, we can stop here.
		return;

	global int* m1 = result->marking; // get the calculated marking of the fireresult.
	
	// let's start from the parent and go to M0.
	unsigned int idParent = info.numMarkingsDone + idVertex;
	while(idParent != INVALID) { // the special about M0 is, it has no parent.
		global Vertex* parent = &info.vertices[idParent];
		global int* m2 = parent->marking;
		bool cover = true;
		bool equal = true;
		// compare m1 and m2
		for(int i=0; i<numPlaces; ++i) {
			// m1 covers m2 iff for all palces i m1[i] >= m2[i] and there is a place i s.t. m1[i] > m2[i].
			if(m1[i] < m2[i])
				cover = false;
			if(m1[i] != m2[i])
				equal = false;
		}
		// if m1 covers m2 ...
		if(cover && !equal) {
			// ... set the OMEGA tokens in m1 where appropriate and stop.
			for(int i=0; i<numPlaces; ++i) {
				if(m1[i] != m2[i])
					m1[i] = OMEGA;
			}
			break;
		} else {
			// ... continue with the parents parent.
			idParent = parent->idParent;
		}
	}
}

// checks, wheter a call to DedupeFireResults is needed. It is not,
// if the results transition wasnt activated or if the result is a duplicate itself.
kernel void CheckCallDedupeFireResults(global void* bufInfo, unsigned int idResult1) {
	global Info* info = (global Info*)bufInfo;

	global FireResult* result1 = &info->fireResults[idResult1];
	if(!result1->isFireable || result1->isDuplicateFireResult)
		info->skipNextKernelCall = 1;
	else
		info->skipNextKernelCall = 0;
}	

// let's find duplicates among the fireresults. This is done successive (see host program).
kernel void DedupeFireResults(global void* bufInfo, unsigned int idResult1) {
	global Info* info = (global Info*)bufInfo;

	// get the other fireresult id and check whether it is in bounds.
	unsigned int idResult2 = get_global_id(0);
	if(idResult2 >= (info->numMarkingsToDo*numTransitions))
		return;

	// get the results
	global FireResult* result1 = &info->fireResults[idResult1];
//	if(!result1->isFireable || result1->isDuplicateFireResult)
//		return;

	global FireResult* result2 = &info->fireResults[idResult2];
	if(!result2->isFireable) // if the results transitions wasnt activated, we can stop here.
		return;

	// get the markings of the results
	global int* m1 = result1->marking;
	global int* m2 = result2->marking;
	// .. and compare them.
	if(equals(m1,m2)) { // if they are equal, the second is a duplicate.
		result2->isDuplicateFireResult = true;
		result2->idFireResult = idResult1;
	}
}

// let's find duplicates among the vertices. Since the results are a disjunction and the vertices are a disjunction, this will happen, if ever, once for each result.
kernel void DedupeVertices(global void* bufInfo) {
	global Info* info = (global Info*)bufInfo;

	// get the ids of the result and vertex.
	unsigned int idResult = get_global_id(0);
	unsigned int idVertex = get_global_id(1);
	// and check boundings.
	if((idResult >= (info->numMarkingsToDo*numTransitions)) || (idVertex >= info->writePtrVertex))
		return;

	// get the result and check whether it is valid.
	global FireResult* result = &info->fireResults[idResult];
	if(!result->isFireable || result->isDuplicateFireResult || result->isDuplicateVertex)
		return;
	// get the vertex.
	global Vertex* vertex = &info->vertices[idVertex];
	// get the markings.
	global int* m1 = vertex->marking;
	global int* m2 = result->marking;
	// check, whether they are equal. if so, the fireresult is a duplicate.
	if(equals(m1,m2)) { // since vertices and fireresults are both deduped, this will if ever only occure once
		result->isDuplicateVertex = true;
		result->idVertex = idVertex;
	}
}

// compare two markings.
bool equals(global int* m1, global int* m2) {
	bool equal = true;
	for(int i=0; i<numPlaces; ++i) {
		if(m1[i] != m2[i])
			equal = false;
	}
	return equal;
}

// convert the fireresults to vertices.
kernel void Convert(global void* bufInfo) {
	Info info = *((global Info*)bufInfo);

	unsigned int numMarkingsToDo = 0; // count the new (unvisited) vertices.
	// for all new markings ...
	for(unsigned int idMarking = 0; idMarking < info.numMarkingsToDo; ++idMarking) {
		// get the parent vertex and set the edges pointer.
		unsigned int idParent = info.numMarkingsDone + idMarking;
		global Vertex* parent = &info.vertices[idParent];
		parent->idEdges = info.writePtrEdge;

		// create the edges and targets. for all transitions ...
		unsigned int numEdges = 0;
		for(unsigned int idTransition = 0; idTransition < numTransitions; ++idTransition) {
			// get the result
			global FireResult* result = &info.fireResults[idMarking*numTransitions + idTransition];
			if(result->isFireable) { // if its transition wasnt activated, we can skip this.
				// get the target vertex. if it is a duplicate, take the stored id, else create a new one.
				unsigned int idVertex;
				if(result->isDuplicateVertex) {
					idVertex = result->idVertex;
				} else if(result->isDuplicateFireResult) {
					idVertex = info.fireResults[result->idFireResult].idVertex; // see below
				} else {
					idVertex = info.writePtrVertex++;
					global Vertex* vertex = &info.vertices[idVertex];
					for(int i=0; i<numPlaces; ++i)
						vertex->marking[i] = result->marking[i];
					vertex->idParent = idParent;
					vertex->idEdges = INVALID;
					vertex->numEdges = 0;
					result->idVertex = idVertex; // set idVertex so it can be used for FireResults that are duplicates of this one.
					++numMarkingsToDo;
				}
				// create the edge
				unsigned int idEdge = info.writePtrEdge++;
				global Edge* edge = &info.edges[idEdge];
				edge->idTransition = idTransition;
				edge->idTarget = idVertex;
				++numEdges;
			}
		}
		parent->numEdges = numEdges; 
	}

	info.numMarkingsDone += info.numMarkingsToDo;
	info.numMarkingsToDo = numMarkingsToDo;

	*((global Info*)bufInfo) = info;
}
