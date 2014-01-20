package org.universAAL.middleware.owl;

import junit.framework.TestCase;

public class TypeExpressionTest extends TestCase {

    protected void setUp() throws Exception {
	super.setUp();
    }

    public void testCycle() {
	Intersection i = new Intersection();
	Union u = new Union();

	i.addType(u);
	u.addType(i);
	boolean gotException = false;

	try {
	    i.matches(u, null, TypeExpression.getDefaultMatchmakingTTL(), null);
	} catch (IllegalArgumentException e) {
	    if (TypeExpression.EXCEPTION_TTL.equals(e.getMessage()))
		gotException = true;
	}
	assertTrue(gotException);

	gotException = false;

	try {
	    u.matches(i, null, TypeExpression.getDefaultMatchmakingTTL(), null);
	} catch (IllegalArgumentException e) {
	    if (TypeExpression.EXCEPTION_TTL.equals(e.getMessage()))
		gotException = true;
	}
	assertTrue(gotException);
    }
}
