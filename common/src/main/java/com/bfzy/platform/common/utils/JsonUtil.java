package com.bfzy.platform.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

/**
 * JSON 工具类.
 * <p>
 * 基于 Jackson 封装，全局复用同一个 {@link ObjectMapper} 实例。
 * </p>
 */
@UtilityClass
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    public String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON: " + obj, e);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getName(), e);
        }
    }

    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + typeReference.getType(), e);
        }
    }

    public <T> List<T> toList(String json, Class<T> elementClass) {
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, elementClass);
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to List<" + elementClass.getName() + ">", e);
        }
    }

    public Map<String, Object> toMap(String json) {
        return toMap(json, String.class, Object.class);
    }

    public <V> Map<String, V> toMap(String json, Class<V> valueClass) {
        return toMap(json, String.class, valueClass);
    }

    public <K, V> Map<K, V> toMap(String json, Class<K> keyClass, Class<V> valueClass) {
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory()
                    .constructMapType(Map.class, keyClass, valueClass);
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to Map<" + keyClass.getName() + ", " + valueClass.getName() + ">", e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
