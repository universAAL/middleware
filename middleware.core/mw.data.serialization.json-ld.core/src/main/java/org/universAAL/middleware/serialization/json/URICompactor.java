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
				for (URIPrefix pr : defaultPrefixes) {
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
				compactedPrefix = String.format("ns\\:%02d", defaultPrefixes.size());
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

	private Collection<URIPrefix> defaultPrefixes = new TreeSet<URICompactor.URIPrefix>();
	private Map<String, URIItem> prefixedItems = new HashMap<String, URICompactor.URIItem>();
	private Collection<URIItem> nonPrefixedItems = new TreeSet<URICompactor.URIItem>();

	public URICompactor() {
		super();
		addDefaultPrefixes();
	}

	private void addDefaultPrefixes() {
		defaultPrefixes.add(new URIPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#"));
		defaultPrefixes.add(new URIPrefix("math", "http://www.w3.org/2000/10/swap/math#"));
		defaultPrefixes.add(new URIPrefix("owl", "http://www.w3.org/2002/07/owl#"));
		defaultPrefixes.add(new URIPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		defaultPrefixes.add(new URIPrefix("rdfa", "http://www.w3.org/ns/rdfa#"));
		defaultPrefixes.add(new URIPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#"));
		defaultPrefixes.add(new URIPrefix("skos", "http://www.w3.org/2004/02/skos/core#"));
		defaultPrefixes.add(new URIPrefix("vcard", "http://www.w3.org/2001/vcard-rdf/3.0#"));
		defaultPrefixes.add(new URIPrefix("xf", "http://www.w3.org/2004/07/xpath-functions"));
		defaultPrefixes.add(new URIPrefix("xml", "http://www.w3.org/XML/1998/namespace"));
		defaultPrefixes.add(new URIPrefix("xsd", "http://www.w3.org/2001/XMLSchema#"));
		defaultPrefixes.add(new URIPrefix("xsd99", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		defaultPrefixes.add(new URIPrefix("xsl10", "http://www.w3.org/XSL/Transform/1.0"));
		defaultPrefixes.add(new URIPrefix("xsl1999", "http://www.w3.org/1999/XSL/Transform"));
		defaultPrefixes.add(new URIPrefix("xslwd", "http://www.w3.org/TR/WD-xsl"));
	}

	/**
	 *
	 * @param prefix
	 * @param refs
	 */
	public void forcePrefix(String prefix, int refs) {
		Iterator<URIPrefix> pit = defaultPrefixes.iterator();
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
		defaultPrefixes.add(p);
	}

	public void addURI(String uri) {
		System.out.println("uri->"+uri);
		
		URIItem item = new URIItem();
		Iterator<URIPrefix> pit = defaultPrefixes.iterator();
		// check existing prefixes for coincidences.
		while (pit.hasNext()) {
			URIPrefix test = pit.next();
			//urn:org.universAAL.middleware.context.rdf:ContextEvent#_:7f00010134849504:385
			if(uri.startsWith(test.fullPrefix)) {
				item.prefix = test;
				item.suffix = uri.substring(test.fullPrefix.length());
				test.references++;
				prefixedItems.put(uri, item);
				return;
			}
		}
		
		if(!prefixedItems.containsKey(uri)) {
			//
			System.out.println("uri not prefixed yet=>"+uri);
			URIItem candidate = this.compactURI(uri);
			if(candidate !=null) {
				prefixedItems.put(uri, candidate);
				return;
			}
		}	
	}

	public List<URIPrefix> getPrefixes() {
		List<URIPrefix> l = new ArrayList<URICompactor.URIPrefix>();
		for (URIPrefix uriPrefix : defaultPrefixes) {
			if (uriPrefix.references > 0) {
				l.add(uriPrefix);
			}
		}
		
		for ( String uriPrefix : prefixedItems.keySet()) {
			l.add(prefixedItems.get(uriPrefix).prefix);
			
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
		Iterator<URIPrefix> pit = defaultPrefixes.iterator();
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
	
	private URIItem compactURI(String uri) {
		URIItem item = new URIItem();
		try {
			URIPrefix prefix = new URIPrefix();
			final char delimiter ='#';
			//FIXME what if the delimiter # not exist and is a / or another character?
			String pref,fullPrefix,sufix;
			int delim_index = uri.indexOf(delimiter);
			fullPrefix = uri.substring(0,delim_index+1);
			sufix = uri.substring(delim_index+1);
			String to_cut = fullPrefix.replaceAll("[^A-Za-z]", "");
			pref= this.generatePrefix(to_cut,2);
			prefix.compactedPrefix = pref;
			prefix.fullPrefix = fullPrefix;
			prefix.references++;
			item.prefix = prefix;
			item.suffix = sufix;	
		} catch (Exception e) {
			e.printStackTrace();
			item=null;
		}
		
		return item;
	}
	
	private String generatePrefix(String candidate, int lenght) {
		
		//remove all non-alphanumeric characters
		//walk the resultant string from end, and get the N first characters (given in lenght variable)
		//if the resultant prefix is aleady taken, then increment length variable and call recursively this function
		//TODO what if the resultant prefix has a lot of characters
		//TODO build a meaningful prefix using the start N character of last string before the last character
		//TODO define criteria to generate prefixes
		//FIXME when candidate is empty..throw IndexOutofBoundException
		//FIXME also this exists to test cases
		
		if(candidate.isEmpty()) {
			//TODO throw exception..however never is null
		}
		
		String pref="";
		int size = lenght;
		candidate=candidate.substring(candidate.length()-size).toLowerCase();
		Iterator<URIPrefix> pit = defaultPrefixes.iterator();
		while(pit.hasNext()) {
			URIPrefix test = pit.next();
			if(test.compactedPrefix.equals(candidate)) {
				this.generatePrefix(candidate, size+1);
			}else {
				return candidate;
			}
		}
		return pref;
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
		//System.out.println("full pref "+a);
		//System.out.println("uri "+b);
		int i = 0;
		int last = 0;
		while (i < (Math.min(a.length(), b.length() - 1)) && a.charAt(i) == b.charAt(i)) {
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
