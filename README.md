# DSpace connector

Library to publish files and/or metadata to DSpace-based publication repositories

## Functionality

1. Textual description of the currently implemented methods could be found in [functionality.md](functionality.md)
2. The main interface is [PublicationRepository.java](src/main/java/bwfdm/connector/dspace/PublicationRepository.java) interface

## How to add to your maven project
    
1. As dependency from maven repository 
    - TODO: will be added soon ...
    
2. As dependency from GitHub (more details see here: <a href="https://jitpack.io/docs/#jitpackio" target="_blank">JitPack</a>)
    
    - add JitPack repository to pom.xml
    
	```
	<repositories>
		<!-- JitPack package repository to use libraries
			not only from maven repository, but also from GitHub.
			More info: https://jitpack.io/docs/#jitpackio -->
		<repository>
			<id>jitpack-repo</id>
			<url>https://jitpack.io</url>
		</repository>
	</repositories>
	```
    
    - add the latest version of "dspace-connector" as a new dependency to pom.xml
    
	```
	<dependency>
		<groupId>com.github.bwfdm</groupId>
		<artifactId>dspace-connector</artifactId>
		<version>master-SNAPSHOT</version>
	</dependency>
	```
    
## How to use
- As an example see [DSpaceTest_v6.java](src/test/java/bwfdm/connector/dspace/test/DSpaceTest_v6.java)
- ..
- TODO: add text

## Features
- connection to DSpace is realized via REST-API and SWORD-v2 protocol.
- REST-API is used to get the most essential information about collections and communities
- SWORD-v2 protocol is used to transfer files to the publication repository (make injest) and to get information from the service document as well, what collections are available for the user 
- as a core component is used the <a href="https://github.com/swordapp/JavaClient2.0" target="_blank">SWORD JavaClient2.0</a>

## Tests
- TODO: add text

## Limitations
- for the current moment only DSpace version 6.x is supported, see [DSpace_v6.java](src/main/java/bwfdm/connector/dspace/DSpace_v6.java)
- [PublicationRepository](src/main/java/bwfdm/connector/dspace/PublicationRepository.java) interface provides only basic functionality for the publication. Further extensions should be added soon. You are welcome to make a contribution ;)
- it is important, that "REST" and "SWORD-v2" have to be activated by the publication repository. For more details please see the <a href="https://wiki.duraspace.org/display/DSDOC6x/Installing+DSpace#InstallingDSpace-InstallationInstructions" target="_blank">DSpace installation manual</a>

 
## Used third party libraries and their licenses
- see [license-third-party.txt](license-third-party.txt)
     

## Own license
- MIT, see [license.txt](license.txt)
