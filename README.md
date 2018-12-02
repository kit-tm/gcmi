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

## Acknowledgements

This work has been performed in the framework of the CELTIC EUREKA project SENDATE-PLANETS (Project ID
C2015/3-1), and it is partly funded by the German BMBF (Project ID 16KIS0460K). The authors alone are responsible
for the content. For more information about SENDATE-PLANETS, please visit http://www.sendate.eu/sendate-planets/. 

The initial prototype of GCMI was created by Simon Herter (https://github.com/sherter) in 2017 in the context of a master thesis within the SENDATE-PLANETS project. The prototype was extended in 2018 by Addis Dittebrandt (https://github.com/theascone). Both deserve thanks and appreciation for their excellent work!