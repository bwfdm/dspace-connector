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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.client.ServiceDocument;

import bwfdm.connector.dspace.dto.v6.CollectionObject;
import bwfdm.connector.dspace.dto.v6.HierarchyObjectRestV6;
import bwfdm.connector.dspace.utils.JsonUtils;
import bwfdm.connector.dspace.utils.WebUtils;
import bwfdm.connector.dspace.utils.WebUtils.RequestType;


public class DSpace_v6 extends DSpaceSwordOnly {

	private static final Logger log = LoggerFactory.getLogger(DSpace_v6.class);

	// For REST
	//
	// URLs
	protected String restURL;
	protected String communitiesURL;
	protected String collectionsURL;
	protected String hierarchyURL;
	protected String restTestURL;

	private CloseableHttpClient httpClient;

	/**
	 * Create DSpace-v6 object, with activated "on-behalf-of" option, what allows to make a submission only 
	 * with credentials of some privileged account (adminUser, adminPassword) on behalf of other account (standardUser),
	 * without using the password of that account (standardUser). 
	 * <p>
	 * <b>INFO:</b> if you have some problems with the SSL certificate (e.g. some exception 
	 * 		with "input is not a X.509 certificate" message), please add a certificate to your keystore, 
	 *      more info here: 
	 *      <a href="https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore">https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore</a>
	 *      
	 * @param serviceDocumentURL the URL string for the service document
	 * @param restURL the URL string to the REST API
	 * @param adminUser some privileged account, that will make a submission on behalf of other user (standardUser)
	 * @param standardUser user account (traditionally without extra privileges), that will be an owner of the submission 
	 * @param adminPassword password for the privileged account (adminUser)
	 */
	public DSpace_v6(String serviceDocumentURL, String restURL, String adminUser, String standardUser, char[] adminPassword) {

		super(serviceDocumentURL, adminUser, standardUser, adminPassword);
		
		requireNonNull(serviceDocumentURL);
		requireNonNull(restURL);
		requireNonNull(adminUser);
		requireNonNull(standardUser);
		requireNonNull(adminPassword);
		
		this.setAllRestURLs(restURL);
		
		// Traditional way to create client. SSL certificate must be actual in this case.
		// In case of some problems with the certificate (possible exceptions "input is not a X.509 certificate"), 
		// read here -> https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore
		this.httpClient = HttpClientBuilder.create().build();
		
		// In case of problems with SSL - httpClient which ignores the SSL certificate
		//this.httpClient = WebUtils.createHttpClientIgnoringSSL();
	}
	
	
	/**
	 * Create DSpace-v6 object, without "on-behalf-of" option
	 * <p>
	 * <b>INFO:</b> if you have some problems with the SSL certificate (e.g. some exception 
	 * 		with "input is not a X.509 certificate" message), please add a certificate to your keystore, 
	 *      more info here: 
	 *      <a href="https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore">https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore</a>
	 *      
	 * @param serviceDocumentURL the URL string for the service document
	 * @param restURL the URL string to the REST API
	 * @param userName user account
	 * @param userPassword password for the user account
	 */
	public DSpace_v6(String serviceDocumentURL, String restURL, String userName, char[] userPassword) {
	
		super(serviceDocumentURL, userName, userPassword);
		
		requireNonNull(serviceDocumentURL);
		requireNonNull(restURL);
		requireNonNull(userName);
		requireNonNull(userPassword);
		
		this.setAllRestURLs(restURL);

		// Traditional way to create client. SSL certificate must be actual in this case.
		// In case of some problems with the certificate (possible exceptions "input is not a X.509 certificate"), 
		// read here -> https://stackoverflow.com/questions/4325263/how-to-import-a-cer-certificate-into-a-java-keystore
		this.httpClient = HttpClientBuilder.create().build();
		
		// In case of problems with SSL - httpClient which ignores the SSL certificate
		//this.httpClient = WebUtils.createHttpClientIgnoringSSL();
	}
	

