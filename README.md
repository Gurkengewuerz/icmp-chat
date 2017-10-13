# ICMPv4 Chat

To visulize what a nice protocol ICMP is i wrote this little programm. ICMP has a data field, where you can set up to 65535 bytes, in this case the chat data.  

### Usage:
1. Build `mvn package`
2. Start program
3. Select Network Card
4. Fill in local network address(es) in the textfield. This is a list, where the messages goes to
5. There you go! In the chat tab you can now view messages and send messages

### Requirements:

 * libpcap 1.1.1 or  WinPcap 4.1.2


### Known bugs:

 * No WiFi support 


### Stuff used to make this:

 * [pcap4j](https://github.com/kaitoy/pcap4j) for packet crafting and captureing
 * [forms_rt](https://github.com/JetBrains/intellij-community/tree/master/platform/forms_rt) for the uiDesigner
