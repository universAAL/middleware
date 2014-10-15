/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

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
package org.universAAL.middleware.brokers.control;

import java.io.File;
import java.io.FileOutputStream;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;

/**
 * A simple class for handling common file operation
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2013-04-02 14:36:09
 *          +0200 (Tue, 02 Apr 2013) $)
 * 
 */
public class FileUtils {

    public static File createFileFromByte(ModuleContext mc, byte[] content,
	    String dst) {
	return createFileFromByte(mc, content, dst, false);
    }

    public static File createFileFromByte(ModuleContext mc, byte[] content,
	    String dst, boolean overwrite) {
	final String METHOD = "createFileFromBytele";
	File file = new File(dst);
	File parent = file.getParentFile();
	if (file.exists() == true && file.isDirectory() == true) {
	    LogUtils.logError(
		    mc,
		    FileUtils.class,
		    METHOD,
		    new Object[] { "Error while creating file the destination "
			    + file.getPath() + " exists but it is a directory" },
		    null);
	    return null;
	}
	if (file.exists() && overwrite == true && file.delete() == false) {
	    LogUtils.logError(
		    mc,
		    FileUtils.class,
		    METHOD,
		    new Object[] { "Error while creating file the destination "
			    + file.getPath() + " exists but couldn't delete it" },
		    null);
	    return null;
	} else if (file.exists() == true && overwrite == false) {
	    LogUtils.logError(mc, FileUtils.class, METHOD,
		    new Object[] { "Error while creating file the destination "
			    + file.getPath() + " exists" }, null);
	    return null;
	}
	if (parent == null) {
	    LogUtils.logDebug(
		    mc,
		    FileUtils.class,
		    METHOD,
		    new Object[] { "The file is considered as relative path, so the file will created in "
			    + new File(".").getAbsolutePath() }, null);
	} else {
	    if (parent.exists() == true && parent.isDirectory() == false) {
		LogUtils.logError(
			mc,
			FileUtils.class,
			METHOD,
			new Object[] { "Error while creating file the destination folder "
				+ parent.getPath() + " exists but it is a file" },
			null);
		return null;
	    }
	    if (parent.exists() == true && parent.canWrite() == false) {
		LogUtils.logError(
			mc,
			FileUtils.class,
			METHOD,
			new Object[] { "Error while creating file the destination folder "
				+ parent.getPath()
				+ " exists but we don't have permission to write" },
			null);
		return null;
	    }
	    if (parent.exists() == false && parent.mkdirs() == false) {
		LogUtils.logError(
			mc,
			FileUtils.class,
			METHOD,
			new Object[] { "Error while creating file the destination folder "
				+ parent.getPath()
				+ " does not exist and the creation of folder failed" },
			null);
		return null;
	    }
	}
	try {
	    file.createNewFile();
	    FileOutputStream fos;
	    fos = new FileOutputStream(file);
	    fos.write(content);
	    fos.flush();
	    fos.close();
	} catch (Exception ex) {
	    LogUtils.logError(
		    mc,
		    FileUtils.class,
		    METHOD,
		    new Object[] { "Error while creating file "
			    + file.getPath() }, null);
	    return null;
	}
	return file;
    }
}
