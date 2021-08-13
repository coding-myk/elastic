package com.learn.elastic.document;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Data
@Document(indexName = "movies")
public class Movies {

    private Long movieId;


    private List<String> genres;

    private List<String> title;

    private Integer year;

    private Integer _version;

}
