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
package bwfdm.connector.dspace.dto.v6;

import java.util.List;

/**
 * 
 * @author Volodymyr Kushnarenko
 */
public class HierarchyObjectRestV6 {

	public String id;
	public String name;
	public String handle;
	public List<HierarchyObjectRestV6> community;
	public List<HierarchyCollectionObjectRestV6> collection;
	
	
	public List<String> getCommunityListForCollection(HierarchyObjectRestV6 obj, String collectionHandle, List<String> communityList) {
				
		for(HierarchyCollectionObjectRestV6 coll: obj.collection) {
			if(coll.handle.equals(collectionHandle)) {
				communityList.add(0, obj.name);
				return communityList;
			}
		}
		
		for(HierarchyObjectRestV6 comm: obj.community) {
			List<String> list = getCommunityListForCollection(comm, collectionHandle, communityList);
			if(list != null) {
				list.add(0, obj.name);
				return list;
			}				
		}
		
		return null;
	}
	
// JSON Example
//Hierarchy: {
//		  "id" : "dfc7e72e-f947-4246-aae4-b375b93474af",
//		  "name" : "bwFDM Test DSpace",
//		  "handle" : "123456789/0",
//		  "community" : [ {
//		    "id" : "d5b29fe7-be1b-48a0-8d16-71e8d4922357",
//		    "name" : "Fakultät für Ingenieurwissenschaften, Informatik und Psychologie",
//		    "handle" : "123456789/31",
//		    "community" : [ ],
//		    "collection" : [ {
//		      "id" : "ecc6daff-8201-4aa6-9bc0-6c02dc6400c9",
//		      "name" : "Forschungsdaten",
//		      "handle" : "123456789/33"
//		    }, {
//		      "id" : "e3858c43-8950-473d-9631-d39492d6e1ea",
//		      "name" : "Publikationen",
//		      "handle" : "123456789/32"
//		    } ]
//		  }, {
//		    "id" : "25013241-e584-4b16-91ee-4332c4447a7c",
//		    "name" : "Fakultät für Mathematik und Wirtschaftswissenschaften",
//		    "handle" : "123456789/34",
//		    "community" : [ ],
//		    "collection" : [ {
//		      "id" : "73efe7b0-392f-42b6-a53d-99d5c56d2ead",
//		      "name" : "Forschungsdaten",
//		      "handle" : "123456789/36"
//		    }, {
//		      "id" : "64d529ca-3499-4128-88a2-599eab5a7bcc",
//		      "name" : "Publikationen",
//		      "handle" : "123456789/35"
//		    } ]
//		  }, {
//		    "id" : "c60119ae-aed7-4437-9022-c15668e8f64b",
//		    "name" : "Fakultät für Medizin",
//		    "handle" : "123456789/41",
//		    "community" : [ {
//		      "id" : "b73ec7fa-efbf-422d-98ec-04bd20b40dec",
//		      "name" : "Chirurgie",
//		      "handle" : "123456789/42",
//		      "community" : [ ],
//		      "collection" : [ {
//		        "id" : "c1ec1265-a337-4cd7-af86-9a98f0a1c85f",
//		        "name" : "Publikationen",
//		        "handle" : "123456789/45"
//		      } ]
//		    }, {
//		      "id" : "5395097e-962b-4810-b289-cddd66302481",
//		      "name" : "Innere",
//		      "handle" : "123456789/46",
//		      "community" : [ {
//		        "id" : "61b063c3-73ec-4b14-adf3-bd33cac9b945",
//		        "name" : "Mikro",
//		        "handle" : "123456789/47",
//		        "community" : [ {
//		          "id" : "16f5e6c2-6624-4d85-900d-3f4a5c05cb36",
//		          "name" : "Plasma",
//		          "handle" : "123456789/48",
//		          "community" : [ ],
//		          "collection" : [ {
//		            "id" : "a619709b-7807-4bd6-8ada-5b8945b99674",
//		            "name" : "Messdaten",
//		            "handle" : "123456789/49"
//		          } ]
//		        } ],
//		        "collection" : [ ]
//		      } ],
//		      "collection" : [ ]
//		    }, {
//		      "id" : "0f72b1b5-480c-4d32-ba61-471f154f9ff3",
//		      "name" : "Radiologie",
//		      "handle" : "123456789/43",
//		      "community" : [ ],
//		      "collection" : [ {
//		        "id" : "90684915-c1c9-40f7-82ae-6243a137a8df",
//		        "name" : "MRI Daten",
//		        "handle" : "123456789/44"
//		      } ]
//		    } ],
//		    "collection" : [ ]
//		  } ],
//		  "collection" : [ ]
//		}

}
