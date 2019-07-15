/*******************************************************************************
 * Copyright 2018 2011 Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.middleware.serialization.json;

import java.util.List;

import junit.framework.TestCase;

import org.universAAL.middleware.serialization.json.URICompactor.URIPrefix;

/**
 * @author amedrano
 *
 */
public class URICompactorTest extends TestCase {

	public void test0() {
		assertTrue(URICompactor.isPrefix(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#", ""));
		assertTrue(URICompactor.isPrefix(
				"http://www.w3.org/2000/01/rdf-schema#", ""));
		assertTrue(URICompactor.isPrefix("http://xmlns.com/foaf/0.1/", ""));

		assertEquals(17, URICompactor.coincidenceIndex(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"http://www.w3.org/2000/01/rdf-schema#"));
		assertEquals(17, URICompactor.coincidenceIndex(
				"http://www.w3.org/2000/02/22-rdf-syntax-ns#",
				"http://www.w3.org/200/01/rdf-schema#"));
	}

	public void test1() {

		assertTrue(URICompactor.isPrefix("http://a.b.c/d#", "e"));

		URICompactor c = new URICompactor();
		c.addURI("http://a.b.c/d#e");
		c.addURI("http://a.b.c/d#f");
		c.addURI("http://a.b.c/d#g");
		c.addURI("http://a.b.c/d#f");
		c.addURI("http://a.b.c/h#f");
		c.addURI("http://a.b.c/h#f");
		c.addURI("http://a.b.c/h#f/u/a");
		c.addURI("http://a.b.c/h#f/u/b");
		c.addURI("http://a.b.c/h#i");
		c.addURI("http://j.k.l/d#f");
		List<URIPrefix> prx = c.getPrefixes();
		assertEquals(8, prx.size());
		for (URIPrefix p : prx) {
			p.getCompactedPrefix();
		}

	}

}