	/*
	 * -------------------------- 
	 * 
	 * DSpace-v6 specific methods 
	 * 
	 * --------------------------
	 */
	
	
	public void setAllRestURLs(String restURL) {
		requireNonNull(restURL);
		
		this.restURL = restURL;
		this.communitiesURL = this.restURL + "/communities";
		this.collectionsURL = this.restURL + "/collections";
		this.hierarchyURL = this.restURL + "/hierarchy";
		this.restTestURL = this.restURL + "/test";
	}

	
	/**
	 * Check if REST-interface is accessible.
	 * 
	 * @return {@code true} if REST-API is accessible and {@code false} otherwise
	 */
	public boolean isRestAccessible() {

		final CloseableHttpResponse response = WebUtils.getResponse(this.httpClient, this.restTestURL, RequestType.GET,
				APPLICATION_JSON, APPLICATION_JSON);
		if ((response != null) && (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)) {
			WebUtils.closeResponse(response);
			return true;
		} else {
			if (response != null) {
				WebUtils.closeResponse(response);
			}
			return false;
		}
	}

	
	/**
	 * Get a list of communities for the current collection. Specific only for DSpace-6.
	 * <p>
	 * REST and SWORD requests are used. ServiceDocument must be received already.
	 * 
	 * @param collectionURL - URL of the collection as {@link String}
	 * @param serviceDocument - object of {@link ServiceDocument}
	 * @param hierarchy - object of {@link HierarchyObjectRestV6}
	 * @param existedCollectionObjects - array of {@link CollectionObject}
	 * 
	 * @return a {@code List<String>} of communities (0 or more communities are
	 *         possible) or {@code null} if a collection was not found
	 */
	protected List<String> getCommunitiesForCollection(String collectionURL, ServiceDocument serviceDocument,
			HierarchyObjectRestV6 hierarchy, CollectionObject[] existedCollectionObjects) {

		requireNonNull(collectionURL);
		requireNonNull(serviceDocument);
		requireNonNull(hierarchy);
		requireNonNull(existedCollectionObjects);
		
		String collectionHandle = getCollectionHandle(collectionURL, serviceDocument, existedCollectionObjects);
		if (collectionHandle == null) {
			return null;
		}

		List<String> communityList = new ArrayList<String>(0);

		// Get List of communities or "null", if collection is not found
		communityList = hierarchy.getCommunityListForCollection(hierarchy, collectionHandle, communityList);
		
		// remove "Workspace" - it is not a community, but it is always on the first level of the hierarchy
		if (communityList != null) {
			communityList.remove(0); 
		}
		return communityList; // List of communities ( >= 0) or "null"
	}
	
	
	/**
	 * Get a complete hierarchy of collections as HierarchyObject. REST is used.
	 * Works up DSpace-6.
	 * 
	 * @return {@link HierarchyObjectRestV6}
	 */
	protected HierarchyObjectRestV6 getHierarchyObjectRestV6() {

		final CloseableHttpResponse response = WebUtils.getResponse(this.httpClient, this.hierarchyURL, RequestType.GET, APPLICATION_JSON, APPLICATION_JSON);
		final HierarchyObjectRestV6 hierarchy = JsonUtils.jsonStringToObject(WebUtils.getResponseEntityAsString(response), HierarchyObjectRestV6.class);
		WebUtils.closeResponse(response);
		return hierarchy;
	}

	
	/**
	 * Get a collection handle based on the collection URL.
	 * <p>
	 * REST and SWORDv2 requests are used.
	 * 
	 * @param collectionURL a {@link String} with the URL of the collection 
	 * 
	 * @return String with a handle or {@code null} if collectionURL was not found
	 */
	public String getCollectionHandle(String collectionURL) {

		requireNonNull(collectionURL);
		
		ServiceDocument serviceDocument = super.getServiceDocument(this.serviceDocumentURL);

		// Get all collections via REST to check, if swordCollectionPath contains a REST-handle
		CollectionObject[] existedCollectionObjects = getAllCollectionObjects();
		return getCollectionHandle(collectionURL, serviceDocument, existedCollectionObjects);
	}

	
	/**
	 * Get a collection handle based on the collection URL. Private method with logic.
	 * <p>
	 * REST and SWORDv2 requests are used. ServiceDocument must be already retrieved.
	 * 
	 * @param collectionURL - URL of the collection as {@link String}
	 * @param serviceDocument - object of {@link ServiceDocument}
	 * @param existedCollections - array of {@link CollectionObject}
	 * 
	 * @return String with a handle or {@code null} if collectionURL was not found
	 */
	protected String getCollectionHandle(String collectionURL, ServiceDocument serviceDocument,
			CollectionObject[] existedCollections) {

		requireNonNull(collectionURL);
		requireNonNull(serviceDocument);
		requireNonNull(existedCollections);
		
		String swordCollectionPath = null;
		
		// Find collectionURL inside the repository (via service document and SWORD protocol)
		for(Map.Entry<String, String> entry: super.getCollections(serviceDocument).entrySet()) {
			if(entry.getKey().equals(collectionURL)) {
				swordCollectionPath=entry.getKey();
			}
		}
		// If cllectionURL was found via service document, find the handle
		if(swordCollectionPath != null) {
			// Compare REST-handle and swordCollectionPath
			for (CollectionObject collection : existedCollections) {
				if (swordCollectionPath.contains(collection.handle)) {
					return collection.handle;
				}
			}
		}
				
		return null; //collectionURL was not found 
	}

	
	/**
	 * Get all existed collections as an array of CollectionObject. REST is used.
	 * 
	 * @return {@link CollectionObject}[]
	 */
	protected CollectionObject[] getAllCollectionObjects() {

		final CloseableHttpResponse response = WebUtils.getResponse(this.httpClient, this.collectionsURL, RequestType.GET,
				APPLICATION_JSON, APPLICATION_JSON);
		final CollectionObject[] collections = JsonUtils
				.jsonStringToObject(WebUtils.getResponseEntityAsString(response), CollectionObject[].class);
		WebUtils.closeResponse(response);
		return collections;
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
	 * DSpace-v6: REST and SWORD requests will be used.
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
			// Service document has only collections (default service document). Combine REST and SWORD requests.
			HierarchyObjectRestV6 hierarchy = getHierarchyObjectRestV6();
			CollectionObject[] existedCollectionObjects = getAllCollectionObjects();
			return getCommunitiesForCollection(collectionURL, serviceDocument, hierarchy, existedCollectionObjects);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * DSpace-v6: REST and SWORD requests will be used. 
	 */
	@Override
	public Map<String, String> getAvailableCollectionsWithFullName(String fullNameSeparator) {

		ServiceDocument serviceDocument = super.getServiceDocument(this.serviceDocumentURL);
		if(serviceDocument == null) {
			return null;
		}
		
		// Check, if "service" tag is provided for the collections - not default service document.
		if(super.isServiceDocumentWithSubservices(serviceDocument)) {
			// Get collections with full name via SWORD protocol only
			return super.getCollectionsAsHierarchy(serviceDocument, fullNameSeparator);
		}
						
		// Service document has only collections (default service document). 
		// Combine REST and SWORD requests.
		Map<String, String> collectionsMap = super.getCollections(serviceDocument);
		
		// Get complete hierarchy of collections and array of CollectionOnject-s (REST) 
		final HierarchyObjectRestV6 hierarchy = getHierarchyObjectRestV6();
		final CollectionObject[] existedCollectionObjects = getAllCollectionObjects();

		// Extend collection name with communities and separators
		for (String collectionUrl : collectionsMap.keySet()) {
			List<String> communities = this.getCommunitiesForCollection(collectionUrl, serviceDocument, hierarchy, existedCollectionObjects);
			// Check if communities is null (e.g. wrong collectionUrl)
			if(communities == null) {
				return null; // error
			}
			String fullName = "";
			for (String community : communities) {
				fullName += community + fullNameSeparator; // add community + separator
			}
			fullName += collectionsMap.get(collectionUrl); // add collection name (title)
			collectionsMap.put(collectionUrl, fullName);
		}
		return collectionsMap;
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
		return (isRestAccessible() && super.isSwordAccessible(this.serviceDocumentURL));
	}

}
