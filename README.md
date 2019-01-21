# Generic Control Message Interception (GCMI)

GCMI is a new API for Software-defined Networking (SDN) that can be used together with the widespread OpenFlow protocol developed by the ONF (https://www.opennetworking.org/software-defined-standards/specifications/). 

Purpuse of the API: create GCMI-apps which operate on existing control messages exchanged between an SDN controller and a set of SDN switches. Applications for this include transparent optimizations to an SDN network or passive control plane monitoring. This repository contains the first release of the GCMI prototype implementation.
The prototype generalizes and simplifies an idea that is applied in many systems, sometimes referred to as "proxy" or "message interception" layer. All control messages that are passed through this layer (Flow Table Modifications, Flow Stats Requests, ...) can be intercepted and altered inside a GCMI-App. 

## Overview

This prototype provides the following features:

- Fully functional TCP proxy layer that can be placed between SDN controllers and SDN switches
- Novel interface to create GCMI-Apps
- REST-based orchestration of GCMI-Apps
- Different interception primitives to get access to control messages
- Basic filtering capabilities for the interception process
- Automatic context management, e.g., to deal with OpenFlow XIDs

The core proxy layer implementation and the API can be found in the composer directory. Different examples of how to use the framework can be found in the examples directory.

## Examples

This repository contains several examples GCMI apps. This list gives a short overview of the working examples and where to find them. For some examples, there is more than one file. In this case, the core file is linked here (you can easily find the related files going to the directory that contains the example).

- Log-and-Forward
	- Link: https://github.com/kit-tm/gcmi/blob/master/examples/log_and_forward/src/main/java/com/github/sherter/jcon/examples/logandforward/LogAndForward.java
	- Description: A very simple app that performs transparent control plane monitoring. It listens for all control messages without filtering and if a control message is received, it creates a log entry and forwards the message without any modifications.
- Throttling
    - Link: https://github.com/kit-tm/gcmi/blob/master/composer/src/main/java/com/github/sherter/jcon/composer/ThrottleLayer.java
    - Description: A very small working example of a GCMI app that does something useful (besides log-and-forward). The app intercepts all FlowStatReq messages and implements a "throttling function": if too many requests are received in a certain time window, the messages are dropped (i.e., only a maximum number of requests is allowed to get to the switch).
- Port Based Capacity Extension
	- Link: https://github.com/kit-tm/gcmi/blob/master/composer/src/main/java/com/github/sherter/jcon/composer/Pbce2Layer.java
	- Description: This is a fully functional re-implementation of PBCE (see https://ieeexplore.ieee.org/document/7809656). PBCE provides the possibility to delegate flows from a switch with many flow table entries to another less loaded neighboring switch without breaking control plane transparency, i.e., without interfering with existing SDN applications.
	- Hint: An earlier example of this app with less functionality can be found here: https://github.com/kit-tm/gcmi/tree/master/examples/pbce),
- TableVisor
	- Link: https://github.com/kit-tm/gcmi/blob/master/composer/src/main/java/com/github/sherter/jcon/composer/TableVisorLayer.java
	- Description: This is a partial re-implemantation of TableVisor (see https://ieeexplore.ieee.org/document/8004108). TableVisor implements an emulation layer that aims to provide switches with n flow tables by aggregating multiple physical switches with < n flow tables.
- Generic Attribute Tagging
    - Link: https://github.com/kit-tm/gcmi/blob/master/examples/generic_attributes/src/main/java/com/github/sherter/jcon/examples/generic_attributes/interception/MessageInterceptor.java
    - Description: An exemplary app that simplified attribute tagging for SDN controller apps by translating high-level tagging rules into existing tag representations inside a packet's header fields. The implementation was part of the Master's Thesis of David Koerver with the title: 'Generic and Transparent Attribute Tagging in Software-defined Networking'. 

There are also several other examples that might not work with the current version of GCMI (not tested).

- Graph Computation
	- Link: https://github.com/kit-tm/gcmi/blob/master/examples/graph_computation/src/main/java/com/github/sherter/jcon/examples/graphcomputation/GraphComputation.java
	- Description: Emulate high load within an GCMI app. Useful to determine how this can influence latency.
- FlowVisor
	- Link: https://github.com/kit-tm/gcmi/tree/master/examples/flowvisor/src/main/java/com/github/sherter/jcon/examples/flowvisor
	- Description: A preliminary implementation of the FlowVisor concept (see https://www.gta.ufrj.br/ensino/cpe717-2011/openflow-tr-2009-1-flowvisor.pdf).
- Learning Switch
	- Link: https://github.com/kit-tm/gcmi/blob/master/examples/learning_switch/src/main/java/com/github/sherter/jcon/examples/learningswitch/SimpleSwitch.java
	- Description: In theory, a GCMI app can replace every regular SDN app. This is demonstrated here where a traditional learning switch is implemented as an GCMI app. Note that this is not the original purpose of GCMI!
- PayLess
	- Link: https://github.com/kit-tm/gcmi/blob/master/examples/payless/src/main/java/com/github/sherter/jcon/examples/payless/Payless.java
	- Description: A small caching layer for monitoring requests, inspired by the PayLess paper (see https://ieeexplore.ieee.org/document/6838227)

## Acknowledgements

This work has been performed in the framework of the CELTIC EUREKA project SENDATE-PLANETS (Project ID
C2015/3-1), and it is partly funded by the German BMBF (Project ID 16KIS0460K). The authors alone are responsible
for the content. For more information about SENDATE-PLANETS, please visit http://www.sendate.eu/sendate-planets/. 

The initial prototype of GCMI was created by Simon Herter (https://github.com/sherter) in 2017 in the context of a master thesis within the SENDATE-PLANETS project. The prototype was extended in 2018 by Addis Dittebrandt (https://github.com/theascone). Both deserve thanks and appreciation for their excellent work!
