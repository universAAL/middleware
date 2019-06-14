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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author amedrano
 *
 */
public class URICompactor {

	private static final ArrayList<String> FORBIDEN_PREFIXES = new ArrayList<String>() {
		{
			add("org");
			add("com");
			add("ont");
			add("http");
			add("ftp");
			add("owl");
		}
	};

	public class URIPrefix implements Comparable<URIPrefix> {
		String compactedPrefix=null;
		String fullPrefix=null;
		int references;

		public URIPrefix() {
			super();
		}

		public URIPrefix(String compactedPrefix, String fullPrefix) {
			this.compactedPrefix = compactedPrefix;
			this.fullPrefix = fullPrefix;
			references = 0;
		}

		/**
		 * @return
		 */
		public String getCompactedPrefix() {
			if (compactedPrefix != null) {
				return compactedPrefix;// returns null
			} else {
				// find a suitable compacted prefix
				Set<String> existingPrefixes = new HashSet<String>();
				for (URIPrefix pr : prefixes) {
					existingPrefixes.add(pr.compactedPrefix); // compacted prefix is not null
				}
				String[] lexem = this.fullPrefix.split("[^\\pL\\p{Pc}]+");
				for (int i = lexem.length - 1; i >= 0; i--) {
					for (int j = Math.min(3, lexem[i].length()); j < lexem[i].length(); j++) {
						String candidate = lexem[i].substring(0, j)
								.toLowerCase();
						if (!existingPrefixes.contains(candidate)
								&& !FORBIDEN_PREFIXES.contains(candidate)) {
							compactedPrefix = candidate;
							return compactedPrefix;
						}
					}
				}
				compactedPrefix = String.format("ns\\:%02d", prefixes.size());
				return compactedPrefix;
			}
		}

		/** {@inheritDoc} */
		public int compareTo(URIPrefix o) {
			return this.fullPrefix.compareTo(o.fullPrefix);
		}
	}

	public class URIItem implements Comparable<URIItem> {
		URIPrefix prefix;
		String suffix;

		public String getCompacted() {
			if (prefix != null) {
				return prefix.getCompactedPrefix() + ":" + suffix;
			} else if (prefix != null) {
				return prefix.fullPrefix + suffix;
			} else {
				return suffix;
			}
		}

		/** {@inheritDoc} */
		public int compareTo(URIItem o) {
			if (prefix == null) {
				if (o.prefix == null) {
					return suffix.compareTo(o.suffix);
				}
				return suffix.compareTo(o.prefix.fullPrefix + suffix);
			}

			if (o.prefix == null) {
				return (prefix.fullPrefix + suffix).compareTo(o.suffix);
			}
			return (prefix.fullPrefix + suffix).compareTo(o.prefix.fullPrefix
					+ suffix);
		}
	}

	private Collection<URIPrefix> prefixes = new TreeSet<URICompactor.URIPrefix>();
	private Map<String, URIItem> prefixedItems = new HashMap<String, URICompactor.URIItem>();
	private Collection<URIItem> nonPrefixedItems = new TreeSet<URICompactor.URIItem>();

	public URICompactor() {
		super();
		addDefaultPrefixes();
	}

