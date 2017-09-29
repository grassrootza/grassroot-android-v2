package za.org.grassroot2.services.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.model.task.Todo;
import za.org.grassroot2.model.task.Vote;

public class TaskDeserlializer implements JsonDeserializer<Task> {
    @Override
    public Task deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Task result;
        JsonObject object = json.getAsJsonObject();
        GrassrootEntityType type = GrassrootEntityType.valueOf(object.get("type").getAsString());
        switch (type) {
            case TODO:
                result = context.deserialize(object, Todo.class);
                break;
            case VOTE:
                result = context.deserialize(object, Vote.class);
                break;
            default:
                result = context.deserialize(object, Meeting.class);
                break;
        }
        return result;
    }
}
