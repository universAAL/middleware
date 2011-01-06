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
 * @author mtazari - <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 *
 */
public class StringUtils {

	public static boolean areEqualPropPaths(String[] pp1, String[] pp2) {
		if (pp1 == pp2)
			return true;
		
		if (pp1 == null  ||  pp2 == null  ||  pp1.length != pp2.length)
			return false;
		
		for (int i=pp1.length-1; i>-1; i--)
			if (pp1[i] == null  ||  !pp1[i].equals(pp2[i]))
				return false;
		
		return true;
	}

	private static int counter = 0;
	private static final String UUID_prefix;
	static {
		String aux = "_:";
		try {
		    byte[] ownIP = InetAddress.getLocalHost().getAddress();
		    for (int i=0; i<4; i++)
		    	aux += (ownIP[i]<16? "0" : "") + Integer.toHexString(ownIP[i]);
		} catch (Exception e) {
			aux += Integer.toHexString(new Random(System.currentTimeMillis()).nextInt());
		}
	    UUID_prefix = aux + ":";
	}
	public static String createUniqueID() {
		return UUID_prefix + Integer.toHexString(counter++); 
	}
	
	public static String deriveLabel(String arg) {
		if (arg == null)
			return null;
		
		// if the arg has a structure like a qualified name, take just its local part
		if (isQualifiedName(arg))
			arg = arg.substring(arg.lastIndexOf('#') + 1);
		
		if (arg.length() == 0)
			return null;
		
		StringBuffer sb = new StringBuffer(arg.length()+10);
		int i = 0;
		int wordStatus = 0; // 0->start word, 1->within word, 2->undefined
		while (i<arg.length()) {
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
	
	public static boolean isDigit(char c) {
		return c >= '0'  &&  c <= '9';
	}
	
	public static boolean isAsciiLetter(char c) {
		return (c >= 'A' && c <= 'Z') || (c <= 'z' && c >= 'a');
	}
	
	public static boolean isNullOrEmpty(String arg) {
		return arg == null  ||  arg.equals("");
	}
	
	public static boolean isNonEmpty(String arg) {
		return arg != null  &&  !arg.equals("");
	}
	
	public static boolean isQualifiedName(String uri) {
		if (!startsWithURIScheme(uri))
			return false;
		
		int i = uri.lastIndexOf('#');
		return  i > 0
			&&  i < uri.length()-1; // at list one char is present after '#'
	}
	
	public static boolean startsWithURIScheme(String arg) {
		if (arg == null  ||  arg.length() == 0)
			return false;
		
		char c = arg.charAt(0);
		int i = arg.indexOf(':');
		if (i < 1  ||  !isAsciiLetter(c))
			return false;
		
		while (--i > 0) {
			c = arg.charAt(i);
			if (!isAsciiLetter(c)  &&  !isDigit(c)  &&  c != '+'  &&  c != '-'  &&  c!= '.')
				return false;
		}
		
		return true;
	}
}
