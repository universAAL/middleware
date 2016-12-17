/*******************************************************************************
 * Copyright 2016 Universidad Politécnica de Madrid
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

package mw.karaf.branding.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author amedrano
 *
 */
public class UTF8toJavaCode {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String utf;
		try {
			utf = readFile(args[0]);
		} catch (IOException e) {
			e.printStackTrace();
			utf="";
		}

		// convert the input string to a character array  
		String text = StringEscapeUtils.escapeJava(utf);
		  
		// display the ASCII encoded string  
		System.out.println ("String s = " + text);  
	}

	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}
}