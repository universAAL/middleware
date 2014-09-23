/*******************************************************************************
 * Copyright 2011 Universidad Politécnica de Madrid - Life Supporting Technologies
 * Copyright 2013 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.middleware.managers.configuration.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;

/**
 * Find the resources referenced by urls.
 * 
 * @author amedrano
 * 
 */
public final class ResourceMapper {

    /**
     * {@link ModuleContext} to enable logging.
     */
    public static ModuleContext mc;

    /**
     * Utility class. no instance allowed.
     */
    private ResourceMapper() {
    }

    /**
     * Utility method: it will look for the resource in the cache, if it not
     * available it will copy it there. Returns the cache location.
     * 
     * @param cacheFolder
     *            the location of the cache folder.
     * @param resource
     *            the file to cache
     * @return the cached location.
     */
    public static File cached(File cacheFolder, URL resource) {
	if (resource == null)
	    return null;
	String extension = resource.getFile();
	if (extension != null) {
	    extension = extension.substring(extension.lastIndexOf('.'));
	} else {
	    extension = "";
	}
	String coded = Integer.toString(resource.toString().hashCode())
		+ extension;
	File cached = new File(cacheFolder, coded);
	if (!cached.exists() || cached.getParentFile().mkdirs()) {
	    // copy
	    try {
		new Retreiver(resource.openStream(), cached);
		// store reference for when the dialog is finished the Retriever
		// is stoped.
	    } catch (IOException e) {
		if (mc != null)
		    LogUtils.logError(
			    mc,
			    ResourceMapper.class,
			    "cached",
			    new String[] { "It seems it is not possible to cache file " },
			    e);
	    }
	}
	return cached;
    }

    /**
     * A class that will perform copy operation in a thread.
     * 
     * @author amedrano
     * 
     */
    static public class Retreiver implements Runnable {

	private boolean work = true;
	private InputStream is;
	private File file;

	/**
	 * @param is
	 * @param file
	 */
	public Retreiver(InputStream is, File file) {
	    super();
	    this.is = is;
	    this.file = file;
	    new Thread(this, "Retriever for " + file.getName()).start();
	}

	/** {@ inheritDoc} */
	protected void finalize() throws Throwable {
	    finish();
	    super.finalize();
	}

	/** {@ inheritDoc} */
	public void run() {
	    try {
		if (file.getParentFile().exists()
			|| file.getParentFile().mkdirs()) {
		    FileOutputStream os = new FileOutputStream(file);
		    byte[] buffer = new byte[4096];
		    int bytesRead;
		    while (((bytesRead = is.read(buffer)) != -1) && work) {
			os.write(buffer, 0, bytesRead);
		    }
		    is.close();
		    os.flush();
		    os.close();
		    if (!work) {
			file.delete();
		    }
		}
	    } catch (FileNotFoundException e) {
		if (mc != null)
		    LogUtils.logError(
			    mc,
			    Retreiver.class,
			    "run",
			    new String[] { "cache seems not to exists, or file: "
				    + file.getAbsolutePath()
				    + " is not accessible" }, e);
	    } catch (IOException e) {
		if (mc != null)
		    LogUtils.logError(
			    mc,
			    Retreiver.class,
			    "run",
			    new String[] { "It seems it is not possible to cache file " },
			    e);
	    } finally {
		try {
		    is.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }

	}

	public void finish() {
	    work = false;
	}
    }
}
