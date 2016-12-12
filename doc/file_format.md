File format
===========

The APT tool works with Petri nets and labeled transition systems. Obviously,
all of these are finite, because describing infinite objects in files can be a
little hard. :-)

Our Petri nets are place/transition nets with arbitrary (non-negative!) arc
weight and an initial marking. Transitions can be given a label which is used
for describing a language. If a transition is not explicitly labeled, it gets
its unique identifier as a label. "Silent transitions", that is transitions
which don't generate any symbol when fired, are not supported.

Labeled transition systems consist of states and arcs between states. One of the
states is the system's initial state. Arcs get a label, too.

The actual file format consists of multiple sections. For labeled transition
systems, the file first describes the states and transitions and then the arcs
between the states. For this, the source node, transition and target node of an
arc are listed in a single line.

Petri nets also begin with a list of places and transitions. Then, for each
transition the flows are described. These are the preset and postset of the
transition. These sets are multiset and thus can contain an entry more than
once.

Altogether, the file formats are, hopefully, simple and easy to read. Just look
at some of the sample files in the nets/ directory to get started! For a formal
grammar and a more detailed description, take a look into [APT.pdf](APT.pdf).

Of course there are also formal definitions of the properties analyzed in our
tool. Many of these can be found in the document APT.pdf in ./doc/.
