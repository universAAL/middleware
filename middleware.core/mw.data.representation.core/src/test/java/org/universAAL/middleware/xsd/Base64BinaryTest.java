package org.universAAL.middleware.xsd;

import junit.framework.TestCase;

public class Base64BinaryTest extends TestCase {

	public void test1() {
		String s = "";
		for (int i = 0; i < 100; i++) {
			Base64Binary b = new Base64Binary(s.getBytes());

			boolean bool = b.getDecodedLength() == s.length();
			if (!bool) {
				System.out.println(" ----------- ");
				System.out.println(b.toString());
				System.out.println(s);
				System.out.println(b.getDecodedLength());
				assertTrue(bool);
			}

			s += "a";
		}
	}
}
