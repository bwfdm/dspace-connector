# Current functionality

The following table lists currently supported methods of the DSpace connector. For more details see the [PublicationRepository](src/main/java/bwfdm/connector/dspace/PublicationRepository.java) interface. 

|           Method            |    Return Type     | Description |
|:--------------------------- |:------------------ |:----------- |
| isAccessible() | boolean | - check if publication repository is accessible |
| setCredentials(String user, char[] password) | boolean | - set user credentials (login, password) <br> - could be used for every user aswell for the admin user, who can further make a publication on behalf of other user |
| isUserRegistered(String loginName) | boolean | - check if user is registered |
| isUserAssigned(String loginName) | boolean | - check if user is allowed to maeke a publication in the repository (amount of available collections > 0) |
| getUserAvailableCollectionsWithTitle(String loginName) | Map<String, String> | - get a map of avaialble kollections <br> - key = link to the collection <br> - value = name of the collection (only the name, WITHOUT subcommunities) |
| getUserAvailableCollectionsWithFullName(String loginName, String fullNameSeparator) | Map<String, String> | - get a map of avaialble kollections <br> - key = link to the collection <br> - value = full name of the collection, which includes all subcommunities and the collection title <br> - all parts of the name (subcommunities and collection title), which are separated with the fullNameSeparator |
| getAdminAvailableCollectionsWithTitle() | Map<String, String> | - get collection titles, which are available for the admin user <br> - the admin user must be set up earlier via "setCredentials" |
| getAdminAvailableCollectionsWithFullName(String fullNameSeparator) | Map<String, String> | - get available for the admin user collection titles with included subcommunities |
| publishFile(String userLogin, String collectionURL, File fileFullPath) | boolean | - publish a file to some collection |
| publishMetadata(String userLogin, String collectionURL, Map<String, String> metadataMap) | boolean | - publich pure metadata (without any other artefact) into the collection <br> - metadata is described via Map, where: <br> -- key = a field name (traditionally based on doublin core, but without "ds" prefix), e.g. "title", "publisher" <br> -- value = the value of the metadata <br> - metadata value is spercific for every publiction repository |
| publishMetadata(String userLogin, String collectionURL, File metadataFileXML) | boolean | - publish pure metadata (without any other artefact) into the collection <br> - metadata is described as an xml-file in ATOM format <br> - see [entry.xml](src/test/resources/testfiles/entry.xml) |
| publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath, Map<String, String> metadataMap) | boolean | - publish a file together with the metadata <br> - file could be as a single file or some zip-archive with many files inside <br> - in case of zip archive, all files inside will be published as a unique file in the scope of this publication <br> - metadata is described via Map (see above) |
| publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath, File metadataFileXML) | boolean | - publish a file together with the metadata <br> - file could be as a single file or some zip-archive with many files inside (see above) <br> - metadata is described via xml-file (see above) |
