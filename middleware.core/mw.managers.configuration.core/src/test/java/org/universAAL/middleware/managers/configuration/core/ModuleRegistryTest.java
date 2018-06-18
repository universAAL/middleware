/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
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

package org.universAAL.middleware.managers.configuration.core;

//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.universAAL.middleware.container.JUnit.JUnitModuleContext;
//import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.interfaces.configuration.ConfigurableModule;
import org.universAAL.middleware.interfaces.configuration.configurationDefinitionTypes.DescribedEntity;
import org.universAAL.middleware.interfaces.configuration.scope.Scope;
//import org.universAAL.middleware.managers.api.ConfigurationManager;
//import org.universAAL.middleware.managers.configuration.core.impl.ConfigurationManagerImpl;
import org.universAAL.middleware.managers.configuration.core.impl.factories.ScopeFactory;
//import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.FileManagement;
import org.universAAL.middleware.managers.configuration.core.impl.secondaryManagers.ModuleRegistry;
import org.universAAL.middleware.managers.configuration.core.owl.ConfigurationOntology;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleSerializer;
import org.universAAL.middleware.serialization.turtle.TurtleUtil;

/**
 * @author amedrano
 *
 */
public class ModuleRegistryTest {

	private class TestModule implements ConfigurableModule {

		boolean configured = false;

		/** {@ inheritDoc} */
		public boolean configurationChanged(Scope param, Object value) {
			configured = true;
			return true;
		}
	}

//	/**
//	 * Utility method: it will look for the resource in the cache, if it not
//	 * available it will copy it there. Returns the cache location.
//	 *
//	 * @param cacheFolder
//	 *            the location of the cache folder.
//	 * @param resource
//	 *            the file to cache
//	 * @return the cached location.
//	 */
//	public static File cached(File cacheFolder, URL resource) {
//		if (resource == null)
//			return null;
//		String extension = resource.getFile();
//		if (extension != null) {
//			extension = extension.substring(extension.lastIndexOf('.'));
//		} else {
//			extension = "";
//		}
//		String coded = Integer.toString(resource.toString().hashCode()) + extension;
//		File cached = new File(cacheFolder, coded);
//		if (!cached.exists() || cached.getParentFile().mkdirs()) {
//			// copy
//			try {
//				new Retreiver(resource.openStream(), cached);
//				// store reference for when the dialog is finished the Retriever
//				// is stoped.
//			} catch (IOException e) {
//				if (mc != null)
//					LogUtils.logError(mc, TestFileProvider.class, "cached",
//							new String[] { "It seems it is not possible to cache file " }, e);
//			}
//		}
//		return cached;
//	}
//
//	/**
//	 * A class that will perform copy operation in a thread.
//	 *
//	 * @author amedrano
//	 *
//	 */
//	static public class Retreiver implements Runnable {
//
//		private boolean work = true;
//		private InputStream is;
//		private File file;
//
//		/**
//		 * @param is
//		 * @param file
//		 */
//		public Retreiver(InputStream is, File file) {
//			super();
//			this.is = is;
//			this.file = file;
//			new Thread(this, "Retriever for " + file.getName()).start();
//		}
//
//		/** {@ inheritDoc} */
//		protected void finalize() throws Throwable {
//			finish();
//			super.finalize();
//		}
//
//		/** {@ inheritDoc} */
//		public void run() {
//			try {
//				if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
//					FileOutputStream os = new FileOutputStream(file);
//					byte[] buffer = new byte[4096];
//					int bytesRead;
//					while (((bytesRead = is.read(buffer)) != -1) && work) {
//						os.write(buffer, 0, bytesRead);
//					}
//					is.close();
//					os.flush();
//					os.close();
//					if (!work) {
//						file.delete();
//					}
//				}
//			} catch (FileNotFoundException e) {
//				if (mc != null)
//					LogUtils.logError(mc, Retreiver.class, "run", new String[] {
//							"cache seems not to exists, or file: " + file.getAbsolutePath() + " is not accessible" },
//							e);
//			} catch (IOException e) {
//				if (mc != null)
//					LogUtils.logError(mc, Retreiver.class, "run",
//							new String[] { "It seems it is not possible to cache file " }, e);
//			} finally {
//				try {
//					is.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//
//		}
//
//		public void finish() {
//			work = false;
//		}
//	}
//
//
//private class TestFileProvider implements FileManagement {
//
//
//	private File mainCFG;
//
//	public TestFileProvider(File mainFolder) {
//		mainCFG = mainFolder;
//	}
//
//	/** {@ inheritDoc} */
//	public File cache(URL url) {
//		return cached(new File(mainCFG, "cache"), url);
//	}
//
//	/** {@ inheritDoc} */
//	public File getMasterFile() {
//		File f = new File(mainCFG, "configurationDB.ttl");
//		f.getParentFile().mkdirs();
//		return f;
//	}
//
//	/** {@ inheritDoc} */
//	public File getLocalFile(String id) {
//		File f = new File(new File(mainCFG, "localFiles"), id);
//		f.getParentFile().mkdirs();
//		return f;
//	}
//
//	/** {@ inheritDoc} */
//	public File getTemporalFile() {
//		try {
//			return File.createTempFile("configManagerTempFile" + index++, ".dat");
//		} catch (IOException e) {
//			return getLocalFile("configManagerTempFile" + index++);
//		}
//	}
//}
//
//private static int index = 0;
	private static JUnitModuleContext mc;

	@BeforeClass
	public static void init() {
		mc = new JUnitModuleContext();
		mc.getContainer().shareObject(mc, new TurtleSerializer(),
				new Object[] { MessageContentSerializer.class.getName() });

		OntologyManagement.getInstance().register(mc, new DataRepOntology());
		OntologyManagement.getInstance().register(mc, new ConfigurationOntology());
		TurtleUtil.moduleContext = mc;
	}

	@Test
	public void simpleTest() {
		DescribedEntity[] des = ConfigSample.getConfigurationDescription();
//		ConfigurationManager cm = new ConfigurationManagerImpl(TurtleUtil.moduleContext, new TestFileProvider(mc.getConfigHome()));
//		cm.register(des, new TestModule());
//		des = ConfigSample.getConfigurationDescription();
//		cm.register(des, new TestModule());
		String urn = ScopeFactory.getScopeURN(des[0].getScope());
		ModuleRegistry mr = new ModuleRegistry();
		TestModule tm1 = new TestModule();
		mr.put(urn, tm1);
		assertTrue(mr.contains(urn));
		assertFalse(mr.configurationChanged(des[1].getScope(), 1));
		assertTrue(mr.configurationChanged(des[0].getScope(), 1));
		assertTrue(tm1.configured);
		mr.remove(tm1);
		assertFalse(mr.contains(urn));
		mr.clear();
	}

	@Test
	public void multipleTest() {
		DescribedEntity[] des = ConfigSample.getConfigurationDescription();
		String urn = ScopeFactory.getScopeURN(des[0].getScope());
		ModuleRegistry mr = new ModuleRegistry();
		TestModule tm1 = new TestModule();
		TestModule tm2 = new TestModule();
		TestModule tm3 = new TestModule();
		mr.put(urn, tm1);
		mr.put(urn, tm2);
		mr.put(urn, tm3);
		assertTrue(mr.contains(urn));
		assertTrue(mr.configurationChanged(des[0].getScope(), 1));
		assertTrue(tm1.configured);
		assertTrue(tm2.configured);
		assertTrue(tm3.configured);
		mr.clear();
		assertFalse(mr.contains(urn));
	}
}
