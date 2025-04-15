# Leavu3

See [the official forum thread](https://forum.dcs.world/topic/137957-a-sneak-peak-of-leavu3) for more info

Leavu is a software for building home cockpits for DCS Flaming Cliffs. It uses [Dcs Remote 2](https://github.com/GiGurra/dcs-remote2) to communicate with DCS. Leavu currently includes one instrument - a generic MFD with 5 pages:
 * HSD
 * RWR
 * SMS
 * FCR
 * INFO


### INSTALLATION

* Make sure you have [java 8 or greater installed](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Download dcs-remote2.zip and leavu3.zip](http://build.culvertsoft.se/dcs/)
* Unzip both where you like
* Launch dcs-remote2.jar on your game PC
  * Verify a tray icon appears (black square with the text *oo DCS* inside it)
  * Edit static-data.json with your callsign and data link host+group information
* Launch leavu3.jar (an MFD should appear)
  * Click INF to get to the INFO page and verify Dcs Remote is connected
* [**Make sure you don't expose dcs-remote2's listening port online**](https://github.com/GiGurra/dcs-remote2)
  * Otherwise anyone can run any lua script inside your DCS game ..
  * This is 12340 by default - do NOT expose/forward/open this port online!

FYI: Dcs-Remote will automatically on startup install all required scripts into your DCS installation.

### CONFIGURATION

**To use it solo on the same PC as DCS, no configuration is needed** - however - You can also run it on a separate computer. 
To run Leavu3 on a different computer than your game PC, unzip the leavu3.zip on your second computer and edit leavu3-cfg.json. Input the ip address of your game PC. NOTE: DCS Remote **must** be running on your game PC.

Leavu can be configured to connect to a datalink. The datalink server is just a separate instance of dcs-remote2. To configure datalink parameters check your dcs-remote2 folder for the file *static-data.json* and fill in your parameters for:
 * data link host
 * data link port
 * data link callsign
 * data link team

**By default your datalink host is set to build.culvertsoft.se, team: BLUE_RABBITS, callsign: JarJar**

You can also configure parameters for window position, size, AA, update rates, etc. Check the leavu3-cfg.json for a full list of configurable parameters.


### WHERE IS THE DATA COMING FROM?

Leavu3 uses [dcs-remote2](https://github.com/GiGurra/dcs-remote2) to inject a [data export script](https://github.com/GiGurra/leavu3/blob/master/src/main/resources/lua_scripts/LoDataExport.lua) into DCS, which then grabs ownship + AI wingman information from the game. In order to display information regarding your human wingmen and their targets, you both need to be connected to the Leavu3 datalink (see above).


### STATUS & BUGS!

ONLY use [the Issue tracker at the top of this page](https://github.com/GiGurra/leavu3/issues) to report bugs!

Thanks!


### Performance!

For max performance, try configuring your jvm to something along the lines of:
JAVA_TOOL_OPTIONS: -XX:+UseG1GC -XX:MaxGCPauseMillis=5 -Xms32m -XX:CompileThreshold=100


### COMPATIBILITY

Built with LibGDX for cross platform compatibility. Works out of the box on:
* Windows
* Linux
* Mac

If you know someone with android experience that wants to do an android build, send a pull request!
