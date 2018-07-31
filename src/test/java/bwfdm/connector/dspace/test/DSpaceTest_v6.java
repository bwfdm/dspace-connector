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
import java.util.HashMap;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import bwfdm.connector.dspace.DSpace_v6;

/**
 * Class to test the DSpace_v6 connector
 * 
 * @author Volodymyr Kushnarenko
 */
public class DSpaceTest_v6 {
	
	protected static final Logger log = LoggerFactory.getLogger(DSpaceTest_v6.class);
	
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
					
					String serviceDocumentURL = eElement.getElementsByTagName("serviceDocumentURL").item(0).getTextContent();
					String restURL = eElement.getElementsByTagName("restURL").item(0).getTextContent();
					String publicationCollectionURL = eElement.getElementsByTagName("publicationCollectionURL").item(0).getTextContent();
					String adminUser = eElement.getElementsByTagName("adminUser").item(0).getTextContent();
					String userLogin = eElement.getElementsByTagName("normalUser").item(0).getTextContent();
					
					System.out.println("[serviceDocumentURL] -- " + serviceDocumentURL);
					System.out.println("[restURL] -- " + restURL);
					System.out.println("[publicationCollectionURL] -- " + publicationCollectionURL);
					System.out.println("[adminUser] -- " + adminUser);
					System.out.println("[normalUser] -- " + userLogin);
					
					System.out.println("\nPassword for \"" + adminUser + "\" [admin]:");
					System.out.println("--> ATTENTION: typo is NOT hidden!");
					char[] adminPassword = scanner.nextLine().toCharArray();
				    
				    // Main test method
				    testRepository(serviceDocumentURL, restURL, publicationCollectionURL, adminUser, adminPassword, userLogin);	
				}
			}
		
		} catch (Exception ex) {
			log.error("Exception by testing: {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
		}
		
		scanner.close();
	}
	
	
	/**
	 * The main testing of the publication repository
	 * 
	 * @param serviceDocumentURL
	 * @param restURL
	 * @param publicationCollectionURL
	 * @param adminUser
	 * @param adminPassword
	 * @param userLogin
	 */
	private static void testRepository(String serviceDocumentURL, 
										String restURL, 
										String publicationCollectionURL, 
										String adminUser, 
										char[] adminPassword, 
										String userLogin) {
		
		String output = "";
		
		// Create DSpace_v6 repository
		DSpace_v6 dspaceRepository = new DSpace_v6(serviceDocumentURL,
											restURL,
											adminUser,
											adminPassword
											); 
		
		// Check if repository is accessible -- OK
		output += "Is repo accessible: " + dspaceRepository.isAccessible() +"\n\n";
		
		// Check if the user is registered -- OK
		output += "Is \"" + userLogin + "\" " + "registered: " + dspaceRepository.isUserRegistered(userLogin) + "\n\n";
		
		// Check if the user is allowed to publish in the repository -- OK
		output += "Is \"" + userLogin + "\" assigned:" + dspaceRepository.isUserAssigned(userLogin) + "\n\n";				
		
		// User available collections with full names
		output += "\n" + "== User available collections with full name \n\n";
		for(Map.Entry<String, String> collection: dspaceRepository.getUserAvailableCollectionsWithFullName(userLogin, "//").entrySet()) {//getAvailableCollectionPaths("//", userLogin).entrySet()) {
			output += collection.getValue() + "\n";
			output += "-- URL:  " + collection.getKey() + 
							   " -> Handle: " + dspaceRepository.getCollectionHandle(collection.getKey()) + "\n";
		}
		
		// Admin available collections with full names
		output += "\n" + "== Admin available collections with full name \n\n";
		for(Map.Entry<String, String> collection: dspaceRepository.getAdminAvailableCollectionsWithFullName("//").entrySet()) {
			output += collection.getValue() + "\n";
			output += "-- URL:  " + collection.getKey() + 
							   " -> Handle: " + dspaceRepository.getCollectionHandle(collection.getKey()) + "\n";
		}
			
		File zipPackageFilesOnly = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/package_files_only.zip").getFile());
		File zipPackageFilesWithMetadata = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/package_files_with_metadata.zip").getFile());
		File zipPackageFilesMetadataOnly = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/package_metadata_only.zip").getFile());
		File zipPackageWithSubfolder = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/package_with_subfolder.zip").getFile());
		File xmlMetadataFile = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/entry.xml").getFile());
		File txtFile = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/test-file.txt").getFile());
		File otherFile = new File(DSpaceTest_v6.class.getClassLoader().getResource("testfiles/test-file.with.dots.txt.t").getFile());
		
		// Metadata
		Map<String, String> metadataMap = new HashMap<String, String>();
		metadataMap.put("title", "My title !!!"); 			//OK, accepted
		metadataMap.put("not-real-field", "unreal-name"); 	//will not be accepted
		metadataMap.put("publisher", "some publisher"); 	//OK, accepted
		metadataMap.put("author", "author-1"); 				//will not be accepted
		//metadataMap.put("dc.contributor.author", "author-2"); 
		
		
//		// Test publication: file only -- OK
//		output += "\n" + "== PUBLICATION test: FILE only ==\n";
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, zipFile);			//true
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, xmlFile);			//true
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, txtFile);			//true
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, otherFile);			//true
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, complicatedFile);	//true
		
		
//		// Test publication: metadata only -- OK
//		output += "\n" + "== PUBLICATION test: METADATA only ==\n";
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, metadataMap);	//true
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, xmlFile); 		//true
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, txtFile); 		//false
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, otherFile); 		//false
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, complicatedFile);//false
//		output += "\n" + dspaceRepository.publishMetadata(userLogin, publicationCollectionURL, zipFile); 		//false
		
		
		// Test publication: file + metadata -- OK
		output += "\n" + "== PUBLICATION test: FILE + METADATA ==\n";
		// Works well!
//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipPackageFilesOnly, metadataMap);		// true
//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipPackageFilesWithMetadata, metadataMap);// true	
		
		//Works well!
		//output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipPackageWithSubfolder, metadataMap);	// true
		
		// TODO: test
//		output += "\n" + dspaceRepository.publishFile(userLogin, publicationCollectionURL, zipPackageFilesWithMetadata);
		
		
		//TODO: test! Now "In-Progress:true" is ignored -> create Issue in SWORD-Client repository
		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipPackageFilesOnly, xmlMetadataFile); 	//true

//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, txtFile, xmlMetadataFile); 	//true

//		
//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, otherFile, otherFile); 	//false
//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, otherFile, metadataMap); 	//true
//		
//		
//		
//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, xmlMetadataFile, xmlMetadataFile); 				//true
//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, txtFile, xmlMetadataFile); 				//true
//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, otherFile, otherFile); 			//false
//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipPackageWithSubfolder, zipPackageWithSubfolder);//false
//		output += "\n" + dspaceRepository.publishFileAndMetadata(userLogin, publicationCollectionURL, zipPackageFilesOnly, xmlMetadataFile); 				//true
//				
		System.out.println(output);
	}
	
	
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
