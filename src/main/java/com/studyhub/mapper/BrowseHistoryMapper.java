package com.studyhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyhub.entity.BrowseHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BrowseHistoryMapper extends BaseMapper<BrowseHistory> {

    @Insert("INSERT INTO browse_history (user_id, note_id) VALUES (#{userId}, #{noteId}) " +
            "ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP")
    int upsert(@Param("userId") Long userId, @Param("noteId") Long noteId);
}
