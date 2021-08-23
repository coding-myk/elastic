package com.learn.elastic.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.elastic.document.Movies;
import com.learn.elastic.mapper.BlogCourseMapper;
import com.learn.elastic.model.BlogCourse;
import org.elasticsearch.index.query.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TestController {
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;



    @Autowired
    private BlogCourseMapper courseMapper;

    @GetMapping("/test")
    public List<Movies> test(){

        MatchPhraseQueryBuilder phraseQueryBuilder = QueryBuilders.matchPhraseQuery("year",1998);

        Query query = new StringQuery(QueryBuilders.termQuery("year",1999).toString());
        //指定具体字段进行查询
//        query.setFields(List.of("year"));


        SearchHits<Movies> searchHits = elasticsearchOperations.search(query, Movies.class);


        searchHits.forEach(e->{
//            System.out.println(e.toString());
            System.out.println("分数为： "+ e.getScore() + "内容为： " + e.getContent());
        });

        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }


    @GetMapping("/put")
    public void test2() throws JsonProcessingException {

        IndexOperations indexOperations = elasticsearchOperations.indexOps(BlogCourse.class);

        System.out.println(new ObjectMapper().writeValueAsString(indexOperations.getMapping()));

        if(!indexOperations.exists()){
            indexOperations.createWithMapping();
        }



        List<BlogCourse> list = courseMapper.selectCourseList();

        List<IndexQuery> indexQueryList = new ArrayList<>();


        list.forEach(e-> indexQueryList.add(new IndexQueryBuilder()
                .withId(e.getCourseId().toString())
                .withObject(e)
                .build()));



        List<IndexedObjectInformation> informationList = elasticsearchOperations.bulkIndex(indexQueryList,BlogCourse.class);

        System.out.println(JSONObject.toJSONString(informationList));
    }


    @GetMapping("/get")
    public void test3(){



        //termQuery 要加keyword
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("courseName.keyword","C++ 友元类(遥控器-电视机)")).build();

//        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchPhraseQuery("courseName","C++ 友元类(遥控器-电视机)")).build();



//        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.queryStringQuery("C++ 友元类(遥控器-电视机)")).build();


//        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.simpleQueryStringQuery("C++ 友元类(遥控器-电视机)")).build();

//        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("courseName","C++ 友元类(遥控器-电视机)")).build();

        SearchHits<BlogCourse> searchHits = elasticsearchOperations.search(query,BlogCourse.class, IndexCoordinates.of("blog-course"));

        searchHits.forEach(e->{
            System.out.println(e.getContent().getCourseName());
        });

//        BlogCourse blogCourse = elasticsearchOperations.get("1",BlogCourse.class);
//
//        assert blogCourse != null;
//        System.out.println(blogCourse.toString());

        elasticsearchOperations.get("999",BlogCourse.class,IndexCoordinates.of("blog-course"));

        BlogCourse blogCourse1 = new BlogCourse();
        blogCourse1.setCourseId(999);
        blogCourse1.setCourseName("嘿嘿");
        blogCourse1.setCourseValue("hahaha");
        blogCourse1.setLanId(1);
        blogCourse1.setIsDelete(0);

//        BlogCourse result = elasticsearchOperations.save(blogCourse1);

//        System.out.println(result.toString());

        String s = elasticsearchOperations.delete("999",blogCourse1.getClass());

        System.out.println(s);













    }

}
