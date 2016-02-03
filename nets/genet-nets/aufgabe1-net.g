# Usage:
#  -min 3
# Event-based state space encoding
# number of necessary regions: 2
.outputs b a c
.graph
p0 b(2) 
p0 a
p1 c(2) 
b p1(2) 
a p1
.marking { p0=2 }
.end
