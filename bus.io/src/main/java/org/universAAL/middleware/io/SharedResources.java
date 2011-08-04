/**
 * 
 */
package org.universAAL.middleware.io;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;
import org.universAAL.middleware.util.ResourceComparator;

/**
 * @author mtazari
 * 
 */
public class SharedResources {
    public static MessageContentSerializer contentSerializer = null;
    public static ModuleContext moduleContext;
    public static Object[] contentSerializerParams;

    public static synchronized void assessContentSerialization(Resource content) {
	if (org.universAAL.middleware.util.Constants.debugMode()) {
	    if (contentSerializer == null) {
		contentSerializer = (MessageContentSerializer) moduleContext
			.getContainer().fetchSharedObject(moduleContext,
				contentSerializerParams);
		if (contentSerializer == null)
		    return;
	    }

	    LogUtils
		    .logDebug(
			    moduleContext,
			    SharedResources.class,
			    "assessContentSerialization",
			    new Object[] { "Assessing message content serialization:" },
			    null);

	    String str = contentSerializer.serialize(content);
	    LogUtils
		    .logDebug(
			    moduleContext,
			    SharedResources.class,
			    "assessContentSerialization",
			    new Object[] { "\n      1. serialization dump\n",
				    str,
				    "\n      2. deserialize & compare with the original resource\n" },
			    null);
	    new ResourceComparator().printDiffs(content,
		    (Resource) contentSerializer.deserialize(str));
	}
    }

    public static void loadExportedClasses() throws ClassNotFoundException {
	Class.forName("org.universAAL.middleware.input.InputEvent");
	Class.forName("org.universAAL.middleware.io.owl.AccessImpairment");
	Class.forName("org.universAAL.middleware.io.owl.DialogType");
	Class.forName("org.universAAL.middleware.io.owl.Gender");
	Class.forName("org.universAAL.middleware.io.owl.Modality");
	Class.forName("org.universAAL.middleware.io.owl.PrivacyLevel");
	Class.forName("org.universAAL.middleware.io.rdf.ChoiceItem");
	Class.forName("org.universAAL.middleware.io.rdf.ChoiceList");
	Class.forName("org.universAAL.middleware.io.rdf.Form");
	Class.forName("org.universAAL.middleware.io.rdf.Group");
	Class.forName("org.universAAL.middleware.io.rdf.InputField");
	Class.forName("org.universAAL.middleware.io.rdf.Label");
	Class.forName("org.universAAL.middleware.io.rdf.MediaObject");
	Class.forName("org.universAAL.middleware.io.rdf.Range");
	Class.forName("org.universAAL.middleware.io.rdf.Repeat");
	Class.forName("org.universAAL.middleware.io.rdf.Select");
	Class.forName("org.universAAL.middleware.io.rdf.Select1");
	Class.forName("org.universAAL.middleware.io.rdf.SimpleOutput");
	Class.forName("org.universAAL.middleware.io.rdf.SubdialogTrigger");
	Class.forName("org.universAAL.middleware.io.rdf.Submit");
	Class.forName("org.universAAL.middleware.io.rdf.TextArea");
	Class.forName("org.universAAL.middleware.output.OutputEvent");
	Class.forName("org.universAAL.middleware.output.OutputEventPattern");
    }

}
