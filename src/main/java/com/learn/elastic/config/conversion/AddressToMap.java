package com.learn.elastic.config.conversion;

import org.apache.tomcat.jni.Address;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.Map;

@WritingConverter
public class AddressToMap implements Converter<Address, Map<String,Object>> {
    @Override
    public Map<String, Object> convert(Address address) {
        return null;
    }
}
