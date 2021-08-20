package com.learn.elastic.config.conversion;

import org.apache.tomcat.jni.Address;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.Map;

@ReadingConverter
public class MapToAddress implements Converter<Map<String,Object>, Address> {
    @Override
    public Address convert(Map<String, Object> stringObjectMap) {
        return null;
    }
}
