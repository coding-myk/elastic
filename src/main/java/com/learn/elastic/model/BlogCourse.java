package com.learn.elastic.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoJsonPoint;

/**
 * blog_course
 * @author
 */
@ApiModel(value="com.learn.elastic.model.BlogCourse")
@Data
@Document(indexName = "blog-course",createIndex = true)
@Setting(sortFields = {"courseId"},sortModes = Setting.SortMode.max, sortOrders = {Setting.SortOrder.desc})
@Mapping
public class BlogCourse implements Serializable{

    @Id
    @Field(name = "courseId",type = FieldType.Long)
    private Integer courseId;

    //指定其分词器
    @Field(name = "courseName",type = FieldType.Nested)
    private String courseName;

    @Field(name = "lanId",type = FieldType.Long)
    private Integer lanId;

    @Field(name = "courseValue",type = FieldType.Text)
    private String courseValue;

    /**
     * 逻辑删除
     */
    @Field(name = "isDelete",type = FieldType.Integer)
    @ApiModelProperty(value="逻辑删除")
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
