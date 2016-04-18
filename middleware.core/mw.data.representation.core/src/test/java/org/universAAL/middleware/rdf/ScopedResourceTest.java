/*******************************************************************************
 * Copyright 2016 2011 Universidad Politécnica de Madrid
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
package org.universAAL.middleware.rdf;

import junit.framework.TestCase;

/**
 * @author amedrano
 *
 */
public class ScopedResourceTest extends TestCase {

	public void test_isSerializableTo() {
		ScopedResource sr = new ScopedResource();
		assertFalse(sr.isSerializableTo(null));
		assertTrue(sr.isSerializableTo("1"));
		
		sr.addScope(ScopedResource.ONLY_LOCAL_SCOPE);
		assertFalse(sr.isSerializableTo("1"));
		
		sr.clearScopes();
		assertFalse(sr.isSerializableTo(null));
		assertTrue(sr.isSerializableTo("1"));
		
		sr.addScope("1");
		assertTrue(sr.isSerializableTo("1"));
		assertFalse(sr.isSerializableTo("2"));
		
		sr.clearScopes();
		sr.setOriginScope("1");
		assertFalse(sr.isSerializableTo("1"));
		assertTrue(sr.isSerializableTo("2"));
		
		sr.addScope("1");
		assertFalse(sr.isSerializableTo("1"));
		assertFalse(sr.isSerializableTo("2"));
		
		
	}
	
	public void test_isScoped() {
		ScopedResource sr = new ScopedResource();
		assertFalse(sr.isScoped());
		sr.addScope("1");
		assertTrue(sr.isScoped());
		sr.addScope("2");
		assertTrue(sr.isScoped());
		sr.clearScopes();
		assertFalse(sr.isScoped());
		sr.addScope(ScopedResource.ONLY_LOCAL_SCOPE);
		assertTrue(sr.isScoped());
		
	}
}
