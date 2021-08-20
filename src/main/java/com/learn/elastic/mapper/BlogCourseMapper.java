package com.learn.elastic.mapper;

import com.learn.elastic.model.BlogCourse;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface BlogCourseMapper {
    int deleteByPrimaryKey(Integer courseId);

    int insert(BlogCourse record);

    int insertSelective(BlogCourse record);

    BlogCourse selectByPrimaryKey(Integer courseId);

    int updateByPrimaryKeySelective(BlogCourse record);

    int updateByPrimaryKey(BlogCourse record);

    List<BlogCourse> selectCourseList();
}
