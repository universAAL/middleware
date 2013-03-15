package org.universAAL.middleware.serialization.turtle;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;

public class TurtleSerializer implements MessageContentSerializerEx {

    private static int cnt = 0;

    /** @see org.universAAL.middleware.serialization.MessageContentSerializer#deserialize(String) */
    public synchronized Object deserialize(String serialized) {
	cnt++;
	String s = "";
	if (TurtleParser.dbg) s = "\n\n------- Turtle start cnt: " + cnt + "\n" + serialized + "\n ------\n\n";
	if (TurtleParser.dbg) System.out.println(s);
	Object o = deserialize(serialized, null);
	if (TurtleParser.dbg) System.out.println("-- Turtle result:");
	if (TurtleParser.dbg) System.out.println(((Resource)o).toStringRecursive());
	if (TurtleParser.dbg) System.out.println("-- Turtle ende cnt: " + cnt);
	return o;
    }

    /**
     * @see org.universAAL.middleware.serialization.MessageContentSerializerEx#deserialize(String,
     *      String)
     */
    public synchronized Object deserialize(String serialized, String resourceURI) {
	try {
	    TurtleParser parser = new TurtleParser();
	    return parser.deserialize(serialized, resourceURI);
	} catch (Exception ex) {
	    LogUtils.logError(TurtleUtil.moduleContext, TurtleSerializer.class,
		    "deserialize", null, ex);
	    return null;
	}
    }

    /** @see org.universAAL.middleware.serialization.MessageContentSerializer#serialize(Object) */
    public String serialize(Object messageContent) {
	return TurtleWriter.serialize(messageContent, 0);
    }
}
