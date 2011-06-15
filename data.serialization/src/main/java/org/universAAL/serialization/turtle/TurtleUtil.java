/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
	Copyright Aduna (http://www.aduna-software.com/) © 2001-2007
	
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
package org.universAAL.serialization.turtle;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.util.StringUtils;

/**
 * A Set of utility functions for TURTLE.
 * 
 * @author mtazari
 * @author Carsten Stockloew
 */
// For the integer values used in some of the methods, please take a look at
// the TURTLE specification: http://www.w3.org/TeamSubmission/turtle/
public class TurtleUtil {

    /** URI for RDF XML Literals. */
    static String xmlLiteral = Resource.RDF_NAMESPACE + "XMLLiteral";

    /**
     * Reference to the {@link org.universAAL.middleware.rdf.TypeMapper}
     * instance.
     */
    static TypeMapper typeMapper = null;

    /**
     * Decode a String that has been encoded before by replacing some escaped
     * sequence with their according character representation.
     */
    static String decodeString(String s) {
	int backSlashIdx = s.indexOf('\\');

	if (backSlashIdx == -1) {
	    // No escaped characters found
	    return s;
	}

	int startIdx = 0;
	int sLength = s.length();
	StringBuffer sb = new StringBuffer(sLength);

	while (backSlashIdx != -1) {
	    sb.append(s.substring(startIdx, backSlashIdx));

	    if (backSlashIdx + 1 >= sLength) {
		throw new IllegalArgumentException("Unescaped backslash in: "
			+ s);
	    }

	    char c = s.charAt(backSlashIdx + 1);

	    if (c == 't') {
		sb.append('\t');
		startIdx = backSlashIdx + 2;
	    } else if (c == 'r') {
		sb.append('\r');
		startIdx = backSlashIdx + 2;
	    } else if (c == 'n') {
		sb.append('\n');
		startIdx = backSlashIdx + 2;
	    } else if (c == '"') {
		sb.append('"');
		startIdx = backSlashIdx + 2;
	    } else if (c == '>') {
		sb.append('>');
		startIdx = backSlashIdx + 2;
	    } else if (c == '\\') {
		sb.append('\\');
		startIdx = backSlashIdx + 2;
	    } else if (c == 'u') {
		// \\uxxxx
		if (backSlashIdx + 5 >= sLength) {
		    throw new IllegalArgumentException(
			    "Incomplete Unicode escape sequence in: " + s);
		}
		String xx = s.substring(backSlashIdx + 2, backSlashIdx + 6);

		try {
		    c = (char) Integer.parseInt(xx, 16);
		    sb.append(c);

		    startIdx = backSlashIdx + 6;
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException(
			    "Illegal Unicode escape sequence '\\u" + xx
				    + "' in: " + s);
		}
	    } else if (c == 'U') {
		// \\Uxxxxxxxx
		if (backSlashIdx + 9 >= sLength) {
		    throw new IllegalArgumentException(
			    "Incomplete Unicode escape sequence in: " + s);
		}
		String xx = s.substring(backSlashIdx + 2, backSlashIdx + 10);

		try {
		    c = (char) Integer.parseInt(xx, 16);
		    sb.append(c);

		    startIdx = backSlashIdx + 10;
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException(
			    "Illegal Unicode escape sequence '\\U" + xx
				    + "' in: " + s);
		}
	    } else {
		throw new IllegalArgumentException("Unescaped backslash in: "
			+ s);
	    }

	    backSlashIdx = s.indexOf('\\', startIdx);
	}

	sb.append(s.substring(startIdx));

	return sb.toString();
    }

    /**
     * Encode a Literal String with line breaks by replacing some characters
     * with their escaped sequence.
     */
    static String encodeLongString(String s) {
	// TODO: not all double quotes need to be escaped. It suffices to encode
	// the ones that form sequences of 3 or more double quotes, and the ones
	// at the end of a string.
	s = globalReplaceChar('\\', "\\\\", s);
	s = globalReplaceChar('"', "\\\"", s);
	return s;
    }

    /**
     * Encode a Literal String without line breaks by replacing some characters
     * with their escaped sequence.
     */
    static String encodeString(String s) {
	s = globalReplaceChar('\\', "\\\\", s);
	s = globalReplaceChar('\t', "\\t", s);
	s = globalReplaceChar('\n', "\\n", s);
	s = globalReplaceChar('\r', "\\r", s);
	s = globalReplaceChar('"', "\\\"", s);
	return s;
    }

    /**
     * Encode an URI String by replacing some characters with their escaped
     * sequence.
     */
    static String encodeURIString(String s) {
	s = globalReplaceChar('\\', "\\\\", s);
	s = globalReplaceChar('>', "\\>", s);
	return s;
    }

    /**
     * For the given URI find the index for splitting the URI into a prefix and
     * the local name of the URI. For example, the URI
     * <code>myOntology#myfunction</code> would be split directly after the
     * symbol '#' to create the prefix <code>myOntology#</code> and the
     * local name <code>myfunction</code>.
     */
    static int findURISplitIndex(String uri) {
	int uriLength = uri.length();

	int idx = uriLength - 1;

	// Search last character that is not a name character
	for (; idx >= 0; idx--) {
	    if (!TurtleUtil.isNameChar(uri.charAt(idx))) {
		// Found a non-name character
		break;
	    }
	}

	idx++;

	// Local names need to start with a 'nameStartChar', skip characters
	// that are not nameStartChar's.
	for (; idx < uriLength; idx++) {
	    if (TurtleUtil.isNameStartChar(uri.charAt(idx))) {
		break;
	    }
	}

	if (idx > 0 && idx < uriLength) {
	    // A valid split index has been found
	    return idx;
	}

	// No valid local name has been found
	return -1;
    }

    /**
     * Replaces all occurrences of a specified char in a String with the given
     * String.
     * 
     * @param c
     *            The character that is to be replaced.
     * @param rpl
     *            The String that will replace the character.
     * @param input
     *            The input which has to be investigated.
     * @return The String with replacements.
     */
    private static String globalReplaceChar(char c, String rpl, String input) {
	char aux;
	int n = input.length();
	StringBuffer sb = new StringBuffer(n << 1);
	for (int i = 0; i < n; i++) {
	    aux = input.charAt(i);
	    if (c == aux)
		sb.append(rpl);
	    else
		sb.append(aux);
	}
	return sb.toString();
    }

    /**
     * Determines if the specified character is either an ASCII letter, a digit,
     * or the symbol '-'.
     */
    static boolean isLanguageChar(int c) {
	return StringUtils.isAsciiLetter((char) c)
		|| StringUtils.isDigit((char) c) || c == '-';
    }

    /** Determines if the specified character is a letter [a-z,A-Z]. */
    static boolean isLanguageStartChar(int c) {
	return StringUtils.isAsciiLetter((char) c);
    }

    /**
     * Determines if the specified character is a valid character for a name,
     * i.e. a letter, a digit or a special symbol.
     */
    static boolean isNameChar(int c) {
	return isNameStartChar(c) || StringUtils.isDigit((char) c) || c == '-'
		|| c == 0x00B7 || c >= 0x0300 && c <= 0x036F || c >= 0x203F
		&& c <= 0x2040;
    }

    /**
     * Determines if the specified character is either a letter or the symbol
     * '_'.
     */
    static boolean isNameStartChar(int c) {
	return c == '_' || isPrefixStartChar(c);
    }

    /** Same as {@link #isNameChar(int)}. */
    static boolean isPrefixChar(int c) {
	return isNameChar(c);
    }

    /** Determines if the specified character is a letter. */
    static boolean isPrefixStartChar(int c) {
	return StringUtils.isAsciiLetter((char) c) || c >= 0x00C0
		&& c <= 0x00D6 || c >= 0x00D8 && c <= 0x00F6 || c >= 0x00F8
		&& c <= 0x02FF || c >= 0x0370 && c <= 0x037D || c >= 0x037F
		&& c <= 0x1FFF || c >= 0x200C && c <= 0x200D || c >= 0x2070
		&& c <= 0x218F || c >= 0x2C00 && c <= 0x2FEF || c >= 0x3001
		&& c <= 0xD7FF || c >= 0xF900 && c <= 0xFDCF || c >= 0xFDF0
		&& c <= 0xFFFD || c >= 0x10000 && c <= 0xEFFFF;
    }

    /**
     * Determines if the specified character is a whitespace (space, tab,
     * newline or carriage return).
     */
    static boolean isWhitespace(int c) {
	// Whitespace character are space, tab, newline and carriage return:
	return c == 0x20 || c == 0x9 || c == 0xA || c == 0xD;
    }

    // public static void main(String args[]) {
    // for (int c=0x00C0; c<=0x00D6; c++)
    // System.out.println((char)c);
    // }
}
