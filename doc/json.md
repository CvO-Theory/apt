JSON interface to APT
=====================

This document assumes that you [already know how to use APT](using.md). It will
describe a JSON-based interface to interact with APT. For this, a separate JAR
file is built: `apt-json.jar`

When you run `apt-json.jar`, it expects JSON commands on its standard input and
formats its result as JSON, too. The input can contain a series of commands and
these are executed one after another.

For example, with the following input

    {"command":"list_modules"}
    {
     "command": "describe_module",
     "module": "bitnet_generator"
    }
    {
     "command": "run_module",
     "module": "bitnet_generator",
     "arguments": {"n": "1"}
    }

this output is produced (some shortening was applied, marked by "[...]"):

    {"modules": [
     {
      "name": "ac",
      "description_long": "Check if [...]",
      "description": "Check if a Petri net is asymmetric-choice",
      "categories": ["Petri net"]
     },
     [...]
    ]}
    
    {
     "return_values": [{
      "name": "pn",
      "type": "uniol.apt.adt.pn.PetriNet"
     }],
     "name": "bitnet_generator",
     "description_long": "Construct a Petri net for a bit nets of a given size.",
     "description": "Construct a Petri net for a bit nets of a given size.",
     "categories": ["Generators"],
     "parameters": [{
      "name": "n",
      "description": "The argument for the Petri net generator",
      "optional": false,
      "type": "java.lang.Integer",
      "properties": []
     }]
    }
    
    {"return_values": {"pn": "[...]"}}

Timeouts
--------

Additionally to what is possible with APT normally, the JSON interface allows to
specify a timeout for the execution of a module:

    {
     "command": "run_module",
     "timeout_milliseconds": 0,
     "module": "regular_language_to_lts",
     "arguments": {"lang": "a"}
    }

result in:

    {
     "error": "Execution was interrupted",
     "type": "uniol.apt.util.interrupt.UncheckedInterruptedException"
    }

Chained module calls
--------------------

This also adds support for chained module calls. This means that the result of
one module is used as the input to another module. To use this feature, instead
of providing a module argument as a string, it is provided as a JSON object
describing another module invocation. A special key "use" describes which result
of the module should be passed on.

For example, this input:

    {
     "command": "run_module",
     "module": "bounded",
     "arguments": {
      "pn": {
       "module": "bitnet_generator",
       "use": "pn",
       "arguments": {"n": "2"}
    }}}

results in:

    {"return_values": {
     "bounded": "Yes",
     "smallest_K": "1"
    }}

What happens is that the "bitnet_generator" is invoked and its return value with
name "pn" is then given to the "bounded" module.
