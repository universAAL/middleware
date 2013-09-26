package org.universAAL.middleware.brokers.message.gson;

import java.lang.reflect.Type;
import org.universAAL.middleware.brokers.message.BrokerMessage;
import org.universAAL.middleware.brokers.message.BrokerMessage.BrokerMessageTypes;
import org.universAAL.middleware.brokers.message.aalspace.AALSpaceMessage;
import org.universAAL.middleware.brokers.message.control.ControlMessage;
//import org.universAAL.middleware.brokers.message.deploy.DeployMessage;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BrokerMessageSerializer implements JsonSerializer<BrokerMessage>,
	JsonDeserializer<BrokerMessage> {

    private static final String CLASSNAME = "CLASSNAME";
    private static final String INSTANCE = "INSTANCE";

    public JsonElement serialize(BrokerMessage src, Type typeOfSrc,
	    JsonSerializationContext context) {

	JsonObject retValue = new JsonObject();
	String className = src.getClass().getCanonicalName();
	retValue.addProperty(CLASSNAME, className);
	JsonElement elem = context.serialize(src);
	retValue.add(INSTANCE, elem);
	return retValue;
    }

    public BrokerMessage deserialize(JsonElement json, Type typeOfT,
	    JsonDeserializationContext context) throws JsonParseException {
	BrokerMessage message = null;
	JsonElement ele = json.getAsJsonObject().get("mType");
	if (ele instanceof JsonPrimitive) {
	    JsonPrimitive prim = (JsonPrimitive) ele;
	    String enumValue = ele.getAsString();
	    BrokerMessageTypes myEyum = BrokerMessageTypes.valueOf(enumValue);
	    switch (myEyum) {
//	    case DeployMessage:
//		message = GsonParserBuilder.getInstance().fromJson(json,
//			DeployMessage.class);
//		break;
	    case ControlMessage:
		message = GsonParserBuilder.getInstance().fromJson(json,
			ControlMessage.class);
		break;
	    default:
		break;
	    }

	}

	return message;

    }
}