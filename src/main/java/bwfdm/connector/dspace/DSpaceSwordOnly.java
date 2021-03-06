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
package bwfdm.connector.dspace;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.client.Content;
import org.swordapp.client.DepositReceipt;
import org.swordapp.client.ProtocolViolationException;
import org.swordapp.client.SWORDClientException;
import org.swordapp.client.SWORDError;
import org.swordapp.client.ServiceDocument;
import org.swordapp.client.SwordResponse;
import org.swordapp.client.UriRegistry;

import bwfdm.connector.dspace.utils.IOUtils;
import bwfdm.exporter.commons.SwordExporter;

public class DSpaceSwordOnly extends SwordExporter implements DSpaceRepository {

	private static final Logger log = LoggerFactory.getLogger(DSpaceSwordOnly.class);

	// For SWORD
	protected String serviceDocumentURL;

	
	/**
	 * Create DSpaceSwordOnly object, with activated "on-behalf-of" option, what allows to make a submission only 
	 * with credentials of some privileged account (adminUser, adminPassword) on behalf of other account (standardUser),
	 * without using the password of that account (standardUser). 
	 * <p>
	 * <b>IMPORTANT:</b> to get correct results for some methods 
	 * (e.g. {@link #getAvailableCollectionsWithFullName(String)}) the service document has to support "service" tag
	 * inside the "collection" tag, what means, that service document has to provides communities instead of collection.
	 * To check it, please use the method {@link #isServiceDocumentWithSubservices(ServiceDocument)} before - 
	 * it has to return {@code true}.
	 * <p>
	 * <b>INFO:</b> if you have some problems with the SSL certificate (e.g. some exception 
	 * 		with "input is not a X.509 certificate" message), please add a certificate to your keystore, 
	 *      more info here: 
	 *      <a href="https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore">https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore</a>
	 *      
	 * @param serviceDocumentURL the URL string for the service document
	 * @param adminUser some privileged account, that will make a submission on behalf of other user (standardUser)
	 * @param standardUser user account (traditionally without extra privileges), that will be an owner of the submission 
	 * @param adminPassword password for the privileged account (adminUser)
	 */
	public DSpaceSwordOnly(String serviceDocumentURL, String adminUser, String standardUser, char[] adminPassword) {

		super(SwordExporter.createAuthCredentials(adminUser, adminPassword, standardUser));
		
		requireNonNull(serviceDocumentURL);
		requireNonNull(adminUser);
		requireNonNull(standardUser);
		requireNonNull(adminPassword);
		
		this.setServiceDocumentURL(serviceDocumentURL);
	}
	
	
	/**
	 * Create DSpaceSwordOnly object, without "on-behalf-of" option
	 * <p>
	 * <b>IMPORTANT:</b> to get correct results for some methods 
	 * (e.g. {@link #getAvailableCollectionsWithFullName(String)}) the service document has to support "service" tag
	 * inside the "collection" tag, what means, that service document has to provides communities instead of collection.
	 * To check it, please use the method {@link #isServiceDocumentWithSubservices(ServiceDocument)} before - 
	 * it has to return {@code true}.
	 * <p>
	 * <b>INFO:</b> if you have some problems with the SSL certificate (e.g. some exception 
	 * 		with "input is not a X.509 certificate" message), please add a certificate to your keystore, 
	 *      more info here: 
	 *      <a href="https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore">https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore</a>
	 *      
	 * @param serviceDocumentURL the URL string for the service document
	 * @param userName user account
	 * @param userPassword password for the user account
	 */
	public DSpaceSwordOnly(String serviceDocumentURL, String userName, char[] userPassword) {
	
		super(SwordExporter.createAuthCredentials(userName, userPassword));
		
		requireNonNull(serviceDocumentURL);
		requireNonNull(userName);
		requireNonNull(userPassword);
		
		this.setServiceDocumentURL(serviceDocumentURL);
	}
	

	/*
	 * ----------------------- 
	 * 
	 * DSpace specific methods 
	 * 
	 * -----------------------
	 */

