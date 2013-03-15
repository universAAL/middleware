/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.serialization;

import java.util.List;

import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.rdf.Resource;

/**
 * Components implementing this interface should share the implementation with an additional property
 * that determines the format URI. In container.osgi, ... (TODO: example)
 * 
 * @author Carsten Stockloew
 */
public interface RDFSerializer {
    
//    public static final String FORMAT_N3 = "http://www.w3.org/ns/formats/N3";
//    public static final String FORMAT_N_Triples = "http://www.w3.org/ns/formats/N-Triples";
//    public static final String FORMAT_OWL_XML_Serialization = "http://www.w3.org/ns/formats/OWL_XML";
//    public static final String FORMAT_OWL_Functional_Syntax = "http://www.w3.org/ns/formats/OWL_Functional";
//    public static final String FORMAT_OWL_Manchester_Syntax = "http://www.w3.org/ns/formats/OWL_Manchester";
//    public static final String FORMAT_POWDER = "http://www.w3.org/ns/formats/POWDER";
//    public static final String FORMAT_POWDER_S = "http://www.w3.org/ns/formats/POWDER-S";
//    public static final String FORMAT_RDFa = "http://www.w3.org/ns/formats/RDFa";
//    public static final String FORMAT_RDF_XML = "http://www.w3.org/ns/formats/RDF_XML";
//    public static final String FORMAT_RIF_XML_Syntax = "http://www.w3.org/ns/formats/RIF_XML";
//    public static final String FORMAT_SPARQL_ResultsInXML = "http://www.w3.org/ns/formats/SPARQL_Results_XML";
//    public static final String FORMAT_SPARQL_ResultsInJSON = "http://www.w3.org/ns/formats/SPARQL_Results_JSON";
//    public static final String FORMAT_SPARQL_ResultsInCSV = "http://www.w3.org/ns/formats/SPARQL_Results_CSV";
//    public static final String FORMAT_SPARQL_ResultsInTSV = "http://www.w3.org/ns/formats/SPARQL_Results_TSV";
//    public static final String FORMAT_TURTLE = "http://www.w3.org/ns/formats/Turtle";

    
    /**
     * 
     * @param serialized
     *            serialized object
     * @return deserialized content of the SodaPop message
     */
    public RDFSerializationResult deserialize(String serialized);

    /**
     * 
     * @param messageContent
     *            content to serialize
     * @return serialized representation of the given object
     */
    public String serialize(Resource messageContent);
    
    /**
     * 
     * @param messageContent
     *            content to serialize
     * @return serialized representation of the given object
     */
    public String serialize(Ontology messageContent);
    
    /**
     * 
     * @param messageContent
     *            content to serialize
     * @return serialized representation of the given object
     */
    public String serialize(List messageContent);

    // see http://www.w3.org/ns/formats/
    public String getFormatURI();
    
    public String getMimeType();
}
