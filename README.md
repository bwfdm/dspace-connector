# DSpace connector

Library to publish files and/or metadata to DSpace-based publication repositories

## Functionality

1. The complete functionality is described in the [PublicationRepository](src/main/java/bwfdm/connector/dspace/PublicationRepository.java) interface

2. Current functionality (of "Publicatoin Repository")
- boolean isAccessible()
	- check if publication repository is accessible
- boolean setCredentials(String user, char[] password);
	- set user credentials (login, password)
	- could be used for every user aswell for the admin user, who can further make a publication on behalf of other user
- boolean isUserRegistered(String loginName);
	- check if user is registered
- boolean isUserAssigned(String loginName)
	- check if user is allowed to maeke a publication in the repository (amount of available collections > 0)
- Map<String, String> getUserAvailableCollectionsWithTitle(String loginName)
	- get a map of avaialble kollections
	- key = link to the collection
	- value = name of the collection (only the name, WITHOUT subcommunities)
- Map<String, String> getUserAvailableCollectionsWithFullName(String loginName, String fullNameSeparator)
	- get a map of avaialble kollections
	- key = link to the collection
	- value = full name of the collection, which includes all subcommunities and the collection title
	- all parts of the name (subcommunities and collection title), which are separated with the fullNameSeparator
- Map<String, String> getAdminAvailableCollectionsWithTitle()
	- get collection titles, which are available for the admin user
	- the admin user must be set up earlier via "setCredentials"
- Map<String, String> getAdminAvailableCollectionsWithFullName(String fullNameSeparator)
	- get available for the admin user collection titles with included subcommunities
- boolean publishFile(String userLogin, String collectionURL, File fileFullPath)
	- publish a file to some collection
- boolean publishMetadata(String userLogin, String collectionURL, Map<String, String> metadataMap)
	- publich pure metadata (without any other artefact) into the collection
	- metadata is described via Map, where:
		- key = a field name (traditionally based on doublin core, but without "ds" prefix), e.g. "title", "publisher"
		- value = the value of the metadata
	- metadata value is spercific for every publiction repository
- boolean publishMetadata(String userLogin, String collectionURL, File metadataFileXML)
	- publish pure metadata (without any other artefact) into the collection
	- metadata is described as an xml-file in ATOM format
	- see [entry.xml](src/test/resources/testfiles/entry.xml)
- boolean publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath, Map<String, String> metadataMap)
	- publish a file together with the metadata
	- file could be as a single file or some zip-archive with many files inside
	- in case of zip archive, all files inside will be published as a unique file in the scope of this publication
	- metadata is described via Map (see above)
- boolean publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath, File metadataFileXML)
	- publish a file together with the metadata
	- file could be as a single file or some zip-archive with many files inside (see above)
	- metadata is described via xml-file (see above)

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
		<groupId>com.github.sara-service</groupId>
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
