package me.paulf.minecraftmania;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;

public final class JsonElements {
    private JsonElements() {
    }

    public static String getString(final JsonObject json, final String memberName) throws JsonSyntaxException {
        final JsonElement element = json.get(memberName);
        if (element != null) {
            return JsonElements.getAsString(json, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a string");
    }

    public static String getAsString(final JsonElement json, final String name) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            return json.getAsString();
        }
        throw new JsonSyntaxException("Expected " + name + " to be a string, was " + JsonElements.toString(json));
    }

    public static JsonObject getAsJsonObject(final JsonElement json, final String name) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject();
        }
        throw new JsonSyntaxException("Expected " + name + " to be an object, was " + JsonElements.toString(json));
    }

    public static JsonArray getAsJsonArray(final JsonElement json, final String name) {
        if (json.isJsonArray()) {
            return json.getAsJsonArray();
        }
        throw new JsonSyntaxException("Expected " + name + " to be an array, was " + JsonElements.toString(json));
    }

    public static String toString(final JsonElement json) {
        if (json == null) return "null (missing)";
        if (json.isJsonNull()) return "null (json)";
        final String s = StringUtils.abbreviateMiddle(String.valueOf(json), "...", 32);
        if (json.isJsonArray()) return "an array (" + s + ")";
        if (json.isJsonObject()) return "an object (" + s + ")";
        if (json.isJsonPrimitive()) {
            final JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isBoolean()) return "a boolean (" + s + ")";
            if (primitive.isNumber()) return "a number (" + s + ")";
            if (primitive.isString()) return "a string (" + s + ")";
        }
        return s;
    }
}
