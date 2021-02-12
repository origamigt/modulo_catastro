/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 *
 * @author dfcalderio
 */
public class NumberSerializer implements JsonDeserializer<Number>, JsonSerializer<Number> {

    @Override
    public Number deserialize(JsonElement json, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        try {
            System.out.println(">> " + type + " >> " + json.getAsString());
            return json.getAsNumber();
//            if (number) {
//                int value = json.getAsJsonPrimitive().getAsInt();
//                return value != 0;
//            } else {
//                String value = json.getAsJsonPrimitive().getAsString();
//                return value.equals("TRUE") || value.equals("true");
//            }
        } catch (ClassCastException e) {
            throw new JsonParseException("Cannot parse json boolean '" + json.toString() + "'", e);
        }
    }

    @Override
    public JsonElement serialize(Number t, Type type, JsonSerializationContext jsc) {
        return new JsonPrimitive(t);
    }

}
