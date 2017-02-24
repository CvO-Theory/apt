This directory contains some integration tests. A test with name FOO consists of
up to four files:

- FOO.args.txt contains the arguments to APT.
- FOO.out.txt contains the expected output on standard out.
- FOO.err.txt contains the expected output on standard error.
- FOO.exit.txt contains the expected exit code.

Only the arguments file must exist. Other, missing files are considered to be
empty, respectively to contain "0" for the exit code.

For each test case, APT is invoked with the provided arguments and it is checked
that the output is as specified.
