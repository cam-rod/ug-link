#!/usr/bin/env bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 me.camrod.ug_link.UGLink