#!/bin/bash
adb uninstall net.davidbourguignon.monvelib;ant debug;adb install bin/MonVelib-debug.apk
