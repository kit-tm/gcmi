## Prerequisites

- Controller App whose OpenFlow messages are intercepted and transformed
- OpenFlow-based network whose messages are intercepted
- IP addresses and ports of Controller App, TIL and network must be chosen adequately. The ones in the examples below correspond to the selected addresses and ports in the Evaluation.

## Start-up order

1. Tagging TIL
2. Controller App
3. Network

## Tagging TIL

Building:
The Tagging TIL can be built by running the following command in the ma-herter folder:

./gradlew installDist

Running:
To run the tagging approach, the following command can be used:

gcmi/examples/generic_attributes/build/install/generic_attributes/bin/generic_attributes --listen 127.0.0.1:7473 --upstream 127.0.0.1:7474

## Controller Apps

Exemplary Ryu-based Controller apps were created for the Evaluation.
They can be started by running the following command:

ryu run --ofp-tcp-listen-port 7474 <path-to-ryu-app-file>

where <path-to-ryu-app-file> may be:
- gcmi/examples/generic_attributes/controller/func_test.py
- gcmi/examples/generic_attributes/controller/conflict_eval.py
- gcmi/examples/generic_attributes/controller/rule_placement_eval.py

## Network

A network supporting OpenFlow is required for tests based on the tagging approach. Mininet networks were created for this matter in a virtual machine.
The corresponding files are located in the following folder:

gcmi/examples/generic_attributes/network/

A Python file from this folder can - when Mininet and Open vSwitch are installed on the machine - be run in the following way:

sudo python3 <python-file.py>

## Parsing Library

For potential future work with Experimenter functionality, the modified Loxigen library is placed in an archive file in:

gcmi/examples/generic_attributes/library/

When the library is built, it should replace the current library in ma-herter/lib/openflowj-3.5.1-SNAPSHOT.jar.

## Ryu Mods

Certain files in the Ryu Controller were required to be changed. They are listed in folder:

gcmi/examples/generic_attributes/library/

## Simulating Header field score 

The simulation is placed in folder:
gcmi/examples/generic_attributes/simulations/

The roles of the simulation files are the following:
- tag_opt.py: File that should be run to start the simulation.
- declarations.py: General classes and parameters of the simulation.
- greedy.py: Greedy selection strategy.
- probabilistic.py: Optimized strategy.

########################################################################################################################################################
