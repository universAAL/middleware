/*******************************************************************************
 * Copyright 2018 2011 Universidad Politécnica de Madrid
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
package org.universAAL.middleware.serialization.json;

import java.util.HashMap;
import java.util.Map;

/**
 * @author amedrano
 * 
 */
public class Context {

	public enum Keyword {
		ID("@id"), VALUE("@value"), LANG("@language"), TYPE("@type"), CONTAINER(
				"@container"), LIST("@list"), SET("@set"), REVERSE("@reverse"), INDEX(
				"@index"), BASE("@base"), VOCAB("@vocab"), GRAPH("@graph"), ;
		private final String text;

		/**
		 * @param text
		 */
		Keyword(final String text) {
			this.text = text;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return text;
		}

		static public boolean isKeyword(String challenge) {
			try {
				Keyword.valueOf(challenge);
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		}
	}

	public static abstract class ContextTermValue {
		protected String key;

		public ContextTermValue(String key) {
			if (Keyword.isKeyword(key)) {
				throw new IllegalArgumentException(
						"context keys cannot be JSON LD Keywords.");
			}
			this.key = key;
		}
	};

	public static class SimpleTerm extends ContextTermValue {
		private String value;

		public SimpleTerm(String key, String value) {
			super(key);
			this.value = value;
		}
	}

	public static class ExtendedTerm extends ContextTermValue {
		private Map<Keyword, String> extendended = new HashMap<Keyword, String>();

		public ExtendedTerm(String key) {
			super(key);
		}

		// Constructor from JSON

		public void add(Keyword key, String value) {
			this.extendended.put(key, value);
		}
	}

	/**
	 * 
	 */
	public Context() {
		// TODO Auto-generated constructor stub
	}

	public void addTerm(ContextTermValue value) {

	}
}
