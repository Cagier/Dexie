# Dexie
Server to read BG readings from mDrip and yDrip devices, decrypt data and send to xDrip phone

You only need the files in the root directory to run this.
Use the RunDexie.bat command to execute

The assumption is that you have java installed (some form of jre)

Also, you probably need to open port 17611 (or whatever you use)
so you may need to make firewall changes and do some port forwarding on your router

I will give details for this later for those who don't know how to do it.

The static address of the PC can be used but if you have a dynamic (changing) IP address
then you need to use something like DuckDNS to give you a hostname that will link to the
machine running the Dexie server.