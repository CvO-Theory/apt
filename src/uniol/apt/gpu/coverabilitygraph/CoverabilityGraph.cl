#if !defined(numTransitions) || !defined(numPlaces)
#error numTransitions OR numPlaces not defined!
#define numTransitions	1
#define numPlaces	1
#endif

#define OMEGA		(-1)
#define NAN		(-2)
#define INVALID		(0xFFFFFFFF)

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

struct _Edge {
	unsigned int idTransition;
	unsigned int idTarget;
};

struct _Vertex {
	int marking[numPlaces];
	unsigned int idParent;  // of course there may be more parents in the graph, but for this one of them is enough to know.
	unsigned int idEdges;
	unsigned int numEdges;
};

struct _FireResult {
	bool isFireable;
	bool isDuplicateFireResult;
	bool isDuplicateVertex;
	int marking[numPlaces];
	unsigned int idFireResult;
	unsigned int idVertex;
};

struct _Info {
	unsigned int numMarkingsDone;
	unsigned int numMarkingsToDo;
	unsigned int writePtrEdge;
	unsigned int writePtrVertex;
	unsigned int skipNextKernelCall;
	global Edge* edges;
	global Vertex* vertices;
	global FireResult* fireResults;
	global const int* matForward;
	global const int* matBackward;
};

bool equals(global int* m1, global int* m2);

kernel void GetSizes(global unsigned int* sizes) {
	sizes[0] = sizeof(Info);
	sizes[1] = sizeof(Edge);
	sizes[2] = sizeof(Vertex);
	sizes[3] = sizeof(FireResult);
}

kernel void Init(
	global void* bufInfo,
	global void* bufEdges,
	global void* bufVertices,
	global void* bufFireResults,
	global const int* matForward,
	global const int* matBackward,
	global int* initialMarking
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

kernel void Fire(global void* bufInfo) {
	global Info* info = (global Info*)bufInfo;

	unsigned int idTransition = get_global_id(0);
	unsigned int idMarking = get_global_id(1);

	unsigned int idMatrix = idTransition*numPlaces;
	unsigned int idParent = info->numMarkingsDone + idMarking;
	unsigned int idResult = idMarking*numTransitions + idTransition;

	global const int* vecForward = &info->matForward[idMatrix];
	global const int* vecBackward = &info->matBackward[idMatrix];
	global Vertex* parent = &info->vertices[idParent];
	global FireResult* result = &info->fireResults[idResult];

	bool fireable = true;

	for(int i=0; i<numPlaces; ++i) {
		int token = parent->marking[i];
		int back = vecBackward[i];
		int forw = vecForward[i];

		if(token >= back) {
			token += forw - back;
			if(token < 0) {
				token = NAN;
			}
		} else if(token != OMEGA) {
			fireable = false;
			break;
		}

		result->marking[i] = token;
	}

	result->isFireable = fireable;
	result->isDuplicateFireResult = false;
	result->isDuplicateVertex = false;
	result->idFireResult = INVALID;
	result->idVertex = INVALID;
}

kernel void CheckCover(global void* bufInfo) {
	Info info = *((global Info*)bufInfo);

	unsigned int idTransition = get_global_id(0);
	unsigned int idMarking = get_global_id(1);

	unsigned int idResult = idMarking*numTransitions + idTransition;
	global FireResult* result = &info.fireResults[idResult];
	if(!result->isFireable)
		return;

	global int* m1 = result->marking;
	
	unsigned int idParent = info.numMarkingsDone + idMarking;
	while(idParent != INVALID) {
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

kernel void CheckCallDedupeFireResults(global void* bufInfo, unsigned int idResult1) {
	global Info* info = (global Info*)bufInfo;

	global FireResult* result1 = &info->fireResults[idResult1];
	if(!result1->isFireable || result1->isDuplicateFireResult)
		info->skipNextKernelCall = 1;
	else
		info->skipNextKernelCall = 0;
}	

kernel void DedupeFireResults(global void* bufInfo, unsigned int idResult1) {
	global Info* info = (global Info*)bufInfo;

	unsigned int idResult2 = get_global_id(0);
	if(idResult2 >= (info->numMarkingsToDo*numTransitions))
		return;

	global FireResult* result1 = &info->fireResults[idResult1];
	if(!result1->isFireable || result1->isDuplicateFireResult)
		return;

	global FireResult* result2 = &info->fireResults[idResult2];
	if(!result2->isFireable)
		return;

	global int* m1 = result1->marking;
	global int* m2 = result2->marking;

	if(equals(m1,m2)) {
		result2->isDuplicateFireResult = true;
		result2->idFireResult = idResult1;
	}
}

kernel void DedupeVertices(global void* bufInfo) {
	global Info* info = (global Info*)bufInfo;

	unsigned int idResult = get_global_id(0);
	unsigned int idVertex = get_global_id(1);

	if((idResult >= (info->numMarkingsToDo*numTransitions)) || (idVertex >= info->writePtrVertex))
		return;

	global FireResult* result = &info->fireResults[idResult];
	if(!result->isFireable || result->isDuplicateFireResult || result->isDuplicateVertex)
		return;

	global Vertex* vertex = &info->vertices[idVertex];

	global int* m1 = vertex->marking;
	global int* m2 = result->marking;

	if(equals(m1,m2)) { // since input and output are both deduped, this will if ever only occure once
		result->isDuplicateVertex = true;
		result->idVertex = idVertex;
	}
}

bool equals(global int* m1, global int* m2) {
	bool equal = true;
	for(int i=0; i<numPlaces; ++i) {
		if(m1[i] != m2[i])
			equal = false;
	}
	return equal;
}

kernel void Convert(global void* bufInfo) {
	Info info = *((global Info*)bufInfo);

	unsigned int numMarkingsToDo = 0;

	for(unsigned int idMarking = 0; idMarking < info.numMarkingsToDo; ++idMarking) {
		unsigned int idParent = info.numMarkingsDone + idMarking;
		global Vertex* parent = &info.vertices[idParent];
		parent->idEdges = info.writePtrEdge;

		unsigned int numEdges = 0;
		for(unsigned int idTransition = 0; idTransition < numTransitions; ++idTransition) {
			global FireResult* result = &info.fireResults[idMarking*numTransitions + idTransition];
			if(result->isFireable) {
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
