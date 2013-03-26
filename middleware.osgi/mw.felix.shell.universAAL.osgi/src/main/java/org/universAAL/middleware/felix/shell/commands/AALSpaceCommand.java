package org.universAAL.middleware.felix.shell.commands;

import java.io.PrintStream;
import java.util.Set;

import org.apache.felix.shell.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.managers.api.AALSpaceManager;

public class AALSpaceCommand implements Command {
    private BundleContext m_context = null;
    private AALSpaceManager aalSpaceManager;

    public AALSpaceCommand(BundleContext context) {
	this.m_context = context;
    }

    public void execute(String arg0, PrintStream out, PrintStream err) {
	ServiceReference ref = m_context
		.getServiceReference(AALSpaceManager.class.getName());
	if (ref != null) {
	    aalSpaceManager = (AALSpaceManager) m_context.getService(ref);
	} else {
	    err.println("AALSapceManager not found");
	    return;
	}
	Set<AALSpaceCard> aalSpaces = aalSpaceManager.getAALSpaces();
	if (aalSpaces != null) {
	    out.println(" Found: " + aalSpaces.size() + " AAL Spaces");
	    out.println(" ----------------------------------------");
	    for (AALSpaceCard aalSpace : aalSpaces) {

		if (aalSpaceManager.getAALSpaceDescriptor() != null
			&& aalSpace.equals(aalSpaceManager
				.getAALSpaceDescriptor().getSpaceCard()))
		    out.println(" * " + aalSpace.toString());
		else
		    out.println(aalSpace.toString());
	    }
	}

    }

    public String getName() {
	return "universAAL:aalsapces";
    }

    public String getShortDescription() {

	return "List the AAL Spaces found on this network";
    }

    public String getUsage() {
	// TODO Auto-generated method stub
	return null;
    }

}
