package com.fitness.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.user.model.entity.CoachDetail;
import com.fitness.modules.user.model.vo.CoachHomePageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 教练详情Mapper接口
 */
@Mapper
public interface CoachDetailMapper extends BaseMapper<CoachDetail> {

    /**
     * 根据用户ID查询教练详情
     *
     * @param userId 用户ID
     * @return 教练详情
     */
    @Select("SELECT * FROM coach_detail WHERE user_id = #{userId} AND deleted = false")
    CoachDetail selectByUserId(@Param("userId") Long userId);

    /**
     * 查询首页展示的教练列表
     * 关联sys_user表获取基本信息，只查询角色为教练的用户
     *
     * @param limit 限制数量
     * @return 教练详情列表
     */
    @Select("SELECT cd.*, su.username, su.avatar, su.phone, su.email " +
            "FROM coach_detail cd " +
            "INNER JOIN sys_user su ON cd.user_id = su.id " +
            "INNER JOIN sys_user_role sur ON su.id = sur.user_id " +
            "INNER JOIN sys_role sr ON sur.role_id = sr.id AND sr.role_code = 'COACH' " +
            "WHERE cd.deleted = false AND su.deleted = false AND su.status = 1 " +
            "ORDER BY cd.student_count DESC " +
            "LIMIT #{limit}")
    List<CoachHomePageVO> selectHomePageCoaches(@Param("limit") int limit);
}
