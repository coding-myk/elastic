package com.learn.elastic.document;

import lombok.Data;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.List;

@Data
@Document(indexName = "movies")
public class Movies {


    /**
     @Field: 应用于字段级别，定义字段的属性，大部分属性映射到各自的Elasticsearch Mapping定义（以下列表不完整，完整参考注解Javadoc）:
     name: 将在 Elasticsearch 文档中表示的字段名称，如果未设置，则使用 Java 字段名称。
     type: 字段类型，可以是
                         Text、
                         Keyword、
                         Long、
                         Integer、
                         Short、
                         Byte、
                         Double、
                         Float、
                         Half_Float、
                         Scaled_Float、
                         Date、
                         Date_Nanos、
                         Boolean、
                         Binary、
                         Integer_Range、
                         Float_Range、
                         Long_Range、
                         Double_Range、
                         Date_Range、
                         Ip_Range、
                         Object
                         Nested、
                         Ip、
                         TokenCount、
                         Percolator、
                         Flattened、
                         Search_As_You_Type。查看 Elasticsearch 映射类型(https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html)
     format: 一种或多种内置日期格式.
     pattern: 一种或多种自定义日期格式.
     store: 标记是否应将原始字段值存储在Elasticsearch中，默认值为false.
     analyzer, searchAnalyzer，用于指定自定义分析器和规范器的规范化器.
    */
    @Field(name = "movieId")
    private Long movieId;

    private List<String> genres;

    private List<String> title;

    private Integer year;

    private Long version;

}
