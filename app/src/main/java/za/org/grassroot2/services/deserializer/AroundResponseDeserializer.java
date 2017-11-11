package za.org.grassroot2.services.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import za.org.grassroot2.model.AroundItem;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.task.Meeting;

public class AroundResponseDeserializer implements JsonDeserializer<AroundItem> {
    @Override
    public AroundItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        AroundItem result;
        JsonObject object = json.getAsJsonObject();
        GrassrootEntityType type;
        if (object.has("type")) {
            type = GrassrootEntityType.valueOf(object.get("type").getAsString());
        } else {
            type = GrassrootEntityType.valueOf(object.get("taskType").getAsString());
        }
        switch (type) {
            case MEETING:
                result = context.deserialize(object, Meeting.class);
                break;
            case GROUP:
            default:
                result = context.deserialize(object, Group.class);
                break;
        }
        return result;
    }
}
