/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.censocat.restful;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;
import static com.origami.censocat.restful.Content.error1;
import static com.origami.censocat.restful.Content.success;
import com.origami.sgm.bpm.util.BooleanSerializer;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.HibernateProxyTypeAdapter;

/**
 *
 * @author Angel Navarro
 */
public class JsonUtils implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(JsonUtils.class.getName());

    private transient Gson gson2;
    private transient GsonBuilder builder;

//    private final JsonSerializer<Number> numberSerializer = new JsonSerializer<Number>() {
//        @Override
//        JsonElement serialize(Number number, Type type, JsonSerializationContext context) {
//            Integer intValue = number.toInteger()
//            intValue == number ? new JsonPrimitive(intValue) : new JsonPrimitive(number)
//        }
//    }
    public String generarJson(Object obj) {
        try {
            builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat("yyyy-MM-dd")
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)
                    .registerTypeAdapter(Boolean.class, new BooleanSerializer())
                    .excludeFieldsWithModifiers(Modifier.STATIC)
                    .setPrettyPrinting();
            gson2 = builder.create();
            return gson2.toJson(obj);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Generar Json.", e);
        }
        return null;
    }

    public String getElementFromJson(String json, String field) {
        try {
            builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat("yyyy-MM-dd")
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)
                    .registerTypeAdapter(Boolean.class, new BooleanSerializer())
                    .excludeFieldsWithModifiers(Modifier.STATIC)
                    .setPrettyPrinting();
            gson2 = builder.create();
            JsonObject fromJson = gson2.fromJson(json, JsonObject.class);
            try {
                System.out.println("Get field data json >> " + field);
                return fromJson.get(field).toString();
            } catch (Exception e) {
                return null;
            }
        } catch (JsonSyntaxException e) {
            LOG.log(Level.SEVERE, "Json to Object", e);
        }
        return null;
    }

    public <T> T jsonToObject(String json, Class<T> clazz) {
        try {
            builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat("yyyy-MM-dd")
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .setLongSerializationPolicy(LongSerializationPolicy.DEFAULT)
                    //                    .registerTypeAdapter(Content.class, ContentJsonDeserializer.class)
                    .registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)
                    .registerTypeAdapter(Boolean.class, new BooleanSerializer())
                    //                    .registerTypeAdapter(Number.class, new NumberSerializer())
                    .excludeFieldsWithModifiers(Modifier.STATIC)
                    .setPrettyPrinting();
            gson2 = builder.create();
            return gson2.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            LOG.log(Level.SEVERE, "Json to Object", e);
        }
        return null;
    }

    public String generarJsonStaticModel(Object obj) {
        try {
            builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat("yyyy-MM-dd")
                    .setPrettyPrinting();
            gson2 = builder.create();
            return gson2.toJson(obj);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Generar Json.", e);
        }
        return null;
    }

    public <T> T jsonToObjectStaticModel(String json, Class<T> clazz) {
        try {
            builder = new GsonBuilder();
            builder.excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat("yyyy-MM-dd")
                    .setPrettyPrinting();
            gson2 = builder.create();
            return gson2.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            LOG.log(Level.SEVERE, "Json to Object", e);
        }
        return null;
    }

    final class ContentJsonDeserializer<T> implements JsonDeserializer<Content<Number>> {

        // This deserializer holds no state
        private final JsonDeserializer<?> contentJsonDeserializer = new ContentJsonDeserializer<>();

        private ContentJsonDeserializer() {
        }

        // ... and we hide away that fact not letting this one to be instantiated at call sites
        <T> JsonDeserializer<T> getContentJsonDeserializer() {
            // Narrowing down the @SuppressWarnings scope -- suppressing warnings for entire method may be harmful
            @SuppressWarnings("unchecked")
            JsonDeserializer<T> contentJsonDes = (JsonDeserializer<T>) this.contentJsonDeserializer;
            System.out.println("///// getContentJsonDeserializer");
            return contentJsonDes;
        }

        @Override
        public Content<Number> deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext context)
                throws JsonParseException {
            try {
                System.out.println("deserialize " + jsonElement.getAsString());
                final JsonObject jsonObject = jsonElement.getAsJsonObject();
                final String responseType = jsonObject.getAsJsonPrimitive("type").getAsString();
                switch (responseType) {
                    case "success":
                        return success(context.deserialize(jsonObject.get("data"), getTypeParameter0(type)));
                    case "error":
                        return error1(context.deserialize(jsonObject.get("data"), ApiError.class));
                    default:
                        throw new JsonParseException(responseType);
                }
            } catch (JsonParseException jsonParseException) {
                Logger.getLogger(JsonUtils.class.getName()).log(Level.SEVERE, ">> ", jsonParseException);
                return null;
            }
        }

        // Trying to detect any given type parameterization for its first type parameter
        private Type getTypeParameter0(final Type type) {
            if (!(type instanceof ParameterizedType)) {
                return Object.class;
            }
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        }

    }

}
