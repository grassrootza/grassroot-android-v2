package za.org.grassroot2.services.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException

import java.lang.reflect.Type

import za.org.grassroot2.model.AroundItem
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.task.Meeting

class AroundResponseDeserializer : JsonDeserializer<AroundItem> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): AroundItem {
        val result: AroundItem
        val `object` = json.asJsonObject
        val type: GrassrootEntityType
        if (`object`.has("type")) {
            type = GrassrootEntityType.valueOf(`object`.get("type").asString)
        } else {
            type = GrassrootEntityType.valueOf(`object`.get("taskType").asString)
        }
        result = when (type) {
            GrassrootEntityType.MEETING -> context.deserialize(`object`, Meeting::class.java)
            GrassrootEntityType.GROUP -> context.deserialize(`object`, Group::class.java)
            else -> context.deserialize(`object`, Group::class.java)
        }
        return result
    }
}
