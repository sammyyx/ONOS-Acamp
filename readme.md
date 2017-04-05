# ONOS-ACAMP

## Brief introduction

Ap Control and Manage Protocol(ACAMP), a protocol used to configure and manage access point(AP) under SDN architecture. This is the source code of controller apps building on ONOS, SDN controller.

![http://wx4.sinaimg.cn/mw690/a8171f87ly1febrfi3q3tj20s50483za.jpg]()

## Architecture

This apps is composed of seven components, including cmd, device, network, process, protocol, rest and utils. 

### cmd

This folder including command that we used in ONOS command line for getting information from ap or setting ap's configuration.

### device

This folder including two classes helping manage protocol value for controller and ap.

#### process

This folder including finite state machine design for protocol processing.

### protocol

This folder including message serialization and deserilization method and protocol constants definition.

### rest

This folder including rest api building for web apps or cmd apps.

### utils

This folder including some tools alike apis for our app, such as conversion for diffrent basic data type and message building methods.