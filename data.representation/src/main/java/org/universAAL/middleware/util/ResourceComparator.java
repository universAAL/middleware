/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institut für Graphische Datenverarbeitung
	
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
package org.universAAL.middleware.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import org.universAAL.middleware.rdf.Resource;

/**
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class ResourceComparator {
	
	private ArrayList done1 = new ArrayList(), done2 = new ArrayList();
	private Stack s = new Stack();
	private boolean isPrinting = false;
	
	private boolean differ(int indent, List l1, List l2, boolean closedList) {
		int i = l1.size(), j = l2.size();
		if (i != j) {
			writeLine(indent, new Object[] {
					"different number of elements: ",
					Integer.toString(i),
					" <-> ",
					Integer.toString(j)
			});
			return true;
		}
		
		boolean result = false;
		if (closedList) {
			while(--i > -1)
				if (differ(indent, "Element"+i, l1.get(i), l2.get(i), true))
					result = true;
		} else {
			boolean wasPrinting = isPrinting;
			isPrinting = false;
			while(--i > -1) {
				for (j=i; j > -1; j--) {
					Object o = l2.get(j);
					if (!differ(indent, "Element"+i, l1.get(i), o, true)) {
						if (i != j) {
							o = l2.set(i, o);
							l2.set(j, o);
						}
						break;
					}
				}
				if (j == -1) {
					isPrinting = wasPrinting;
					writeLine(indent, new Object[] {
							"Element",
							Integer.toBinaryString(i),
							" not found!"
					});
					isPrinting = false;
					result = true;
				}
			}
			isPrinting = wasPrinting;
		}
		return result;
	}
	
	private boolean differ(int indent, Resource r1, Resource r2) {
		int i = done1.indexOf(r1.getURI()), j = done2.indexOf(r2.getURI());
		if (i != j) {
			writeLine(indent, new Object[] {
					"different log indexes: ",
					Integer.toString(i),
					" <-> ",
					Integer.toString(j)
			});
			return true;
		}
		
		if (i > -1)
			return false;
		
		done1.add(r1.getURI());
		done2.add(r2.getURI());
		
		if (r1.isAnon() != r2.isAnon()) {
			writeLine(indent, new Object[] {
					r1.isAnon()? "anon <-> " : "not-anon <-> ",
					r2.isAnon()? "anon" : "not-anon"
			});
			return true;
		} else if (!r1.isAnon()  &&  !r1.getURI().equals(r2.getURI())) {
			writeLine(indent, new Object[] {"different URIs"});
			return true;
		}
		
		i = r1.numberOfProperties();
		j = r2.numberOfProperties();
		if (i != j) {
			writeLine(indent, new Object[] {
					"different number of props: ",
					Integer.toString(i),
					" <-> ",
					Integer.toString(j)
			});
			return true;
		}
		
		if (i == 0)
			if (r1.getURI().equals(r2.getURI()))
				return false;
			else {
				writeLine(indent, new Object[] { "different empty resources" });
				return true;
			}
		
		boolean result = false;
		for (Enumeration e=r1.getPropertyURIs(); e.hasMoreElements();) {
			String prop = (String) e.nextElement();
			if (differ(indent, prop, r1.getProperty(prop), r2.getProperty(prop), r1.isClosedCollection(prop)))
				result = true;
		}
		return result;
	}
	
	private boolean differ(int indent, String prop, Object v1, Object v2, boolean closedList) {
		if (v1 == null  ||  v2 == null) {
			writeLine(indent, new Object[] {
					prop,
					": ",
					v1==null? "null" : "not-null",
					" <-> ",
					v2==null? "null" : "not-null"
			});
			return true;
		} else if (v1.getClass() != v2.getClass()
				&& (!(v1 instanceof List)  ||  !(v2 instanceof List))) {
			writeLine(indent, new Object[] {
					prop,
					": ",
					v1.getClass().getName(),
					" <-> ",
					v2.getClass().getName()
			});
			return true;
		} else if (v1 instanceof List)
			if (differ(indent+1, (List) v1, (List) v2, closedList)) {
				writeLine(indent, new Object[] {
						prop,
						": different lists"
				});
				return true;
			} else
				return false;
		else if (v1 instanceof Resource)
			if (differ(indent+1, (Resource) v1, (Resource) v2)) {
				writeLine(indent, new Object[] {
						prop,
						": ",
						toString((Resource) v1),
						" <-> ",
						toString((Resource) v2)
				});
				return true;
			} else
				return false;
		else if (!v1.equals(v2)) {
			writeLine(indent, new Object[] {
					prop,
					": ",
					v1,
					" <-> ",
					v2
			});
			return true;
		} else
			return false;
	}
	
	public boolean areEqual(Resource r1, Resource r2) {
		isPrinting = false;
		return r1 != null  &&  r2 != null 
			&& r1.getClass() == r2.getClass()
			&& !differ(0, r1, r2);
	}
	
	public void printDiffs(Resource r1, Resource r2) {
		isPrinting = true;
		System.out.println("Comparing " + toString(r1) + " with " + toString(r2) + ":");
		if (r1 == null  ||  r2 == null)
			System.out.println("NULL values cannot be compared!");
		else if (r1.getClass() != r2.getClass()) {
			writeLine(0, new Object[] {
					"  different types: ",
					r1.getClass().getName(),
					" <-> ",
					r2.getClass().getName()
			});
			System.out.println(s.pop());
		} else if (differ(1, r1, r2))
			while(!s.empty())
				System.out.println(s.pop());
		else
			System.out.println("  No diffs found!");
	}

	private String toString(Resource r) {
		return (r.isAnon()? "" : r.hasQualifiedName()? r.getLocalName() : r.getURI())
				+ "@" + StringUtils.deriveLabel(r.getType());
	}
	
	private void writeLine(int indent, Object[] lineContent) {
		if (isPrinting) {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<indent; i++)
				sb.append("  ");
			for (int i=0; i<lineContent.length; i++)
				sb.append(lineContent[i]);
			s.push(sb.toString());
		}
	}
}
