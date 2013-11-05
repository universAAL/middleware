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
 */package org.universAAL.middleware.interfaces.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Dictionary;
import java.util.Properties;

/**
 * Utility class for widely-used operations
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class Util {

	/**
	 * This method
	 * 
	 * @param context
	 * @return
	 */
	public static Dictionary<String, String> getSLPProperties(URL propUrl) {
		Properties prop = new Properties();
		if (propUrl != null) {
			InputStream input;
			try {
				input = propUrl.openStream();
			} catch (IOException e1) {
				return null;
			}
			try {

				try {
					prop.load(input);
					return (Dictionary) prop;
				} catch (IOException e) {

					return null;
				}
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					return null;
				}
			}
		}
		return (Dictionary) prop;

	}

	/**
	 * This method returns a File object found in the target dir containing the
	 * marker
	 * 
	 * @param marker
	 * @param targetDir
	 * @return
	 */
	public static File getFile(final String marker, URI targetDir) {
		File targetFolder = new File(targetDir);
		String[] files = targetFolder.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.contains(marker));
			}
		});
		if (files == null || files.length <= 0) {
			return null;
		} else {
			return new File(targetFolder.toString() + File.separatorChar
					+ files[0]);
		}
	}

}
