package bwfdm.connector.dspace;

import java.util.Map;

/**
 * DSpace-specific common methods, which are using only SWORD protocol, without any REST-request.
 * Implementation of the interface should be done in further classes such as e.g. DSpace_v6, DSpace_v5.
 * 
 * @author Volodymyr Kushnarenko
 */
public interface DSpaceRepositorySwordOnly {

	/**
	 * Get collections, which are available for the current authentication credentials, and show their full name
	 * (e.g. for DSpace-repository it means "community/subcommunity/collection", where "/" is the fullNameSeparator)
	 * <p>
	 * Could be, that the current credentials have an access only for some specific collections.
	 * <p>
	 * <b>IMPORTANT:</b> credentials are used implicitly. Definition of the credentials must be done in other place, e.g. via class constructor.
	 * <p>
	 * <b>IMPORTANT:</b> only SWORD protocol will be used, without any REST-request.
	 *  
	 * @param fullNameSeparator a {@link String} separator between collections and communities (e.g. "/"). 
	 * 			It could be also used as a separator for further parsing of the the collection's full name. 
	 * @return Map of Strings, where key = "collection full URL", value = "collection full name" 
	 * 			(it could be also empty if there are not available collections) or {@code null} in case of error.
	 */
	public Map<String, String> getAvailableCollectionsWithFullNameSwordOnly(String fullNameSeparator);
}
