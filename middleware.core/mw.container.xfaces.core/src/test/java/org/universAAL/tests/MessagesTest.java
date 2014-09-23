/*******************************************************************************
 * Copyright 2013 2011 Universidad Politécnica de Madrid
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
package org.universAAL.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import junit.framework.TestCase;

import org.universAAL.middleware.container.utils.Messages;

/**
 * @author amedrano
 * 
 */
public class MessagesTest extends TestCase {

    public void testUsual() {
	URL msg = this.getClass().getClassLoader()
		.getResource("messages.properties");
	assertNotNull(msg);
	Messages m = null;
	try {
	    m = new Messages(msg, Locale.ENGLISH);
	} catch (IllegalArgumentException e) {
	    fail();
	} catch (IOException e) {
	    fail();
	}
	assertNotNull(m);
	assertEquals("foo", m.getString("message1"));
    }

    public void testUsualWithLocaleChange() {
	URL msg = this.getClass().getClassLoader()
		.getResource("messages.properties");
	assertNotNull(msg);
	Messages m = null;
	try {
	    m = new Messages(msg, Locale.ENGLISH);
	} catch (IllegalArgumentException e) {
	    fail();
	} catch (IOException e) {
	    fail();
	}
	assertNotNull(m);
	m.setLocale(Locale.GERMAN);
	assertEquals("fuu", m.getString("message1"));
    }

    public void testUsualWithNoExsistentLocaleChange() {
	URL msg = this.getClass().getClassLoader()
		.getResource("messages.properties");
	assertNotNull(msg);
	Messages m = null;
	try {
	    m = new Messages(msg, Locale.ENGLISH);
	} catch (IllegalArgumentException e) {
	    fail();
	} catch (IOException e) {
	    fail();
	}
	assertNotNull(m);
	m.setLocale(Locale.CHINESE);
	assertEquals("foo", m.getString("message1"));
    }

    public void testNonExistentLocaleConstructor() {
	URL msg = this.getClass().getClassLoader()
		.getResource("messages.properties");
	assertNotNull(msg);
	Messages m = null;
	try {
	    m = new Messages(msg, Locale.CHINESE);
	} catch (IllegalArgumentException e) {
	    fail();
	} catch (IOException e) {
	    fail();
	}
	assertNotNull(m);
	m.setLocale(Locale.GERMAN);
	assertEquals("fuu", m.getString("message1"));
    }

    public void testNonExistentDefaultPropertiesFile() {
	URL msg = this.getClass().getClassLoader()
		.getResource("NOmessages.properties");
	try {
	    new Messages(msg, Locale.ENGLISH);
	    fail();
	} catch (IllegalArgumentException e) {
	    assertEquals("URL should not be null.", e.getMessage());
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void testNonExistentDefaultPropertiesFile2() {
	URL msg = null;
	try {
	    msg = new URL("http://localhost/someMessages.properties");
	} catch (MalformedURLException e1) {
	    e1.printStackTrace();
	    fail();
	}
	assertNotNull(msg);
	try {
	    new Messages(msg, Locale.ENGLISH);
	    fail();
	} catch (IllegalArgumentException e) {
	    fail();
	} catch (FileNotFoundException e) {
	    // Success!
	} catch (IOException e) {
	    // Success?
	}
    }
}
