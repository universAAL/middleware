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

package org.universAAL.middleware.brokers.message.gson;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.universAAL.middleware.brokers.message.BrokerMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

/**
 *
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */

public class GsonParserBuilder {

    private static GsonParserBuilder instance = null; // riferimento all'
                                                      // istanza

    private Gson gson;

    public String toJson(Object src) {
        return gson.toJson(src);
    }

    public <T> T fromJson(String json, Class<T> classOfT)
            throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    public <T> T fromJson(JsonElement jsonElement, Class<T> classOfT)
            throws JsonSyntaxException {
        return gson.fromJson(jsonElement, classOfT);
    }

    private GsonParserBuilder() {
    }// costruttore

    private class UnmappedJSonTypeException extends JsonParseException {

        /**
         *
         */
        private static final long serialVersionUID = 8726220629388716348L;

        public UnmappedJSonTypeException(String msg) {
            super(msg);
        }

    }

    private class SerializableSerializer implements
            JsonSerializer<Serializable> {
        public JsonElement serialize(Serializable src, Type typeOfSrc,
                JsonSerializationContext context) {
            return new JsonPrimitive(Serializable.class.toString());
        }
    }

    private class SerializableDeserializer implements
            JsonDeserializer<Serializable> {

        public Serializable deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            if (json.getAsJsonPrimitive().isNumber()) {
                return json.getAsJsonPrimitive().getAsInt();
            }
            if (json.getAsJsonPrimitive().isBoolean()) {
                return json.getAsJsonPrimitive().getAsBoolean();
            }
            if (json.getAsJsonPrimitive().isString()) {
                return json.getAsJsonPrimitive().getAsString();

            } else {
                /**
                 * Unable to infer data type, the supported data type are:
                 * String, Int, Boolean, and array of the mentioned types
                 */
                throw new UnmappedJSonTypeException("Unmapped json data type");
            }
        }
    }

    public static Gson getInstance() {
        synchronized (GsonParserBuilder.class) {
            if (instance == null) {
                instance = new GsonParserBuilder();
                instance.buildGson();
                return instance.gson;
            }
        }
        return instance.gson;
    }

    private Gson buildGson() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Serializable.class,
                        new SerializableDeserializer())
                .registerTypeAdapter(BrokerMessage.class,
                        new BrokerMessageSerializer()).serializeNulls()
                .create();
        return gson;

    }
}
