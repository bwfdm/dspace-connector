All notable changes to this project will be documented in this file.
We follow the [Semantic Versioning 2.0.0](http://semver.org/) format.


## 0.3.1 - 2019-04-08

### Added
- none

### Deprecated
- none

### Removed
- none

### Fixed
- Update version of "jackson-databind" dependency to "2.8.11.2" to solve a security issue:  
  * see -> https://nvd.nist.gov/vuln/detail/CVE-2018-12022



## 0.3.0 - 2019-03-05

### Added
- usage of the Exporter-Commons library 0.4.0 as a dependency (see https://github.com/bwfdm/exporter-commons)
  * some methods have new names and input variables
    - exportNewEntryWithMetadataAndFile(String collectionURL, Map<String, List<String>> metadataMap, File file, boolean unpackFileIfArchive)
    - createEntryWithMetadataAndFile(String collectionURL, Map<String, List<String>> metadataMap, File file, boolean unpackZip, boolean inProgress)
- new class DSpaceSwordOnly, which provides a full functionality via only SWORD-v2 protocol. For that case some extra configuration of the DSpace repository is needed:
  * on the publication repository server open the sword-v2 configuration file: `/dspace/config/modules/sword-server.cfg`
  * set value: `expose-communities = true` (to offer in the service document a list of communities instead of collections, per default is "false")
  * restart the web server as usual to apply the changes
- automatic support of different types of the service document (with collections or with communities inside)     
- explicit usage of "in-progress: true" and "in-progress: false" headers (as input variable for the export methods)
- usage of the HTTP-client which does not ignore the SSL. If you have some problems (e.g. some exceptions "input is not a X.509 certificate"), please see here:
  * https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore

### Deprecated
- none

### Removed
- exportNewEntryWithFileAndMetadata (see "added" section above)

### Fixed
- none 




## 0.2.0 - 2018-12-26

### Added
- usage of the Exporter-Commons library as a dependency (see https://github.com/bwfdm/exporter-commons)
- implementation of the ExportRepository interface (see https://github.com/bwfdm/exporter-commons/blob/master/src/main/java/bwfdm/exporter/commons/ExportRepository.java)

### Deprecated
- none

### Removed
- PublicationReposiotry interface is not supported now
- some functionality was reduced (metadata as XML-file is not officially supported for the current moment)

### Fixed
- none  




## 0.1.0 - 2018-08-22

### Added
- basic DSpace-v6 functionality is supported

### Deprecated
- none

### Removed
- none

### Fixed
- none
