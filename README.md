# DSpace connector

Library to publish files (as zip-archives) and/or metadata to DSpace-based publication repositories

## How to add to your project
    
1. Usage as maven library 
    - TODO: will be added soon ...
    
2. Usage as library from GitHub (will be updated!)
    - via [JitPack] (https://jitpack.io/docs/#jitpackio)
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
    - add new dependency to pom.xml
    
    ```
    <dependency>
			<groupId>com.github.sara-service</groupId>
            <artifactId>dspace-connector</artifactId>
            <version>master-SNAPSHOT</version>
	</dependency>
    ```
    
## How to use
- TODO: add text

## Features
- connection to DSpace is realized via REST-API and SWORD-v2 protocol.
- REST is used for getting some information about collections and communities
- SWORD is used for publication and and getting information, what collections are available for the user
- as a core component is used the [SWORD JavaClient2.0] (https://github.com/swordapp/JavaClient2.0)

## Tests
- TODO: add text

 
## Used dependencies and their licenses
- see [license-third-party.txt](license-third-party.txt)
     

## Own license
- MIT, see [license.txt](license.txt)




