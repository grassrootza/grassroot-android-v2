package za.org.grassroot2.services.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException

import java.lang.reflect.Type

import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.Vote

class TaskDeserlializer : JsonDeserializer<Task> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Task {
        val result: Task
        val `object` = json.asJsonObject
        val type: GrassrootEntityType
        if (`object`.has("type")) {
            type = GrassrootEntityType.valueOf(`object`.get("type").asString)
        } else {
            type = GrassrootEntityType.valueOf(`object`.get("taskType").asString)
        }
        result = when (type) {
            GrassrootEntityType.TODO -> context.deserialize(`object`, Todo::class.java)
            GrassrootEntityType.VOTE -> context.deserialize(`object`, Vote::class.java)
            else -> context.deserialize(`object`, Meeting::class.java)
        }
        return result
    }
}
