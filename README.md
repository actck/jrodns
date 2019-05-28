# jrodns
A dns proxy tool to help implement ipset for routeros.

In normal, people using dnsmasq+iptables+ipset to do that thing you know.
But if you are using a hw-router like routerboard(routeros), you can't install external
 service or access that "internal-iptables".
 
This just a dns proxy tool, you still need a trusted dns resource.

# how it working
1. listen dns query request
2. forward query to the top dns server
3. got the dns answer and put it into routeros firewall using routeros' api.

# prepare and run
1. java env.
2. Compile and package with mvn. An executeable jar file named jrodns-exec.jar
will be generating.
3. Put a config file same path with the jar. 
Config file name must be "jrodns.properties".
4. run with command "java -jar jrodns-exec.jar".
5. change client's dns setting

# config

|key |require|default|desc|
|:---|  :---:|   :---: |:---|
|gfwlistPath|1| |the gfwlist file path. Value could be file name or absolute path
|rosIp|1| | ros server ip
|rosUser|1| | ros router login username
|rosPwd|1| | ros router login password
|rosFwadrKey|1| | address-list key to set in ros
|rosIdle|0|30| ros api-connection check delay
|localPort|0|53|local port for client dns query request
|remote|1| |remote dns server for dns iterator request
|remotePort|1|53| remote dns server port for dns iterator request
|maxThread|0|10|server worker count

