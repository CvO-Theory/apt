Using APT
=========

The various analysis methods that APT contains are implemented as modules. So
first we have to figure out which modules are available. To do this, we start
the JAR file without any extra arguments. On Unix-like systems this is done via
"java -jar apt.jar":

    $ java -jar apt.jar
    Usage: apt <module> <arguments>

    Petri net
    =========
      bcf                          Check if a Petri net is behaviourally conflict free (BCF)
      bicf                         Check if a Petri net is binary conflict free (BiCF)
      bounded                      Check if a Petri net is bounded or k-bounded
      coverability_graph           Compute a Petri net's coverability graph
    [...]

Let's assume we want to check if a Petri net is bounded. But what arguments do
we have to provide? Let's figure out!

    $ java -jar apt.jar bounded
    Too few arguments

    Usage: apt bounded <pn> [<k>]
      pn         The Petri net that should be examined
      k          If given, k-boundedness is checked

    Check if a Petri net is bounded or k-bounded. A Petri net is bounded if there
    is an upper limit for the number of token on each place. It is k-bounded if
    this limit isn't bigger than k.

OK, so the bounded-module wants a Petri net as its argument. Petri nets are read
from files. The file format used is explained [here](file_format.md). A sample file that
comes bundled with this software is "nets/eb-nets/basic/pn3-net.apt":

    $ java -jar apt.jar  bounded nets/eb-nets/basic/pn3-net.apt
    bounded: No
    witness_place: s2
    witness_firing_sequence: "a;b"

This tells us that at least the place "s1" is unbounded and thus the net as a
whole is unbounded, too.

Next we want to compute a coverability graph. The "coverability_graph" module
does that for us. However, the name is quite long and we are lazy. For this
reason, we can shorten the name, as long as the result still uniquely identifies
a module. This means the following will work:

    $ java -jar apt.jar coverab nets/eb-nets/basic/pn3-net.apt
    [long output here]

The command prints the resulting LTS in the file format. That doesn't help us
much. So what can we do about this? Let's go a step back and look at the
description for the "coverability_graph" module:

    $ java -jar apt.jar coverab
    Too few arguments
    Usage: apt coverability_graph <pn> [<lts>]
      pn         The Petri net that should be examined
      lts        Optional file name for writing the output to

    Compute a Petri net's coverability graph

Aha! This module has an optional argument. This is an output argument: The
resulting LTS will be written to this file. Let's try this:

    $ java -jar apt.jar coverab nets/eb-nets/basic/pn3-net.apt lts-for-pn3-net.apt
    reachability_graph: No

The output becomes much shorter now. Also, the module tells us that the
resulting coverability graph is not a reachability graph. We already know this,
because the net is unbounded, but it is nice to know that the net didn't
suddenly become bounded. :-)

There are also other kinds of optional arguments:

    $ java -jar apt.jar weakly_live
    Too few arguments
    Usage: apt weakly_live <pn> [<transition>]
      pn         The Petri net that should be examined
      transition A transition that should be checked for liveness

    Test if a Petri net or a transition (if given) is weakly live. A transition is
    weakly live if an infinite fire sequence exists which fires this transition
    infinitely often. A Petri net is weakly live when all of its transitions are
    weakly live.

This module has an optional transition argument. We can see that it is optional,
because it is written in square brackets. This means we can either call it
without any argument for testing if the Petri net is weakly live or we can call
it with a transition to check if that transition is weakly live.

So, is the net weakly live?

    $ java -jar apt.jar weakly_live nets/eb-nets/basic/pn1b-net.apt
    weakly_live: No
    sample_counterexample_transition: t2

Nope, it isn't. What about just some specific transition. Let's check t1.

    $ java -jar apt.jar weakly_live nets/eb-nets/basic/pn1b-net.apt t1
    weakly_live: Yes

Since APT was created with scriptability in mind it can be integrated
in other programs. For example, APT calls can be connected via pipes
in most UNIX-like operating systems that provide bash or similar
shells.

Instead of generating intermediate files like in the following
example which draws the coverability graph of Petri net

    $ java -jar apt.jar coverab nets/eb-nets/basic/pn1b-net.apt coverability.apt
    reachability_graph: Yes
    $ java -jar apt.jar draw coverability.apt coverability.dot

we can combine both these calls to APT and thus avoid generating the
intermediate file "coverability.apt". In order to do this we redirect the
file output of the coverability graph module directly to the file
input of the draw module:

    $ java -jar apt.jar coverab nets/eb-nets/basic/pn1b-net.apt - | java -jar apt.jar draw - coverability.dot

The drawn coverability graph is directly written to the file
"coverability.dot" without creating an intermediate file for the
coverability graph in the APT file format.

The "-" tells APT to use the standard input or standard output when
files are either read from or written to a file.

Another way to integrate APT in other programs is to use its return
value. Among other use cases this can be used to create your own
output that is shown to the user like in the following example:

    if apt bounded nets/eb-nets/basic/pn1b-net.apt > /dev/null; then
      echo "The Petri net is bounded"
    else;
      echo "The Petri net is not bounded"
    fi;

Instead of showing the user the usual output from APT (which in this
case is directed to /dev/null/ to hide it from the user) the user
is presented with the phrase "The Petri net is bounded" if the Petri
net is indeed bounded or "The Petri net is not bounded" otherwise.

There are two modules which use external programs. These are the "use-synet" and
"use-petrify" modules.  If you want to use these modules, you need to make sure
that your PATH variable contains the directory where those programs are located.

Please note that there is now a "synthesize" module which offers all the
possibilities of "use-synet" and "use-petrify" and can even do more.

Synet and Petrify can be downloaded here:

http://www.irisa.fr/s4/tools/synet/

http://www.lsi.upc.edu/~jordicf/petrify/
