/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
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
import java.io.IOException;
import java.net.URL;

import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.FileManagement;

/**
 * @author amedrano
 *
 */
public class FileProvider implements FileManagement{

    private static int index = 0;

    private File mainCFG;
    
    public FileProvider(File mainFolder){
	mainCFG = mainFolder;
    }
    
    /** {@ inheritDoc}	 */
    public File cache(URL url) {
	return ResourceMapper.cached(new File(mainCFG, "cache"), url);
    }

    /** {@ inheritDoc}	 */
    public File getMasterFile() {
	File f = new File(mainCFG, "configurationDB.ttl");
	f.getParentFile().mkdirs();
	return f;
    }

    /** {@ inheritDoc}	 */
    public File getLocalFile(String id) {
	File f = new File(new File(mainCFG, "localFiles"), id);
	f.getParentFile().mkdirs();
	return f;
    }

    /** {@ inheritDoc}	 */
    public File getTemporalFile() {
	try {
	    return File.createTempFile("configManagerTempFile" + index ++, ".dat");
	} catch (IOException e) {
	    return getLocalFile("configManagerTempFile" + index ++);
	}
    }
     
}
