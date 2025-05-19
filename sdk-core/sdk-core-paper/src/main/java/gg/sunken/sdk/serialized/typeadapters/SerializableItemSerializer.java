package gg.sunken.sdk.serialized.typeadapters;

import gg.sunken.sdk.serialized.SerializableItem;
import com.google.gson.*;

import java.lang.reflect.Type;

public class SerializableItemSerializer implements JsonSerializer<SerializableItem>, JsonDeserializer<SerializableItem> {
    @Override
    public SerializableItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return SerializableItem.deserialize(json.getAsJsonObject());
    }

    @Override
    public JsonElement serialize(SerializableItem src, Type typeOfSrc, JsonSerializationContext context) {
        return src.serialize();
    }
}