	public void setServiceDocumentURL(String serviceDocumentURL) {
		this.serviceDocumentURL = serviceDocumentURL;
	}
	
	public String getServiceDocumentURL() {
		return this.serviceDocumentURL;
	}

	
	/**
	 * Export metadata only to some already existed entry (via edit-URL, {@link SwordRequestType}) or create a Private method which can use different request types. See
	 * {@link SwordRequestType}.
	 * 
	 * @param url - collection URL (with "collection" substring inside) or item URL (with "edit" substring inside)
	 * 				where to to export (or edit) metadata 
	 * @param metadataMap - metadata as a Map
	 * @param swordRequestType - object of {@link SwordRequestType}
	 * @param inProgress {@code boolean} value for the "In-Progress" header 
	 * 		  <p>
	 * 	      For DSpace "In-Progress: true" means, that export will be done at first to the user's workspace, 
	 *        where further editing of the exported element is possible. And "In-Progress: false" means export directly 
	 *        to the workflow, without a possibility of further editing.
	 *
	 * @return {@link String} with the URL to edit the entry (with "edit" substring inside)
	 * 
	 * @throws IOException in case of IO error
	 * @throws SWORDClientException in case of SWORD error
	 * @throws SWORDError in case of SWORD error
	 * @throws ProtocolViolationException in case of SWORD error
	 */
	protected String exportMetadataAsMap(String url, Map<String, List<String>> metadataMap,
			SwordRequestType swordRequestType, boolean inProgress)throws IOException, SWORDClientException, SWORDError, ProtocolViolationException {

		SwordResponse response = super.exportElement(url, swordRequestType, SwordExporter.MIME_FORMAT_ATOM_XML, 
				UriRegistry.PACKAGE_BINARY, null, metadataMap, inProgress);
		
		if(response instanceof DepositReceipt) {
			return ((DepositReceipt)response).getEditLink().getHref(); //response from DEPOSIT request
		} else {
			//FIXME: 	
			//current implementation of SWORD-Client library returns in case of "REPLACE" request
			//the SwordResponse object only with the actual status code, 
			//"Location" link and other fields are "null". That's why to avoid misunderstanding, 
			//when "Location" link is null we return just an input-url (which also should be the "edit" url 
			//in case of REPLACE request) 
			//			
			String editURL = response.getLocation(); //should be a string with the edit URL ("/swordv2/edit/" substring inside)
			return ((editURL != null) ? editURL : url); // return input-url if "Location" link is null
		}
	}

	
	/**
	 * Private method which can use different request types. See
	 * {@link SwordRequestType}.
	 * 
	 * @param url - collection URL (with "collection" substring inside) or item URL (with "edit" substring inside)
	 * 				where to to export (or edit) metadata
	 * @param metadataFileXML 
	 * 			  - file in XML-format (ATOM format of the metadata description) and
	 *            	with an XML-extension  
	 * @param swordRequestType - object of {@link SwordRequestType}
	 * @param inProgress {@code boolean} value for the "In-Progress" header 
	 * 		  <p>
	 * 	      For DSpace "In-Progress: true" means, that export will be done at first to the user's workspace, 
	 *        where further editing of the exported element is possible. And "In-Progress: false" means export directly 
	 *        to the workflow, without a possibility of further editing.
	 * 
	 * @return {@link String} with the URL to edit the entry (with "edit" substring inside)
	 * 
	 * @throws IOException in case of IO error
	 * @throws SWORDClientException in case of SWORD error
	 * @throws SWORDError in case of SWORD error
	 * @throws ProtocolViolationException in case of SWORD error
	 */
	protected String exportMetadataAsFile(String url, File metadataFileXML, SwordRequestType swordRequestType, boolean inProgress) 
				throws IOException, SWORDClientException, SWORDError, ProtocolViolationException{

		// Check if file has an XML-extension
		String ext = super.getFileExtension(metadataFileXML.getName()).toLowerCase();
		if (!ext.equals("xml")) {
			log.error("Wrong metadata file extension: {} : Supported extension is: {}",	ext, "xml");
			throw new ProtocolViolationException("Wrong metadta file extension: " + ext + " : Supported extension is: xml");
		}

		String mimeFormat = SwordExporter.MIME_FORMAT_ATOM_XML;
		String packageFormat = super.getPackageFormat(metadataFileXML.getName());
		SwordResponse response = super.exportElement(url, swordRequestType, mimeFormat, packageFormat, metadataFileXML, null, inProgress);
		
		if(response instanceof DepositReceipt) {
			return ((DepositReceipt)response).getEditLink().getHref(); //response from DEPOSIT request
		} else {
			//FIXME: 	
			//current implementation of SWORD-Client library returns in case of "REPLACE" request
			//the SwordResponse object only with the actual status code, 
			//"Location" link and other fields are "null". That's why to avoid misunderstanding, 
			//when "Location" link is null we return just an input-url (which also should be the "edit" url 
			//in case of REPLACE request) 
			//			
			String editURL = response.getLocation(); //should be a string with the edit URL ("/swordv2/edit/" substring inside)
			return ((editURL != null) ? editURL : url); // return input-url if "Location" link is null
		}
	}
	
	
	/**
	 * Export (create) a new entry with a file in some collection, which is available for the current authentication credentials.
	 * <p>
	 * IMPORTANT: authentication credentials are used implicitly. 
	 * Definition of the credentials is realized via the class constructor.
	 * 
	 * IMPORTANT: the header "In-Progress: true" will be used implicitly.
	 *
	 * @param collectionURL the full URL of the collection, where the export (ingest) will be done 
	 * @param file an archive file with one or more files inside (e.g. ZIP-file as a standard) or a binary file 
	 * 			which will be exported.
	 * @param unpackFileIfArchive should be used for archive files (e.g. ZIP). A flag which decides, 
	 * 			if the exported archive will be unpacked in the repository ({@code true} value,
	 * 			new entry will include in this case all files of the archive file) or archive will be exported 
	 * 			as a binary file ({@code false} value, new entry will include only 1 file - the exported archive
	 * 			as a binary file). <b>NOTE:</b> if unpacking is not supported by the repository, 
	 * 			please use {@code false} value.
	 *
	 * @return {@link String} with the URL of the new created entry or {@code null} in case of error.	
	 *
	 * @throws IOException in case of IO error
	 */
	public String exportNewEntryWithFile(String collectionURL, File file, boolean unpackFileIfArchive) throws IOException {

		requireNonNull(collectionURL);
		requireNonNull(file);
		requireNonNull(unpackFileIfArchive);
		
		String mimeFormat = SwordExporter.MIME_FORMAT_ZIP; // for every file type, to publish even "XML" files as a normal file
		String packageFormat = SwordExporter.getPackageFormat(file.getName(), unpackFileIfArchive); // unpack zip-archive or export as a binary 
		
		try {
			SwordResponse response = super.exportElement(collectionURL, SwordRequestType.DEPOSIT, mimeFormat, packageFormat, file, null, true); //use "In-Progress: true" implicitly
			if(response instanceof DepositReceipt) {
				return ((DepositReceipt)response).getEditLink().getHref(); // "edit" URL from the DEPOSIT receipt
			} else {
				return null; // for current moment we should receipt a DepositReceipt object. If not, that something went wrong. 
			}
		} catch (SWORDClientException | SWORDError | ProtocolViolationException e) {
			log.error("Exception by exporting new entry with file only: {}: {}", e.getClass().getSimpleName(), e.getMessage());
			return null;
		}
	}

	
	/**
	 * TODO: move method declaration and javadoc to the SwordExporter abstract class.
	 * 
	 * Create new entry with metadata only (without any file) in some collection.
	 * Metadata are described as a XML-file.
	 * <p>
	 * IMPORTANT: authentication credentials are used implicitly. 
	 * Definition of the credentials is realized via the class constructor.
	 *
	 * @param collectionURL holds the collection URL where the metadata will be exported to.
	 * @param metadataFileXml XML-file with metadata in ATOM format.
	 * @param inProgress {@code boolean} value for the "In-Progress" header 
	 * 		  <p>
	 * 	      For DSpace "In-Progress: true" means, that export will be done at first to the user's workspace, 
	 *        where further editing of the exported element is possible. And "In-Progress: false" means export directly 
	 *        to the workflow, without a possibility of further editing.
	 * 
	 * @return {@link String} with the entry URL which includes "/swordv2/edit/" substring inside. 
	 * 		This URL could be used without changes for further update of the metadata 
	 * 		(see {@link #replaceMetadataEntry(String, Map, boolean) replaceMetadataEntry(entryURL, metadataMap, inProgress)}) 
	 * 		<p>
	 * 		<b>IMPORTANT for Dataverse repository:</b> for further update/extension of the media part 
	 * 		(e.g. uploaded files inside the dataset) please replace "/swordv2/edit/" substring inside the entry URL to 
	 * 		"/swordv2/edit-media/". 
	 * 		For more details please visit <a href="http://guides.dataverse.org/en/latest/api/sword.html">http://guides.dataverse.org/en/latest/api/sword.html</a>
	 * 		<p>
	 * 		<b>IMPORTANT for DSpace repository:</b> further update/extension of the media part (e.g. uploaded files)
	 * 		via SWORD is not supported, only update of the metadata is allowed.
	 * 
	 * @throws IOException in case of IO error
	 * @throws SWORDClientException in case of SWORD error
	 */
	public String createEntryWithMetadata(String collectionURL, File metadataFileXml, boolean inProgress) throws IOException, SWORDClientException{
		
		requireNonNull(collectionURL);
		requireNonNull(metadataFileXml);
		requireNonNull(inProgress);
		
		try {
			return exportMetadataAsFile(collectionURL, metadataFileXml, SwordRequestType.DEPOSIT, inProgress);
		} catch (ProtocolViolationException | SWORDError e) {
			throw new SWORDClientException("Exception by creation of item with only metadta as XML-file: " 
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
	
	/**
	 * TODO: move method declaration and javadoc to the SwordExporter abstract class.
	 * 
	 * Create a new entry with some file and metadata in the provided collection.
	 * Metadata are described as a XML-file.
	 * <p>
	 * IMPORTANT: authentication credentials are used implicitly. 
	 * Definition of the credentials is realized via the class constructor.
	 * <p>
	 * For DSpace: export will be realized in 2 steps: 1 - export a file (create a new entry), 
	 * 2 - add metadata via PUT request.
	 * 
	 * @param collectionURL holds the collection URL where items will be exported to, usually has "collection" substring inside
	 * @param metadataFileXml holds the metadata which is necessary for the ingest
	 * @param file holds a file which can contain one or multiple files
	 * @param unpackZip decides whether to unpack the zipfile or places the packed zip file as uploaded data
	 * @param inProgress {@code boolean} value for the "In-Progress" header 
	 * 		  <p>
	 * 	      For DSpace "In-Progress: true" means, that export will be done at first to the user's workspace, 
	 *        where further editing of the exported element is possible. And "In-Progress: false" means export directly 
	 *        to the workflow, without a possibility of further editing.
	 *
	 * @return {@link String} with the entry URL which includes "/swordv2/edit/" substring inside. 
	 * 		This URL could be used without changes for further update of the metadata 
	 * 		(see {@link #replaceMetadataEntry(String, Map, boolean) replaceMetadataEntry(entryURL, metadataMap, inProgress)}) 
	 * 		<p>
	 * 		<b>IMPORTANT for Dataverse repository:</b> for further update/extension of the media part 
	 * 		(e.g. uploaded files inside the dataset) please replace "/swordv2/edit/" substring inside the entry URL to 
	 * 		"/swordv2/edit-media/". 
	 * 		For more details please visit <a href="http://guides.dataverse.org/en/latest/api/sword.html">http://guides.dataverse.org/en/latest/api/sword.html</a>
	 * 		<p>
	 * 		<b>IMPORTANT for DSpace repository:</b> further update/extension of the media part (e.g. uploaded files)
	 * 		via SWORD is not supported, only update of the metadata is allowed.
	 *
	 * @throws IOException in case of IO error
	 * @throws SWORDClientException in case of SWORD error
	 */
	public String createEntryWithMetadataAndFile(String collectionURL, File metadataFileXml, File file, boolean unpackZip, boolean inProgress)
			throws IOException, SWORDClientException {
		
		requireNonNull(collectionURL);
		requireNonNull(file);
		requireNonNull(unpackZip);
		requireNonNull(metadataFileXml);
		
		String mimeFormat = SwordExporter.MIME_FORMAT_ZIP; // as a common file (even for XML-file)
		String packageFormat = SwordExporter.getPackageFormat(file.getName(), unpackZip);

		try {
			// Step 1: export file (as file or archive), without metadata
			SwordResponse response = super.exportElement(collectionURL, SwordRequestType.DEPOSIT, mimeFormat, 
					packageFormat, file, null, true); // "POST" request (DEPOSIT). Use "In-Progress: true" explicitly, to avoid unwanted publication already on the 1st step
			String editLink = response.getLocation();
			if (editLink == null) {
				throw new SWORDClientException("Error by exporting file and metadta as xml-file: "
						+ "after the file export the item URL for editing (as a response) is null. "
						+ "Not possible to add metadata as the next step.");
			}
			
			// Step 2: add metadata (as a XML-file)
			//
			// "PUT" request (REPLACE) is used to overwrite some previous automatically generated metadata
			return exportMetadataAsFile(editLink, metadataFileXml, SwordRequestType.REPLACE, inProgress);
	
			// NOTE: if replace order (step 1: export metadata, step 2: export file) --> Bad request, ERROR 400
			
		} catch (ProtocolViolationException | SWORDError e) {
			throw new SWORDClientException("Exception by exporting file and metadta as xml-file: " 
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
		
	
	/*
	 * ---------------------------------
	 * 
	 * SwordExporter methods realization
	 * 
	 * ---------------------------------
	 */

	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Implementation via parsing of response String using regular expressions.
	 * 
	 */
	@Override
	public Map<String, String> getCollectionEntries(String collectionUrl) {
		
		requireNonNull(collectionUrl);
		
		Map<String, String> entriesMap = new HashMap<String, String>();
		
		try {
			// Get request on collectionUrl, same as via "curl" 
			// -> curl -i $collectionUrl --user "$USER_MAIL:$USER_PASSWORD"
			Content content = super.getSwordClient().getContent(collectionUrl, SwordExporter.MIME_FORMAT_ATOM_XML, 
					UriRegistry.PACKAGE_SIMPLE_ZIP, super.getAuthCredentials());
			try {
				String response = IOUtils.readStream(content.getInputStream());
				
				Pattern entryPattern = Pattern.compile("<entry>(.+?)</entry>", Pattern.DOTALL | Pattern.MULTILINE); //e.g. "<entry>some_entry_with_other_tags_inside</entry>
				Matcher entryMatcher = entryPattern.matcher(response);
				
				// Find all entries
				while(entryMatcher.find()) {
					String entryString = entryMatcher.group(1);
					
					Pattern idPattern = Pattern.compile("<id>(.+?)</id>", Pattern.DOTALL | Pattern.MULTILINE); //e.g. "<id>https://some_link</id>"
					Matcher idMatcher = idPattern.matcher(entryString);
					
					Pattern titlePattern = Pattern.compile("<title.+?>(.+?)</title>", Pattern.DOTALL | Pattern.MULTILINE); //e.g. "<title type="text">some_title</title>" 
					Matcher titleMatcher = titlePattern.matcher(entryString);
					
					// Find id and title
					if(idMatcher.find() && titleMatcher.find()) { 
						entriesMap.put(idMatcher.group(1), titleMatcher.group(1));
					}
				}
			} catch (IOException e) {
				log.error("Exception by converting Bitstream to String: {}: {}", e.getClass().getSimpleName(), e.getMessage());
				return null;
			}	
		} catch (SWORDClientException | ProtocolViolationException | SWORDError e) {
			log.error("Exception by getting content (request) via SWORD: {}: {}", e.getClass().getSimpleName(), e.getMessage());
			return null;
		}
		return entriesMap;
	}
	
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createEntryWithMetadata(String collectionURL, Map<String, List<String>> metadataMap, boolean inProgress) 
			throws SWORDClientException {
		
		requireNonNull(collectionURL);
		requireNonNull(metadataMap);
		
		try {			
			return exportMetadataAsMap(collectionURL, metadataMap, SwordRequestType.DEPOSIT, inProgress);			
		} catch (IOException | ProtocolViolationException | SWORDError e) {
			throw new SWORDClientException("Exception by export metadta as Map: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
		
	/**
	 * {@inheritDoc}
	 * <p>
	 * DSpace: export will be realized in 2 steps: 1 - export a file (create a new entry), 
	 * 2 - add metadata via PUT request.
	 */
	@Override
	public String createEntryWithMetadataAndFile(String collectionURL, Map<String, List<String>> metadataMap, File file, boolean unpackZip, boolean inProgress)
			throws IOException, SWORDClientException {
		
		requireNonNull(collectionURL);
		requireNonNull(file);
		requireNonNull(unpackZip);
		requireNonNull(metadataMap);
		requireNonNull(inProgress);
		
		String mimeFormat = SwordExporter.MIME_FORMAT_ZIP; // as a common file (even for XML-file)
		String packageFormat = SwordExporter.getPackageFormat(file.getName(), unpackZip);
		
		try {
			// Step 1: export file (as file or archive), without metadata
			SwordResponse response = super.exportElement(collectionURL, SwordRequestType.DEPOSIT, mimeFormat, 
					packageFormat, file, null, true); // "POST" request (DEPOSIT). Use "In-Progress: true" explicitly, to avoid unwanted publication already on the 1st step 
			String editLink = response.getLocation();
			if (editLink == null) {
				throw new SWORDClientException("Error by export file and metadta as Map: "
						+ "after the file export the item URL for editing (as a response) is null. "
						+ "Not possible to add metadata as the next step.");
			}
			
			// Step 2: add metadata (as a Map structure)
			//
			//"PUT" request (REPLACE) is used to overwrite some previous automatically generated metadata
			return exportMetadataAsMap(editLink, metadataMap, SwordRequestType.REPLACE, inProgress);
						
			// NOTE: if replace order (step 1: export metadata, step 2: export file) --> Bad request, ERROR 400
			
		} catch (ProtocolViolationException | SWORDError e) {
			throw new SWORDClientException("Exception by export file and metadta as Map: " 
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		
	}
		
	
	/*
	 * -----------------------------------------
	 * 
	 * DSpaceRepository interface implementation
	 * 
	 * -----------------------------------------
	 */
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * DSpaceSwordOnly: SWORD requests will be used.
	 */
	@Override
	public List<String> getCommunitiesForCollection(String collectionURL) {

		ServiceDocument serviceDocument = super.getServiceDocument(this.serviceDocumentURL);
		if(serviceDocument == null) {
			return null;
		}

		// Check, if "service" tag is provided for the collections - not default service document.
		if(super.isServiceDocumentWithSubservices(serviceDocument)) {
			// Get communities for collections via SWORD protocol only, with usage of HierarchyObject class
			return super.getHierarchy(serviceDocument).getServiceHierarchyForCollection(collectionURL);
		} else {			
			// Service document has only collections (default service document). Error for SwordOnly case.
			return null;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * DSpaceSwordOnly: SWORD requests will be used. 
	 */
	@Override
	public Map<String, String> getAvailableCollectionsWithFullName(String fullNameSeparator) {

		// Get available collections from the ServiceDocument (SWORD)
		ServiceDocument serviceDocument = super.getServiceDocument(this.serviceDocumentURL);
		if(serviceDocument == null) {
			return null;
		}
		
		// Check, if "service" tag is provided for the collections - not default service document.
		if(super.isServiceDocumentWithSubservices(serviceDocument)) {
			// Get collections with full name via SWORD protocol only
			return super.getCollectionsAsHierarchy(serviceDocument, fullNameSeparator);
		} else {
			// Service document has only collections (default service document). Error for SwordOnly case.
			return null;
		}
	}
		
	
	/*
	 * -----------------------------------------
	 * 
	 * ExportRepository interface implementation
	 * 
	 * -----------------------------------------
	 */
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * For DSpace it is done by access to the Service Document via SWORD-protocol
	 * and checking an access to the REST-API.
	 * 
	 * @return {@code true} if service document and REST-API are accessible, and
	 *         {@code false} otherwise (e.g. by Error 403).
	 */
	@Override
	public boolean isRepositoryAccessible() {
		return super.isSwordAccessible(this.serviceDocumentURL);
	}

	
	/**
	 * {@inheritDoc}
	 * <p>
	 * In DSpace it will be checked via access to the service document
	 * (SWORD-protocol).
	 */
	@Override
	public boolean hasRegisteredCredentials() {
		return super.getServiceDocument(this.serviceDocumentURL) != null;
	}

	
	/**
	 * {@inheritDoc}
	 * <p>
	 * In DSpace it will be checked via access to the service document and 
	 * count of available for the current authentication credentials collections.
	 * 
	 * @return {@code true} if there is at least 1 collection for export, 
	 * 			and {@code false} if there are not available collections.
	 */
	@Override
	public boolean hasAssignedCredentials() {
		
		ServiceDocument serviceDocument = super.getServiceDocument(this.serviceDocumentURL);
		if ((serviceDocument != null) && (super.getCollections(serviceDocument).size() > 0)) {
			return true;
		}
		return false;
	}

	
	/**
	 * {@inheritDoc}
	 * <p>
	 * DSpaceSwordOnly: SWORD requests will be done.
	 */
	@Override
	public Map<String, String> getAvailableCollections() {
		ServiceDocument serviceDocument = super.getServiceDocument(this.serviceDocumentURL);
		return super.getCollections(serviceDocument);
	}

	
	/**
	 * {@inheritDoc}
	 * <p>
	 * DSpaceSwordOnly: SWORD requests will be done.
	 * <p>
	 * <b>IMPORTANT:</b> the header "In-Progress: true" will be used implicitly. 
	 * 		        New entry will be placed in the user's workspace for further editing before the final publication.  
	 */
	@Override
	public String exportNewEntryWithMetadata(String collectionURL, Map<String, List<String>> metadataMap) {
		try {
			return this.createEntryWithMetadata(collectionURL, metadataMap, true); // "In-Progress: true" is used implicitly
		} catch (SWORDClientException e) {
			log.error("Exception by creation of new entry with metadata as Map.", e);
			return null;
		}
	}

	
	/**
	 * {@inheritDoc}
	 * <p>
	 * DSpaceSwordOnly: SWORD requests will be done.
	 * <p>
	 * <b>IMPORTANT:</b> the header "In-Progress: true" will be used implicitly. 
	 * 		        New entry will be placed in the user's workspace for further editing before the final publication. 
	 */
	@Override
	public String exportNewEntryWithMetadataAndFile(String collectionURL, Map<String, List<String>> metadataMap, 
			File file, boolean unpackFileIfArchive) throws IOException {
		
		try {
			return this.createEntryWithMetadataAndFile(collectionURL, metadataMap, file, unpackFileIfArchive, true); // "In-Progress: true" is used implicitly
		} catch (SWORDClientException e) {
			log.error("Exception by creation of new entry with file and metadata as Map.", e);
			return null;
		}
	}
}
