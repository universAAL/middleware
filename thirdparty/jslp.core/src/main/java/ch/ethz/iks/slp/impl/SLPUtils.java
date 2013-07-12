/* Copyright (c) 2005-2008 Jan S. Rellermeyer
 * Systems Group,
 * Department of Computer Science, ETH Zurich.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of ETH Zurich nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ch.ethz.iks.slp.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class.
 * 
 * @author Jan S. Rellermeyer, ETH Z�rich
 * @since 0.1
 */
final class SLPUtils {

	/**
	 * hidden constructor.
	 */
	private SLPUtils() {

	}

	/**
	 * get a <code>List</code> of attribute/value pairs in String
	 * representation from a <code>Dictionary</code>.
	 * 
	 * @param attributes
	 *            the <code>Dictionary</code>
	 * @return the <code>List</code>.
	 */
	static List dictToAttrList(final Dictionary attributes) {
		List attList = new ArrayList();
		if (attributes != null) {
			for (Enumeration keys = attributes.keys(); keys.hasMoreElements();) {
				Object key = keys.nextElement();
				StringBuffer buffer = new StringBuffer();
				buffer.append("(");
				buffer.append(key);
				buffer.append("=");
				buffer.append(attributes.get(key));
				buffer.append(")");
				attList.add(buffer.toString());
			}
		}
		return attList;
	}

	/**
	 * get a <code>Dictionary</code> of attributes and their values from an
	 * attribute <code>List</code>.
	 * 
	 * @since 0.6
	 * @param attrList
	 *            the attribute list.
	 * @return the <code>Dictionary</code>.
	 */
	static Dictionary attrListToDict(final List attrList) {
		Dictionary dict = new Hashtable();

		for (Iterator iter = attrList.iterator(); iter.hasNext();) {
			String attrStr = (String) iter.next();
			attrStr = attrStr.substring(1, attrStr.length() - 1);
			int pos = attrStr.indexOf("=");
			if (pos > -1) {
				String key = attrStr.substring(0, pos).trim();
				String value = attrStr.substring(pos + 1).trim();
				dict.put(key, value);
			}
		}

		return dict;
	}

	/**
	 * add a value to a value list in a Map.
	 * 
	 * @param map
	 *            the map.
	 * @param key
	 *            the key.
	 * @param value
	 *            the value to be added to the list.
	 */
	static void addValue(final Map map, final Object key, final Object value) {

		List values;
		if ((values = (List) map.get(key)) == null) {
			values = new ArrayList();
		}
		if (values.contains(value)) {
			return;
		}
		values.add(value);
		map.put(key, values);
	}

	/**
	 * remove a value from a value list in a Map.
	 * 
	 * @param map
	 *            the map.
	 * @param key
	 *            the key.
	 * @param value
	 *            the value to be removed from the list.
	 */
	static void removeValue(final Map map, final Object key, final Object value) {
		List values;
		if ((values = (List) map.get(key)) == null) {
			return;
		}
		values.remove(value);
		if (!values.isEmpty()) {
			map.put(key, values);
		} else {
			map.remove(key);
		}
	}

	/**
	 * remove a value from all keys where it occurs.
	 * 
	 * @param map
	 *            the map.
	 * @param value
	 *            the value.
	 */
	static void removeValueFromAll(final Map map, final Object value) {
		final Object[] keys = map.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			List list = (List) map.get(keys[i]);
			list.remove(value);
			if (list.isEmpty()) {
				map.remove(keys[i]);
			}
		}
	}

	/**
	 * get the current timestamp as defined in RFC 2608.
	 * 
	 * @return the current timestamp.
	 */
	static int getTimestamp() {
		long systemTime = System.currentTimeMillis();
		systemTime /= 1000;
		return (int) systemTime;
	}

	/**
	 * find case insensitive matching between a key List and a Dictionary of
	 * attributes.
	 * 
	 * @param keyList
	 *            the key List.
	 * @param attributes
	 *            the attribute Dictionary.
	 * @return a List of matches.
	 */
	static List findMatches(final List keyList, final Dictionary attributes) {
		List results = new ArrayList();
		Set caseInsensitiveKeyList = new HashSet();
		List wildcards = new ArrayList();
		if (!keyList.isEmpty()) {
			for (Iterator keys = keyList.iterator(); keys.hasNext();) {
				String key = (String) keys.next();
				if (key.indexOf("*") == -1) {
					caseInsensitiveKeyList.add(key.toLowerCase());
				} else {
					wildcards.add(key);
				}
			}
		}

		for (Enumeration keys = attributes.keys(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			if (keyList.isEmpty()
					|| caseInsensitiveKeyList.contains(key.toLowerCase())) {
				results.add("(" + key + "=" + attributes.get(key).toString()
						+ ")");
				continue;
			}
			for (Iterator iter = wildcards.iterator(); iter.hasNext();) {
				String wildcard = (String) iter.next();
				if (equalsWithWildcard(wildcard.toCharArray(), 0, key
						.toCharArray(), 0)) {
					results.add("(" + key + "="
							+ attributes.get(key).toString() + ")");
					continue;
				}
			}

		}
		return results;
	}

	/**
	 * equality check with wildcards
	 * 
	 * @param val
	 *            the value
	 * @param valIndex
	 *            the current position within the value
	 * @param attr
	 *            the attribute
	 * @param attrIndex
	 *            the current position within the attribute.
	 * @return true if equals.
	 */
	private static boolean equalsWithWildcard(char[] val, int valIndex,
			char[] attr, int attrIndex) {
		if (val.length == valIndex) {
			return attr.length == attrIndex;
		}
		if (val[valIndex] == '*') {
			valIndex++;
			do {
				if (equalsWithWildcard(val, valIndex, attr, attrIndex)) {
					return true;
				}
				attrIndex++;
			} while (attr.length - attrIndex > -1);
			return false;
		} else {
			return (attr.length == attrIndex || attr[attrIndex] != val[valIndex]) ? false
					: equalsWithWildcard(val, ++valIndex, attr, ++attrIndex);
		}
	}
}
