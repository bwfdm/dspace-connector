/*
 * Unless expressly otherwise stated, code from this project is licensed under the MIT license [https://opensource.org/licenses/MIT].
 * 
 * Copyright (c) <2018> <Volodymyr Kushnarenko, Stefan Kombrink, Markus Gärtner, Florian Fritze, Matthias Fratz, Daniel Scharon, Sibylle Hermann, Franziska Rapp and Uli Hahn>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package bwfdm.connector.dspace.test;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.client.SWORDClientException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import bwfdm.connector.dspace.DSpaceRepository;
import bwfdm.connector.dspace.DSpace_v6;
import bwfdm.exporter.commons.ExportRepository;

/**
 * Class for manual testing of DSpace_v6 methods
 * 
 * @author Volodymyr Kushnarenko
 */
public class DSpaceTest_v6 {
	
	protected static final Logger log = LoggerFactory.getLogger(DSpaceTest_v6.class);
	
	// Files for export
	private static File zipPackageFilesOnly = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/package_files_only.zip").getFile());
	private static File zipPackageFilesWithMetadata = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/package_files_with_metadata.zip").getFile());
	private static File zipPackageFilesMetadataOnly = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/package_metadata_only.zip").getFile());
	private static File zipPackageWithSubfolder = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/package_with_subfolder.zip").getFile());
	private static File xmlMetadataFile = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/entry.xml").getFile());
	private static File txtFile = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/test-file.txt").getFile());
	private static File otherFile = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/test-file.with.dots.txt.t").getFile());
	
	private static boolean inProgress = true;
	
