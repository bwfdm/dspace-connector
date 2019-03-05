# DSpace connector

Library to publish files and/or metadata to DSpace-based publication repositories

## Functionality

1. Textual description of the currently implemented methods could be found in [functionality.md](functionality.md)
2. The <a href="https://github.com/bwfdm/exporter-commons/blob/master/src/main/java/bwfdm/exporter/commons/ExportRepository.java" target="_blank">ExportRepository interface</a> is implemented, what provides a common language with other even not DSpace-based repositories

## How to add to your maven project (as dependency in pom.xml)
  
```
<dependency>
	<groupId>com.github.bwfdm</groupId>
	<artifactId>dspace-connector</artifactId>
	<version>0.3.0</version>
</dependency>  
```
      
## How to use
- As an example see [DSpaceTest.java](src/test/java/bwfdm/connector/dspace/test/DSpaceTest.java)

## Features
- connection to DSpace is realized via REST-API and SWORD-v2 protocol
- complete functionality via only SWORD-v2 protocol is possible too, please use [DSpaceSwordOnly.java](src/main/java/bwfdm/connector/dspace/DSpaceSwordOnly.java) class for that. IMPORTANT: for this case some special configuration of the publication repository is needed (see "Limitations" section bellow).   
- REST-API is used to get the most essential information about collections and communities
- SWORD-v2 protocol is used to transfer files to the publication repository (make injest) and to get information from the service document as well, what collections (and in extra case - communities) are available for the user (see <a href="https://github.com/bwfdm/exporter-commons" target="_blank">Exporter Commons</a>) 
- as a core component for the SWORD-v2 communication the <a href="https://github.com/swordapp/JavaClient2.0" target="_blank">SWORD JavaClient2.0</a> library is used

## Tests
- see [DSpaceTest.java](src/test/java/bwfdm/connector/dspace/test/DSpaceTest.java)

## Limitations
- it is important, that "REST" and "SWORD-v2" have to be activated by the publication repository. For more details please see the <a href="https://wiki.duraspace.org/display/DSDOC6x/Installing+DSpace#InstallingDSpace-InstallationInstructions" target="_blank">DSpace installation manual</a>
- it is also possible to provide full functionality ONLY via SWORD-v2 protocol using the [DSpaceSwordOnly.java](src/main/java/bwfdm/connector/dspace/DSpaceSwordOnly.java) class. For this case some extra configuration of the publication repository is needed:
  * on the publication repository server open the sword-v2 configuration file: `/dspace/config/modules/sword-server.cfg`
  * set value: `expose-communities = true` (to offer in the service document a list of communities instead of collections, per default is "false")
  * restart the web server as usual to apply the changes  
- support of the REST-API currently is only implemented for DSpace version 6.x, see [DSpace_v6.java](src/main/java/bwfdm/connector/dspace/DSpace_v6.java)   
- currently supported is only metadata as a Map<String, List<String>>, support of the metadata as an XML-file will be implemented in future releases
 
## Used third party libraries and their licenses
- see [license-third-party.txt](license-third-party.txt)

## Own license
- MIT, see [license.txt](license.txt)
