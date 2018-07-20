# Benchmarking hints

## Java's garbage collector

Java employs garbage collection for memory management. While APT itself is
mostly single-threaded, Java's garbage collector can employ multiple threads.
Thus, when APT is using more than one CPU core over longer periods of time, this
is a hint that memory is full and only the garbage collector is actually
running.

It is recommended to raise the memory limit if memory usage could be an
issue. Java being Java, more memory is always better.

By default, Java will only use about a quarter of the available physical memory.
To explicitly set a memory limit, you can use the `-Xmx` command line option.
For example, `java -Xmx2g -jar apt.jar` runs APT with a memory limit of two
gigabytes.

## Measuring Petri net synthesis

The `synthesize`-module for doing Petri net synthesis computes all unsolvable
separation problems by default. If you are not interested in this information
and only want either a Petri net or a failure, it is highly useful to use the
`quick-fail` option. This will make APT return a failure without computing all
unsolvable separation problems, which can often be a lot faster for unsolvable
inputs.
