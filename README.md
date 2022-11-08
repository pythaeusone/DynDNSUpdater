# DynDNSUpdater
DynDNS Updater für https://ipv64.net.<br>
Java version 8.0.352+<br>
Getestet auf Windows 10 Pro, Ubuntu 22.04, Mac OSC Catalina <br>
Ausführen über die Console:
```
java -jar DynDNSUpdater.jar eureDomain Updatehash Zeit_in_Minuten ipArt
java -jar DynDNSUpdater.jar sub.ipv64.net hfqYT4PU8Lpv5ysBaJZwNFoI 30 ipv4
java -jar DynDNSUpdater.jar sub.ipv64.net hfqYT4PU8Lpv5ysBaJZwNFoI 30 ipv6
java -jar DynDNSUpdater.jar sub.ipv64.net hfqYT4PU8Lpv5ysBaJZwNFoI 30 ipv64

ipv4 = nur IPv4
ipv6 = nur IPv6
ipv64 = ipv4 & ipv6
```
Verwendete libs:<br> Java-JSON<br>

JavaDoc:<br> https://pythaeusone.github.io/DynDNSUpdater/
