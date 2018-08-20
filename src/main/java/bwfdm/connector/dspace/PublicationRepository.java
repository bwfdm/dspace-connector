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

import java.io.File;

/**
 * General Interface for the publication repository.
 * 
 * @author Stefan Kombrink
 * @author Volodymyr Kushnarenko
 */

import java.util.Map;

public interface PublicationRepository {

	/**
	 * Check if publication repository is accessible via API
	 * 
	 * @return {@code true} if repository is accessible and {@code false} otherwise
	 */
	public boolean isAccessible();

	/**
	 * Set login and password of the user. Password is needed for the communication
	 * with the publication repository via API (e.g. SWORD or REST)
	 * <p>
	 * If the publication repository is DSpace you should put login/password ONLY of
	 * the admin-user. Credentials of the admin-user will be used for the REST/SWORD
	 * mechanism. This mechanism is needed because of limitations of DSpace-API,
	 * where password is always needed.
	 * <p>
	 * 
	 * @param user - user login
	 * @param password - user password
	 */
	public void setCredentials(String user, char[] password);

	/**
	 * Check if user is registered in the publication repository
	 * 
	 * @param loginName - user login name
	 * 
	 * @return {@code true} if user is registered and {@code false} otherwise
	 */
	public boolean isUserRegistered(String loginName);

	/**
	 * Check if user is assigned to publish something in the repository
	 *
	 * @param loginName - user login name
	 * 
	 * @return {@code true} if count of user available collections is great than
	 *         zero, otherwise {@code false}
	 */
	public boolean isUserAssigned(String loginName);

	/**
	 * Get collections, which are available for the user Could be, that user has an
	 * access only for some specific collections.
	 * 
	 * @param loginName - user login name
	 * 
	 * @return Map of Strings, where key="Collection full URL", value="Collection
	 *         title", or empty Map if there are not available collections.
	 */
	public Map<String, String> getUserAvailableCollectionsWithTitle(String loginName);

	/**
	 * Get collections, which are available for the user, and show their full name
	 * (e.g. for DSpace-repository it means "community/subcommunity/collection")
	 * <p>
	 * Could be, that user has an access only for some specific collections.
	 * 
	 * @param loginName - user login name
	 * @param fullNameSeparator - separator between name parts, as a {@link String}, e.g. "/"
	 *            
	 * @return Map of Strings, where key="Collection full URL", value="Collection
	 *         full name", or empty Map if there are not available collections.
	 */
	public Map<String, String> getUserAvailableCollectionsWithFullName(String loginName, String fullNameSeparator);

	/**
	 * Get available for the admin-user collections for publication. As credentials
	 * for the request are used login/password of the admin-user
	 * 
	 * @return Map of Strings, where key="Collection full URL", value="Collection
	 *         title", or empty Map if there are not available collections.
	 */
	public Map<String, String> getAdminAvailableCollectionsWithTitle();

	/**
	 * Get available for the admin-user collections with full name (e.g. for
	 * DSpace-repository it means "community/subcommunity/collection")
	 * <p>
	 * As credentials for the request are used login/password of the admin-user
	 * 
	 * @param fullNameSeparator - separator between name parts, as a {@link String}, e.g. "/"
	 * 
	 * @return Map of Strings, where key="Collection full URL", value="Collection
	 *         full name", or empty Map if there are not available collections.
	 */
	public Map<String, String> getAdminAvailableCollectionsWithFullName(String fullNameSeparator);

	/**
	 * Publish a file to some collections, which is available for the user.
	 * 
	 * @param userLogin - user login name
	 * @param collectionURL - URL of the collection where to publish
	 * @param fileFullPath - full path to the file 
	 * 
	 * @return {@code true} if publication was successful and {@code false} otherwise (e.g. some error has occurred)
	 */
	public boolean publishFile(String userLogin, String collectionURL, File fileFullPath);

	/**
	 * Publish metadata only (without any file) to some collection, which is
	 * available for the user. Metadata are described as a {@link java.util.Map}.
	 * 
	 * @param userLogin - user login name
	 * @param collectionURL - URL of the collection where to publish
	 * @param metadataMap - metadata as a Map
	 * 
	 * @return {@code true} if publication was successful and {@code false} otherwise (e.g. some error has occurred)
	 */
	public boolean publishMetadata(String userLogin, String collectionURL, Map<String, String> metadataMap);

	/**
	 * Publish metadata only (without any file) to some collection, which is
	 * available for the user. Metadata are described in the xml-file.
	 * 
	 * @param userLogin - user login name
	 * @param collectionURL - URL of the collection where to publish
	 * @param metadataFileXML - metadata as a file in XML-format
	 * 
	 * @return {@code true} if publication was successful and {@code false} otherwise (e.g. some error has occurred)
	 */
	public boolean publishMetadata(String userLogin, String collectionURL, File metadataFileXML);

	/**
	 * Publish a file together with the metadata. Metadata are described as a
	 * {@link java.util.Map}.
	 * 
	 * @param userLogin - user login name
	 * @param collectionURL - URL of the collection where to publish
	 * @param fileFullPath - full path to the file 
	 * @param metadataMap - metadata as a Map
	 * 
	 * @return {@code true} if publication was successful and {@code false} otherwise (e.g. some error has occurred)
	 */
	public boolean publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath,
			Map<String, String> metadataMap);

	/**
	 * Publish a file together with the metadata. Metadata are described in the
	 * xml-file.
	 * 
	 * @param userLogin - user login name
	 * @param collectionURL - URL of the collection where to publish 
	 * @param fileFullPath - full path to the file
	 * @param metadataFileXML - metadata as a file in XML-format
	 *  
	 * @return {@code true} if publication was successful and {@code false} otherwise (e.g. some error has occurred)
	 */
	public boolean publishFileAndMetadata(String userLogin, String collectionURL, File fileFullPath,
			File metadataFileXML);

}
