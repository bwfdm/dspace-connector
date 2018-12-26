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
	<version>0.2.0</version>
</dependency>  
```
      
## How to use
- As an example see [DSpaceTest_v6.java](src/test/java/bwfdm/connector/dspace/test/DSpaceTest_v6.java)

## Features
- connection to DSpace is realized via REST-API and SWORD-v2 protocol
- REST-API is used to get the most essential information about collections and communities
- SWORD-v2 protocol is used to transfer files to the publication repository (make injest) and to get information from the service document as well, what collections are available for the user (see <a href="https://github.com/bwfdm/exporter-commons" target="_blank">Exporter Commons</a>) 
- as a core component is used <a href="https://github.com/swordapp/JavaClient2.0" target="_blank">SWORD JavaClient2.0</a>

## Tests
- see [DSpaceTest_v6.java](src/test/java/bwfdm/connector/dspace/test/DSpaceTest_v6.java)

## Limitations
- for the current moment only DSpace version 6.x is supported, see [DSpace_v6.java](src/main/java/bwfdm/connector/dspace/DSpace_v6.java)   
- it is important, that "REST" and "SWORD-v2" have to be activated by the publication repository. For more details please see the <a href="https://wiki.duraspace.org/display/DSDOC6x/Installing+DSpace#InstallingDSpace-InstallationInstructions" target="_blank">DSpace installation manual</a>
- currently supported only metadata as a Map<String, List<String>>, support of metadata as an XML-file will be implemented soon
 
## Used third party libraries and their licenses
- see [license-third-party.txt](license-third-party.txt)

## Own license
- MIT, see [license.txt](license.txt)
