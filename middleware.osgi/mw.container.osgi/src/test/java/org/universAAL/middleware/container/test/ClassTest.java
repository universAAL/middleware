/*******************************************************************************
 * Copyright 2016 Universidad Politécnica de Madrid UPM
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
package org.universAAL.middleware.container.test;

import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;

import junit.framework.TestCase;

/**
 * @author amedrano
 *
 */
public class ClassTest extends TestCase {

	public void test_isAssignableFrom() {
		ModuleActivator activatorClass = new ModuleActivator() {

			public void stop(ModuleContext arg0) throws Exception {
				// TODO Auto-generated method stub

			}

			public void start(ModuleContext arg0) throws Exception {
				// TODO Auto-generated method stub

			}
		};

		assertFalse(activatorClass.getClass().isAssignableFrom(ModuleActivator.class));
		assertTrue(ModuleActivator.class.isAssignableFrom(activatorClass.getClass()));
	}

}