	public static void main(String[] args) {
		
		Scanner scanner = new Scanner(System.in); //for password input (ATTENTION: typo is NOT hidden!)
		
		try {
			
			File fXML = new File(DSpaceTest_v6.class.getClassLoader().getResource("repositories.xml").getFile());
			File fXSD = new File(DSpaceTest_v6.class.getClassLoader().getResource("repositories_schema.xsd").getFile());
			
			// Validate xml schema
			if(!validateXMLSchema(fXSD, fXML)) {
				log.error("XML schema is not valid! Exit.");
				System.exit(1);
			}
			
			// Parse xml file
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXML);
			doc.getDocumentElement().normalize(); //normalization
			
			NodeList nList = doc.getElementsByTagName("repoConfig"); //Config for every repository
			
			for(int i=0; i<nList.getLength(); i++) {
				
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					System.out.println("==== TEST: DSpace v6, Repository Nr." + (i+1) + " ====\n");
					
					Element eElement = (Element) nNode;
					
					String serviceDocumentUrl = eElement.getElementsByTagName("serviceDocumentUrl").item(0).getTextContent();
					String restUrl = eElement.getElementsByTagName("restUrl").item(0).getTextContent();
					String publicationCollectionUrl = eElement.getElementsByTagName("publicationCollectionUrl").item(0).getTextContent();
					String adminUser = eElement.getElementsByTagName("adminUser").item(0).getTextContent();
					String normalUser = eElement.getElementsByTagName("normalUser").item(0).getTextContent();
					
					System.out.println("[serviceDocumentUrl] -- " + serviceDocumentUrl);
					System.out.println("[restUrl] -- " + restUrl);
					System.out.println("[publicationCollectionUrl] -- " + publicationCollectionUrl);
					System.out.println("[adminUser] -- " + adminUser);
					System.out.println("[normalUser] -- " + normalUser);
										
					// Main test method
					DSpace_v6 dspace_v6 = null;
					if(serviceDocumentUrl.equals("") || restUrl.equals("") || publicationCollectionUrl.equals("")) {
						log.error("Error: not defined serviceDocumentUrl or metadataReplacementEntryUrl. Please check the repositories.xml file.");
					} else {
						if(!adminUser.equals("") && !normalUser.equals("")) {
							// Enter admin-user password
							System.out.println("\nPassword for \"" + adminUser + "\" [admin]:");
							System.out.println("--> ATTENTION: typo is NOT hidden!");
							char[] adminPassword = scanner.nextLine().toCharArray();
							dspace_v6 = new DSpace_v6(serviceDocumentUrl, restUrl, adminUser, normalUser, adminPassword);
						} else {
							if (!normalUser.equals("")) {
								// Enter user password
								System.out.println("\nPassword for \"" + normalUser + "\" [normal user]:");
								System.out.println("--> ATTENTION: typo is NOT hidden!");
								char[] userPassword = scanner.nextLine().toCharArray();
								dspace_v6 = new DSpace_v6(serviceDocumentUrl, restUrl, normalUser, userPassword);
							}
						}
						
					}
					
					// Test: ExportRepository methods
				    testExportRepository((ExportRepository)dspace_v6, publicationCollectionUrl);
				    
				    // Test: DSpaceRepository methods
				    testDSpaceRepository((DSpaceRepository)dspace_v6, publicationCollectionUrl);
				    				    
				    // Test: DSpace_v6 methods
				    testDSpace_v6(dspace_v6, publicationCollectionUrl);
				    
				}
			}
		
		} catch (Exception ex) {
			log.error("Exception by testing: {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
			ex.printStackTrace();
		}
		
		scanner.close();
	}
	
	
	/**
	 * Testing of ExportRepository methods
	 * 
	 * @param dspaceRepository {@link DSpaceRepository} object
	 * @param exportCollectionUrl {@link String} with the collection URL, where to export (publish)
	 * 
	 */
	private static void testExportRepository(ExportRepository repository, String exportCollectionUrl) {
		
		String output = "\n\n**** Currently testing: ExportRepository methods ****\n\n";
						
		// Check if repository is accessible
		output += "Is repository accessible: " + repository.isRepositoryAccessible() +"\n";
		
		// Check if repository has registered credentials
		output += "Has registered credentials: " + repository.hasRegisteredCredentials() + "\n";
		
		// Check if repository has assigned credentials
		output += "Has assigned credentials: " + repository.hasAssignedCredentials() + "\n";
		
		
		// Get available collections
		output += "== Get available collections == \n";
		Map<String, String> collectionMap = repository.getAvailableCollections();
		if(collectionMap != null) {
			for(Map.Entry<String, String> collection: collectionMap.entrySet()) {
				output += collection.getValue() + " -> URL: " + collection.getKey() + "\n";
			}
		} else {
			output += "Error by getting available collections! \n";
		}
		
		
		// Metadata
		Map<String, List<String>> metadataMap = new HashMap<String, List<String>>();
		metadataMap.put("title", Arrays.asList("ExportRepository test: \"My title.\"")); 			//OK, accepted
		metadataMap.put("not-real-field", Arrays.asList("ExportRepository test: unreal-name")); 	//will not be accepted
		metadataMap.put("publisher", Arrays.asList("ExportRepository test: some publisher")); 		//OK, accepted
		metadataMap.put("author", Arrays.asList("author-1")); 										//will not be accepted
		metadataMap.put("creator", Arrays.asList("creator-1", "creator-2", "creator-3")); 			//OK 
					
		
		// Export metadata only
		output += "\n" + "== PUBLICATION test: METADATA only ==\n";
		metadataMap.put("title", Arrays.asList("ExportRepository test: export metadata as map"));
		output += "\n" + repository.exportNewEntryWithMetadata(exportCollectionUrl, metadataMap);
		
		
		// Export file with metadata
		try {
			output += "\n" + "== Export test: FILE + METADATA ==\n";
			
			// ZIP-package with files only (no extra xml-file)
			metadataMap.put("title", Arrays.asList("ExportRepository test, export map-metadata and file: " + zipPackageFilesOnly.getName()));
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, zipPackageFilesOnly, false, metadataMap) + "\n";
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, zipPackageFilesOnly, true, metadataMap) + "\n";
			
			// ZIP-package with files and extra xml-file
			metadataMap.put("title", Arrays.asList("ExportRepository test, export map-metadata and file: " + zipPackageFilesWithMetadata.getName()));
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, zipPackageFilesWithMetadata, false, metadataMap) + "\n";
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, zipPackageFilesWithMetadata, true, metadataMap) + "\n";
			
			// ZIP-package with metadata only
			metadataMap.put("title", Arrays.asList("ExportRepository test, export map-metadata and file: " + zipPackageFilesMetadataOnly.getName()));
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, zipPackageFilesMetadataOnly, false, metadataMap) + "\n";
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, zipPackageFilesMetadataOnly, true, metadataMap) + "\n";
						
			// ZIP-package with subfolder
			metadataMap.put("title", Arrays.asList("ExportRepository test, export map-metadata and file: " + zipPackageWithSubfolder.getName()));
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, zipPackageWithSubfolder, false, metadataMap) + "\n";
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, zipPackageWithSubfolder, true, metadataMap) + "\n";
			
			// TXT-file
			metadataMap.put("title", Arrays.asList("ExportRepository test, export map-metadata and file: " + txtFile.getName()));
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, txtFile, false, metadataMap) + "\n";
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, txtFile, true, metadataMap) + "\n";
			
			// XML-file (will be exported as a normal file, metadata will not be extracted)
			metadataMap.put("title", Arrays.asList("ExportRepository test, export map-metadata and file: " + xmlMetadataFile.getName()));
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, xmlMetadataFile, false, metadataMap) + "\n";
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, xmlMetadataFile, true, metadataMap) + "\n";
			
			// Other-format-file
			metadataMap.put("title", Arrays.asList("ExportRepository test, export map-metadata and file: " + otherFile.getName()));
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, otherFile, false, metadataMap) + "\n";
			output += repository.exportNewEntryWithFileAndMetadata(exportCollectionUrl, otherFile, true, metadataMap) + "\n";
			
		} catch (IOException e) {
			output += "ERROR! Exception during export: " + e.getMessage() + "\n";
		}
		
		//TODO: further code bellow will be implemented soon (adaptation to the new interface is needed)
		
		//Works well!
		//output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipPackageWithSubfolder, metadataMap);	// true
		
		// TODO: test
		//output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, zipPackageFilesWithMetadata);
		
		
		//TODO: test! Now "In-Progress:true" is ignored -> create Issue in SWORD-Client repository
		//TODO: implement export of metadata as xml-file
		//output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipPackageFilesOnly, xmlMetadataFile); 	//true

				
		System.out.println(output);
	}
	
	
	/**
	 * Testing of DSpaceRepository methods
	 * 
	 * @param dspaceRepository {@link DSpaceRepository} object
	 * @param exportCollectionUrl {@link String} with the collection URL, where to export (publish)
	 * 
	 */
	private static void testDSpaceRepository(DSpaceRepository dspaceRepository, String exportCollectionUrl) {
		
		String output = "\n\n**** Currently testing: DSpaceRepository methods ****\n\n";
		
		// Test for collections with full name
		output += "== User available collections with full name: \n";
		Map<String, String> collectionsWithFullName = dspaceRepository.getAvailableCollectionsWithFullName("//");
		if(collectionsWithFullName != null) {
			for(Map.Entry<String, String> collection: collectionsWithFullName.entrySet()) {
				output += collection.getValue() + "\n";
				output += "-- URL: " + collection.getKey() + "\n";			
			}			
		} else {		
			output += "Error by getting collections with full name! \n";
		}
				
		// Test for communities of the collection
		output += "== Get communities for collection: " + exportCollectionUrl + "\n";
		List<String> communities = dspaceRepository.getCommunitiesForCollection(exportCollectionUrl);
		if(communities != null) {
			for(String community: communities) {
				output += "-- URL: " + community;
			}
		} else {
			output += "Error by getting communities for collection! \n";
		}
		
		// Print output
		System.out.println(output);
	}
	
	
	/**
	 * Testing of DSpace_v6 methods
	 * 
	 * @param dspaceRepository {@link DSpaceRepository} object
	 * @param exportCollectionUrl {@link String} with the collection URL, where to export (publish)
	 * 
	 */
	private static void testDSpace_v6(DSpace_v6 dspace_v6, String exportCollectionUrl) {
	
		String output = "\n\n**** Currently testing: DSpace_v6 methods ****\n\n";
		
		// Is REST accessible
		output += "Is REST accessible: " + dspace_v6.isRestAccessible() + "\n";
				
		// Get collection handle
		output += "Collection: " + exportCollectionUrl + " -> handle: " + dspace_v6.getCollectionHandle(exportCollectionUrl) + "\n";
				
				
		// Test for collections with full name and handle
		output += "== User available collections with full name and handle: \n";
		Map<String, String> collectionsWithFullName = dspace_v6.getAvailableCollectionsWithFullName("//");
		if(collectionsWithFullName != null) {
			for(Map.Entry<String, String> collection: collectionsWithFullName.entrySet()) {
				output += collection.getValue() + "\n";
				output += "-- URL:  " + collection.getKey() + 
								   " -> Handle: " + dspace_v6.getCollectionHandle(collection.getKey()) + "\n";
			}			
		} else {
			output += "Error by getting collections with full name! \n";
		}
		
		
		// Test for collection entries
		output += "== Get collection entries. Collection: " + exportCollectionUrl + "\n";
		Map<String, String> entryMap = dspace_v6.getCollectionEntries(exportCollectionUrl);
		if(entryMap != null) {
			for(Map.Entry<String, String> entry: entryMap.entrySet()){
				output += entry.getValue() + " -> URL: " + entry.getKey() + "\n";
			}
		} else {
			output += "Error by getting collection entries! \n";
		}
		
		
		// Metadata
		Map<String, List<String>> metadataMap = new HashMap<String, List<String>>();
		metadataMap.put("not-real-field", Arrays.asList("DSpace_v6 test: unreal-name")); 	//will not be accepted
		metadataMap.put("publisher", Arrays.asList("DSpace_v6 test: some publisher")); 		//OK, accepted
		metadataMap.put("author", Arrays.asList("author-1")); 								//will not be accepted
		metadataMap.put("creator", Arrays.asList("creator-1", "creator-2", "creator-3")); 	//OK 
		
		
		// Create entry with map-metadata only
		output += "\n" + "== Create entry with map-metadata ==\n";
		metadataMap.put("title", Arrays.asList("DSpace_v6 test, create entry with map-metadata"));
		try {
			output += dspace_v6.createEntryWithMetadata(exportCollectionUrl, metadataMap, inProgress) + "\n";
		} catch (SWORDClientException e) {
			output += "ERROR! Exception during creation of entry with map-metadata: " + e.getMessage() + "\n";
		}
		
		
		// Create entry with map-metadata and file
		try {
			output += "\n" + "== Create entry with metadata and file ==\n";
			
			// ZIP-package with files only (no extra xml-file)
			metadataMap.put("title", Arrays.asList("DSpace_v6 test, create entry with map-metadata and file: " + zipPackageFilesOnly.getName()));
			output += dspace_v6.createEntryWithMetadataAndFile(exportCollectionUrl, zipPackageFilesOnly, false, metadataMap, inProgress) + "\n";
			output += dspace_v6.createEntryWithMetadataAndFile(exportCollectionUrl, zipPackageFilesOnly, true, metadataMap, inProgress) + "\n";
			
			// ZIP-package with files and extra xml-file
			metadataMap.put("title", Arrays.asList("DSpace_v6 test, create entry with map-metadata and file: " + zipPackageFilesWithMetadata.getName()));
			output += dspace_v6.createEntryWithMetadataAndFile(exportCollectionUrl, zipPackageFilesWithMetadata, false, metadataMap, inProgress) + "\n";
			output += dspace_v6.createEntryWithMetadataAndFile(exportCollectionUrl, zipPackageFilesWithMetadata, true, metadataMap, inProgress) + "\n";		
		
		} catch (IOException | SWORDClientException e) {
			output += "ERROR! Exception during entry creation: " + e.getMessage() + "\n";
		}
		
		
		// Create entry with map-metadata and file with "In-Progress: false"
		try {
			output += "\n" + "== Create entry with metadata and file with \"In-Progress: false\" ==\n";
			
			// ZIP-package with files and extra xml-file
			metadataMap.put("title", Arrays.asList("DSpace_v6 test, create entry with map-metadata and file: " + zipPackageFilesWithMetadata.getName() + ", In-Progress: false"));
			output += dspace_v6.createEntryWithMetadataAndFile(exportCollectionUrl, zipPackageFilesWithMetadata, true, metadataMap, false) + "\n"; // "In-Progress: false" explicitly		
		
		} catch (IOException | SWORDClientException e) {
			output += "ERROR! Exception during entry creation with \"In-Progress: false\": " + e.getMessage() + "\n";
		}		
		
		
		// Export new entry with file
		try {
			output += "\n" + "== Export new entry with file ==\n";
			
			// ZIP-package with files only (no extra xml-file)
			output += dspace_v6.exportNewEntryWithFile(exportCollectionUrl, zipPackageFilesOnly, false) + "\n";
			output += dspace_v6.exportNewEntryWithFile(exportCollectionUrl, zipPackageFilesOnly, true) + "\n";
			
			// ZIP-package with files and extra xml-file
			output += dspace_v6.exportNewEntryWithFile(exportCollectionUrl, zipPackageFilesWithMetadata, false) + "\n";
			output += dspace_v6.exportNewEntryWithFile(exportCollectionUrl, zipPackageFilesWithMetadata, true) + "\n";
						
		} catch (IOException e) {
			output += "ERROR! Exception during entry creation: " + e.getMessage() + "\n";
		}
				
		
		// Print output
		System.out.println(output);
	}
	
	
	
	/**
	 * Validate a XML-schema
	 * 
	 * @param fileXSD file with the XML schema
	 * @param fileXML file with the data in XML
	 * @return {@code true} if schema is correct and {@code false} otherwise
	 */
	public static boolean validateXMLSchema(File fileXSD, File fileXML) {
	
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = schemaFactory.newSchema(fileXSD);
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(fileXML));
			return true;
		
		} catch (SAXException | IOException ex) {
			log.error("Exception by XML schema validation: {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
			return false;
		}
	}
	
}
