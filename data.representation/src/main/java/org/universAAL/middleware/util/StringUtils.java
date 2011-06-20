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

import java.net.InetAddress;
import java.util.Random;

/**
 * A set of utility methods for Strings.
 * 
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied
 *         Tazari</a>
 * @author Carsten Stockloew
 */
public class StringUtils {

    /** Internal counter for creating unique IDs. */
    private static int counter = 0;

    /** The prefix for creating unique IDs. */
    private static final String UUID_prefix;

    static {
	String aux = "_:";
	String peerID = System.getProperty("sodapop.peerID");
	if (peerID != null) {
	    aux = peerID
		    + '+'
		    + Integer
			    .toHexString(new Random(System.currentTimeMillis())
				    .nextInt());
	} else {
	    try {
		byte[] ownIP = InetAddress.getLocalHost().getAddress();
		int val;
		for (int i = 0; i < ownIP.length; i++) {
		    val = ownIP[i] & 0xFF;
		    aux += (val < 16 ? "0" : "") + Integer.toHexString(val);
		}
	    } catch (Exception e) {
	    }
	    aux += Integer.toHexString(new Random(System.currentTimeMillis())
		    .nextInt());
	}
	UUID_prefix = aux + ":";
    }

    /** Create a unique ID. */
    public static String createUniqueID() {
	return UUID_prefix + Integer.toHexString(counter++);
    }

    /**
     * Tests whether two property paths are equal, i.e. have the same length and
     * all elements are equal.
     */
    public static boolean areEqualPropPaths(String[] pp1, String[] pp2) {
	if (pp1 == pp2)
	    return true;

	if (pp1 == null || pp2 == null || pp1.length != pp2.length)
	    return false;

	for (int i = pp1.length - 1; i > -1; i--)
	    if (pp1[i] == null || !pp1[i].equals(pp2[i]))
		return false;

	return true;
    }

    /**
     * This method tries to derive a meaningful label from a given String. If
     * the String is a qualified name (see {@link #isQualifiedName}), only the
     * local part - the part after '#' - is taken. Multiple words starting with
     * upper case letters and written in one word are separated (e.g. 'myName'
     * is transformed to 'My Name').
     */
    public static String deriveLabel(String arg) {
	if (arg == null)
	    return null;

	// if the arg has a structure like a qualified name, take just its local
	// part
	if (isQualifiedName(arg))
	    arg = arg.substring(arg.lastIndexOf('#') + 1);

	if (arg.length() == 0)
	    return null;

	StringBuffer sb = new StringBuffer(arg.length() + 10);
	int i = 0;
	int wordStatus = 0; // 0->start word, 1->within word, 2->undefined
	while (i < arg.length()) {
	    char c = arg.charAt(i++);
	    if (Character.isLetter(c)) {
		switch (wordStatus) {
		case 0:
		    sb.append(Character.toUpperCase(c));
		    wordStatus = 1;
		    break;
		case 1:
		    if (Character.isUpperCase(c))
			sb.append(' ');
		    sb.append(c);
		    break;
		case 2:
		    sb.append(' ').append(Character.toUpperCase(c));
		    wordStatus = 1;
		    break;
		}
	    } else if (c == '_') {
		sb.append(' ');
		wordStatus = 0;
	    } else {
		if (wordStatus == 1)
		    sb.append(' ');
		sb.append(c);
		wordStatus = 2;
	    }
	}
	return sb.toString();
    }

    /** Determines if the specified character is a digit [0-9]. */
    public static boolean isDigit(char c) {
	return c >= '0' && c <= '9';
    }

    /** Determines if the specified character is a letter [a-z,A-Z]. */
    public static boolean isAsciiLetter(char c) {
	return (c >= 'A' && c <= 'Z') || (c <= 'z' && c >= 'a');
    }

    /** Determines if the specified String is null or empty. */
    public static boolean isNullOrEmpty(String arg) {
	return arg == null || arg.equals("");
    }

    /** Determines if the specified String is not null and not empty. */
    public static boolean isNonEmpty(String arg) {
	return arg != null && !arg.equals("");
    }

    /**
     * Determines if the specified URI String is a qualified name. The following
     * conditions are checked:
     * <ul>
     * <li>the String starts with a URI scheme (see {@link #startsWithURIScheme}
     * )</li>
     * <li>the String contains the symbol '#'</li>
     * <li>there is at least one character following the symbol '#'</li>
     * </ul>
     */
    public static boolean isQualifiedName(String uri) {
	if (!startsWithURIScheme(uri))
	    return false;

	int i = uri.lastIndexOf('#');
	return i > 0 && i < uri.length() - 1; // at least one char is present
					      // after '#'
    }

    /**
     * Determines if a prefix of the specified String is conform to an URI
     * definition. The following conditions are checked:
     * <ul>
     * <li>the String starts with a letter ([a-z,A-Z])</li>
     * <li>the String contains the symbol ':'</li>
     * <li>all characters from the beginning to the symbol ':' are either a
     * letter, a digit, or one of [+, -, .]</li>
     * </ul>
     */
    public static boolean startsWithURIScheme(String arg) {
	if (arg == null || arg.length() == 0)
	    return false;

	char c = arg.charAt(0);
	int i = arg.indexOf(':');
	if (i < 1 || !isAsciiLetter(c))
	    return false;

	while (--i > 0) {
	    c = arg.charAt(i);
	    if (!isAsciiLetter(c) && !isDigit(c) && c != '+' && c != '-'
		    && c != '.')
		return false;
	}

	return true;
    }

    public static void main(String args[]) {
	System.out.println(deriveLabel("kjD2390 ösd ydg: öasä"));
    }
}
