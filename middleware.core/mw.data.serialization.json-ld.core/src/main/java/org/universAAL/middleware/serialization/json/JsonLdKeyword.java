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

public enum JsonLdKeyword {
	CONTEXT("@context"), ID("@id"), VALUE("@value"), LANG("@language"), TYPE(
			"@type"), CONTAINER("@container"), LIST("@list"), SET("@set"), REVERSE(
			"@reverse"), INDEX("@index"), BASE("@base"), VOCAB("@vocab"), GRAPH(
			"@graph"),BLANK_NODE("_:") ;
	private final String text;

	/**
	 * @param text
	 */
	JsonLdKeyword(final String text) {
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
			JsonLdKeyword.valueOf(challenge);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}