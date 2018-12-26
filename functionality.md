# Current functionality

For the current moment DSpace Connector implements the ExportRepository-interface (common methods which should be actual for different types of publication repositories) and has also some DSpace-specific methods. 

The methods of ExportRepository interface are provided bellow. Please use these methods as a common interface for the DSpace Connector. For more information please see the <a href="https://github.com/bwfdm/exporter-commons/blob/master/src/main/java/bwfdm/exporter/commons/ExportRepository.java" target="_blank">ExportRepository.java</a> file.

|           Method            |    Return Type     | Return Value | Description |
|:--------------------------- |:------------------ | :----------- |:----------- |
| isRepositoryAccessible() | boolean | `true` if repository is accessible and `false` otherwise | Check if publication repository is accessible via some API |
| hasRegisteredCredentials() | boolean | `true` if credentials are registered and `false` otherwise | Check if current authentication credentials (e.g. user/admin login and password (including also on-behalf-of option)) are registered in the repository |
| hasAssignedCredentials() | boolean | `true` if credentials are allowed to make an export into repository and `false` otherwise | Check if current authentication credentials (e.g. user/admin login and password (including also on-behalf-of option)) are allowed to make an export (publication) into repository. Different credentials can have different access rights |
| getAvailableCollections() | Map<String, String> or `null` in case of error| Map of avaialble kollections <br> -- key = link to the collection <br> -- value = title of the collection (only collection title, WITHOUT subcommunities) | Get available for the current authentication credentials collections
| exportNewEntryWithMetadata(String collectionUrl, Map<String, List<String>> metadataMap) | String or `null` in case of error| `String` with the URL of the new created entry | Export (create) a new entry with metadata only (without any file) in some collection, which should be available for the current authentication credentials
| exportNewEntryWithFileAndMetadata(String collectionURL, File file, boolean unpackFileIfArchive, Map<String, List<String>> metadataMap) | String or `null` in case of error| `String` with the URL of the new created entry | Export (create) a new entry with a file and metadata in some collection, which should be available for the current authentication credentials

If more functionality is needed, DSpace Connector provides some other methods.

DSpace common methods, for more information please see the [DSpaceRepository interface](src/main/java/bwfdm/connector/dspace/DSpaceRepository.java):

|           Method            |    Return Type     | Return Value | Description |
|:--------------------------- |:------------------ | :----------- |:----------- |
| getCommunitiesForCollection(String collectionURL) | List<String> or `null` in case of error| `List<String>` of communities for the collection (0 or more communities are possible) | Get a list of communities for the provided collection
| getAvailableCollectionsWithFullName(String fullNameSeparator) | Map<String, String> or `null` in case of error| `Map<String, String>`, where key = "collection full URL", value = "collection full name". The Map could be also empty if there are not available collections | Get collections, which are available for the current authentication credentials, and show their full name (e.g. for DSpace-repository it means "community/subcommunity/collection", where "/" is the fullNameSeparator)

DSpace v.6 specific methods:

|           Method            |    Return Type     | Return Value | Description |
|:--------------------------- |:------------------ | :----------- |:----------- |
| isRestAccessible() | boolean | `true` if REST-interface is accessible and `false` otherwise | Check if REST-interface is accessible
| exportNewEntryWithFile(String collectionUrl, File file, boolean unpackFileIfArchive) | String or `null` in case of error | `String` with the URL of the new created entry | Export (create) a new entry with a file in some collection, which should be available for the current authentication credentials
| createEntryWithMetadata(String collectionURL, Map<String, List<String>> metadataMap) | String | `String` with the entry URL which includes "/swordv2/edit/" substring inside. This URL could be used without changes for further update of the metadata. | Method is used as a body for "exportNewEntryWithMetadata(String collectionUrl, Map<String, List<String>> metadataMap)" - export the metadata only (without any file) to some collection, which should be available for the current authentication credentials
| createEntryWithMetadataAndFile(String collectionURL, File file, boolean unpackZip, Map<String, List<String>> metadataMap) | String | `String` with the entry URL which includes "/swordv2/edit/" substring inside. This URL could be used without changes for further update of the metadata. | Method is used as a body for "exportNewEntryWithFileAndMetadata(String collectionURL, File file, boolean unpackFileIfArchive, Map<String, List<String>> metadataMap)" - export a file together with the metadata to some collection, which should be available for the current authentication credentials