	private void addDefaultPrefixes() {
		prefixes.add(new URIPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#"));
		prefixes.add(new URIPrefix("math", "http://www.w3.org/2000/10/swap/math#"));
		prefixes.add(new URIPrefix("owl", "http://www.w3.org/2002/07/owl#"));
		prefixes.add(new URIPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		prefixes.add(new URIPrefix("rdfa", "http://www.w3.org/ns/rdfa#"));
		prefixes.add(new URIPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#"));
		prefixes.add(new URIPrefix("skos", "http://www.w3.org/2004/02/skos/core#"));
		prefixes.add(new URIPrefix("vcard", "http://www.w3.org/2001/vcard-rdf/3.0#"));
		prefixes.add(new URIPrefix("xf", "http://www.w3.org/2004/07/xpath-functions"));
		prefixes.add(new URIPrefix("xml", "http://www.w3.org/XML/1998/namespace"));
		prefixes.add(new URIPrefix("xsd", "http://www.w3.org/2001/XMLSchema#"));
		prefixes.add(new URIPrefix("xsd99", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		prefixes.add(new URIPrefix("xsl10", "http://www.w3.org/XSL/Transform/1.0"));
		prefixes.add(new URIPrefix("xsl1999", "http://www.w3.org/1999/XSL/Transform"));
		prefixes.add(new URIPrefix("xslwd", "http://www.w3.org/TR/WD-xsl"));
	}

	/**
	 *
	 * @param prefix
	 * @param refs
	 */
	public void forcePrefix(String prefix, int refs) {
		Iterator<URIPrefix> pit = prefixes.iterator();
		// check existing prefixes for coincidences.
		while (pit.hasNext()) {
			URIPrefix test = pit.next();
			if (test.fullPrefix.equals(prefix)) {
				test.references = refs;
				return;
			}
			if (test.fullPrefix.compareTo(prefix) > 0) {
				// don't bother continuing, prefixes are sorted
				break;
			}
		}

		URIPrefix p = new URIPrefix();
		p.fullPrefix = prefix;
		p.references = refs;
		prefixes.add(p);
	}

	public void addURI(String uri) {
		URIItem item = new URIItem();
		Iterator<URIPrefix> pit = prefixes.iterator();
		// check existing prefixes for coincidences.
		while (pit.hasNext()) {
			URIPrefix test = pit.next();
			int comp = coincidenceIndex(test.fullPrefix, uri);
			if (comp == test.fullPrefix.length()) {
				// prefix fully coincides.
				item.prefix = test;
				item.suffix = uri.substring(comp + 1);
				test.references++;
				prefixedItems.put(uri, item);
				return;
			}
			if (test.fullPrefix.compareTo(uri) > 0) {
				// don't bother continuing, prefixes are sorted
				break;
			}
		}

		Iterator<URIItem> npit = nonPrefixedItems.iterator();
		URICompactor.URIItem test = null;
		// Itereate over non-prefixed Items
		while (npit.hasNext()) {
			test = npit.next();
			int comp = coincidenceIndex(test.suffix, uri);
			String prefix = uri.substring(0, comp + 1);
			String testSuffix = test.suffix.substring(comp + 1);
			String uriSuffix = uri.substring(comp + 1);
			if (isPrefix(prefix, testSuffix) && isPrefix(prefix, uriSuffix)) {
				// create prefix
				URIPrefix p = new URIPrefix();
				p.fullPrefix = prefix;
				p.references = 2;
				prefixes.add(p);
				// prefix test
				test.prefix = p;
				test.suffix = testSuffix;
				// prefix item
				item.prefix = p;
				item.suffix = uriSuffix;
				prefixedItems.put(uri, item);
				break;
			}
			if (test.suffix.compareTo(uri) > 0) {
				// Stop, you will not find it any further.
				break;
			}
		}
		if (test != null && test.prefix != null) {
			// remove test from non-prefixed, this could not be done within
			// while loop.
			nonPrefixedItems.remove(test);
			return;
		}

		// URI without existing prefix and without a companion
		item.suffix = uri;
		nonPrefixedItems.add(item);
	}

	public List<URIPrefix> getPrefixes() {
		List<URIPrefix> l = new ArrayList<URICompactor.URIPrefix>();
		for (URIPrefix uriPrefix : prefixes) {
			if (uriPrefix.references > 0) {
				l.add(uriPrefix);
			}
		}
		return l;
	}

	public List<URIItem> getURIs() {
		List<URIItem> l = new ArrayList<URIItem>();
		l.addAll(prefixedItems.values());
		l.addAll(nonPrefixedItems);
		return l;
	}

	/**
	 * Compact a given URI with the stored state.
	 *
	 * @param uri
	 * @return the attempt of compacting, prefix null if not compacted.
	 */
	public URIItem compact(String uri) {
		if (prefixedItems.containsKey(uri)) {
			return prefixedItems.get(uri);
		}

		URIItem item = new URIItem();
		Iterator<URIPrefix> pit = prefixes.iterator();
		// check existing prefixes for coincidences.
		while (pit.hasNext()) {
			URIPrefix test = pit.next();
			int comp = coincidenceIndex(test.fullPrefix, uri);
			if (comp == test.fullPrefix.length()) {
				// prefix fully coincides.
				item.prefix = test;
				item.suffix = uri.substring(comp + 1);
				return item;
			}
			if (test.fullPrefix.compareTo(uri) > 0) {
				// don't bother continuing, prefixes are sorted
				break;
			}
		}
		// URI without existing prefix
		item.suffix = uri;
		return item;
	}

	/**
	 * Finds the last index where it, and all previous, chars are equal; and it
	 * is not an alphanumeric value.
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static int coincidenceIndex(String a, String b) {
		int i = 0;
		int last = 0;
		while (i < (Math.min(a.length(), b.length() - 1))
				&& a.charAt(i) == b.charAt(i)) {
			if (!Character.isAlphabetic(a.charAt(i))
					&& !Character.isDigit(a.charAt(i))) {
				last = i;
			}
			i++;
		}
		return last;
	}

	/**
	 * @param prefix
	 * @return
	 */
	static public boolean isPrefix(String prefix, String suffix) {
		// suffix must comply with
		// https://www.w3.org/TR/turtle/#grammar-production-PN_LOCAL
		// clean hex
		String cleanSuffix = suffix.replaceAll(
				"\\%([0-9]|[A-F]|[a-f])([0-9]|[A-F]|[a-f])", "");
		// clean scapes
		cleanSuffix = cleanSuffix.replaceAll(
				"\\\\[_~\\.\\-\\!\\$&\'\\(\\)\\*\\+\\,;=/\\?#@%]", "");

		char last = prefix.charAt(prefix.length() - 1);
		return prefix.length() > 8
				&& !Character.isAlphabetic(last)
				&& !Character.isDigit(last)
				&& !cleanSuffix
						.matches("[_~\\.\\-\\!\\$&\'\\(\\)\\*\\+\\,;=/\\?#@%]+");
	}
}
