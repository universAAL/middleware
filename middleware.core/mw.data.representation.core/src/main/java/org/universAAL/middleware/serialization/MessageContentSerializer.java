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

import java.util.Dictionary;

/**
 * Classes implementing <code>StringSerializableParser</code> can serialize and
 * deserialize the content bus messages.
 *
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 *
 */
public interface MessageContentSerializer {

	/**
	 * Attribute name which should be used to register
	 * {@link MessageContentSerializer} (in the {@link Dictionary} object as last
	 * parameter of the registration parameters). The value of this attribute
	 * indicates using IETF's RFC 6838. the type of serialization used by the
	 * {@link MessageContentSerializer}. <br>
	 * The following table can be used as quick reference, however check the MIME
	 * type of the serialization syntax used.
	 * <table>
	 * <tr>
	 * <th>format name</th>
	 * <th>usage as Content-Type header</th>
	 * </tr>
	 * <tr>
	 * <td>TriG</td>
	 * <td>application/trig, application/x-trig</td>
	 * </tr>
	 * <tr>
	 * <td>TriX</td>
	 * <td>application/trix</td>
	 * </tr>
	 * <tr>
	 * <td>N3</td>
	 * <td>text/n3, text/rdf+n3</td>
	 * </tr>
	 * <tr>
	 * <td>Turtle</td>
	 * <td>text/turtle, application/x-turtle</td>
	 * </tr>
	 * <tr>
	 * <td>N-Triples</td>
	 * <td>application/n-triples, text/plain</td>
	 * </tr>
	 * <tr>
	 * <td>RDF/XML</td>
	 * <td>application/rdf+xml, application/xml</td>
	 * </tr>
	 * <tr>
	 * <td>BinaryRDF</td>
	 * <td>application/x-binary-rdf</td>
	 * </tr>
	 * <tr>
	 * <td>N-Quads</td>
	 * <td>application/n-quads, text/x-nquads, text/nquads</td>
	 * </tr>
	 * <tr>
	 * <td>JSON-LD</td>
	 * <td>application/ld+json</td>
	 * </tr>
	 * <tr>
	 * <td>RDF/JSON</td>
	 * <td>application/rdf+json</td>
	 * </tr>
	 * </table>
	 * @since 4.0.0
	 */
	public String CONTENT_TYPE = "Content-Type";

	/**
	 * State the content type the {@link MessageContentSerializer} is using.
	 * @return a string using RFC 6838.
	 */
	public String getContentType();
	
	/**
	 * Deserialize a bus message.
	 *
	 * @param serialized serialized object
	 * @return Deserialized content of the bus message.
	 */
	public Object deserialize(String serialized);

	/**
	 * Serialize a bus message.
	 *
	 * @param messageContent content to serialize
	 * @return Serialized representation of the given object.
	 */
	public String serialize(Object messageContent);
}
