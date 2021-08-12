package com.learn.elastic;

import com.learn.elastic.document.Movies;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;

import java.util.List;

@SpringBootTest(classes = ElasticApplicationTests.class)
class ElasticApplicationTests {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    void contextLoads() {
        Query query = new StringQuery(QueryBuilders.termQuery("year",1999).toString());
        SearchHits<Movies> searchHits = elasticsearchOperations.search(query, Movies.class);
        System.out.println(searchHits.stream().toString());

    }

}
