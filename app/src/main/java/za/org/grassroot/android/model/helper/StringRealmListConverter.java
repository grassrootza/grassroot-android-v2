package za.org.grassroot.android.model.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import io.realm.RealmList;

/**
 * Created by luke on 2017/08/17.
 */

public class StringRealmListConverter implements
        JsonSerializer<RealmList<RealmString>>, JsonDeserializer<RealmList<RealmString>> {
    @Override
    public JsonElement serialize(RealmList<RealmString> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (RealmString s : src) {
            array.add(context.serialize(s));
        }
        return null;
    }

    @Override
    public RealmList<RealmString> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RealmList<RealmString> strings = new RealmList<>();
        JsonArray array = new JsonArray();
        for (JsonElement element : array) {
            strings.add((RealmString) context.deserialize(element, RealmString.class));
        }
        return strings;
    }
}
