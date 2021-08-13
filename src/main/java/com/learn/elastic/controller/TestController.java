package com.learn.elastic.controller;

import com.learn.elastic.document.Movies;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @GetMapping("/test")
    public void test(){
        Query query = new StringQuery(QueryBuilders.termQuery("_id","0aDwPHsBtmFMIBayNxLr").toString());
        SearchHits<Movies> searchHits = elasticsearchOperations.search(query, Movies.class);

        searchHits.stream().forEach(e-> {
            Movies movies = e.getContent();
            System.out.println(e);
            System.out.println(movies);
        });

        System.out.println(1);
    }

}
