package com.alp2.photonote.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import com.alp2.photonote.model.PhotoItem;
import com.alp2.photonote.model.PhotoNote;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class PhotoStorage {
    private static final String PREFS_NAME = "PhotoNotePrefs";
    private static final String PHOTOS_KEY = "photos";
    private final SharedPreferences prefs;
    private final Gson gson;

    public PhotoStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        gson = new GsonBuilder()
            .registerTypeAdapter(Uri.class, new UriSerializer())
            .registerTypeAdapter(Uri.class, new UriDeserializer())
            .create();
    }

    public void savePhotos(ArrayList<PhotoItem> photos) {
        String json = gson.toJson(photos);
        prefs.edit().putString(PHOTOS_KEY, json).apply();
    }

    public ArrayList<PhotoItem> loadPhotos() {
        String json = prefs.getString(PHOTOS_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<ArrayList<PhotoItem>>(){}.getType();
        ArrayList<PhotoItem> photos = gson.fromJson(json, type);
        return photos != null ? photos : new ArrayList<>();
    }

    private static class UriSerializer implements JsonSerializer<Uri> {
        @Override
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(json.getAsString());
        }
    }
} 