package com.studyhub.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.studyhub.common.XssUtil;

import java.io.IOException;

/**
 * Jackson 反序列化时自动过滤 XSS
 * 所有 String 类型的字段在反序列化时都会经过 XSS 过滤
 */
public class XssDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        return XssUtil.filter(value);
    }
}
