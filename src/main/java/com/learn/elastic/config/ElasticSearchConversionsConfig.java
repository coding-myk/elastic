package com.learn.elastic.config;

import com.learn.elastic.config.conversion.AddressToMap;
import com.learn.elastic.config.conversion.MapToAddress;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.util.Arrays;

//@Configuration
public class ElasticSearchConversionsConfig extends AbstractElasticsearchConfiguration {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public RestHighLevelClient elasticsearchClient() {
        return restHighLevelClient;
    }


    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(Arrays.asList(new AddressToMap(),new MapToAddress()));
    }
}
