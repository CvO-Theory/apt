# foo
.model d"@ä #bar
.inputs a b c d
.state   graph
s0 a s1 b s2 d s0

   
s1 c s4 b s2
s1 d s5 b s0
s5 c s3 b s0
s4 d s3

.marking {s0}

.end # This is the end
