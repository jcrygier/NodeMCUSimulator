NodeMCU Simulator
=================

Simple Simulator for the NodeMCU (ESP8266) hardware.  This project is VERY early on, and is not nearly built out.  It was
started because I was tired of the test cycle of writing the Lua files to hardware and testing with actual hardware.

This project is built around LuaJ (http://www.luaj.org/luaj/3.0/README.html) with the NodeMCU API mocked out in Java, the
best possible.  Since the NodeMCU API's (http://nodemcu.readthedocs.org/en/dev/) are implemented in Java rather than hardware
it will never be 100% correct, but this project aims to get close.

Please, if anyone is interested, help is definitely welcome to make this more complete.  Some ideas is to implement more of
the UI (at least ADC), and possibly extend it to have a System Out console, and maybe even Lua Debugger.

To run, simply build and execute the Main class.  The simulation will automatically kick off the 'init.lua' file in the
working directory.  A sample blink program has been included.

The following API's have some / all work done:

Gpio
----

Keeps the pin states in memory, and allows for external interrupt / triggering.  All functions are working except 'serout'.

Net
---

Just the beginnings of the net API is complete.  It's just enough to create a simple client (No Server Yet).

Timer
-----

Large portion of the important parts of the API are complete (alarm, register, unregister, start).  Needs some work to be complete
but works in separate Java threads, so no busy CPU.

Wifi
----

Dummy module for now, just retains state.  Really just so I could run 'real' programs w/o it blowing up (Since a PC is
often operating just as a STATION and already connected).

Json
----

Fully implemented version (in Lua).  Easier to implement in lua (borrowed function), since there is no built in mechanism
to Coerce a Lua Table to a Java Map.

