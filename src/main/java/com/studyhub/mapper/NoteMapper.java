package com.studyhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyhub.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    @Update("UPDATE note SET view_count = COALESCE(view_count, 0) + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    @Update("UPDATE note SET like_count = COALESCE(like_count, 0) + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE note SET like_count = GREATEST(COALESCE(like_count, 0) - 1, 0) WHERE id = #{id}")
    int decrementLikeCount(@Param("id") Long id);

    @Update("UPDATE note SET favorite_count = COALESCE(favorite_count, 0) + 1 WHERE id = #{id}")
    int incrementFavoriteCount(@Param("id") Long id);

    @Update("UPDATE note SET favorite_count = GREATEST(COALESCE(favorite_count, 0) - 1, 0) WHERE id = #{id}")
    int decrementFavoriteCount(@Param("id") Long id);
}
